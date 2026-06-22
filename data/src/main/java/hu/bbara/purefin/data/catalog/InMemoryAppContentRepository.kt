package hu.bbara.purefin.data.catalog

import androidx.datastore.core.DataStore
import hu.bbara.purefin.core.concurrency.SingleFlight
import hu.bbara.purefin.core.data.HomeRepository
import hu.bbara.purefin.core.data.NetworkMonitor
import hu.bbara.purefin.core.data.UserSessionRepository
import hu.bbara.purefin.core.model.MediaUiModel
import hu.bbara.purefin.core.model.MovieUiModel
import hu.bbara.purefin.core.model.SeriesUiModel
import hu.bbara.purefin.data.converter.toEpisode
import hu.bbara.purefin.data.converter.toLibrary
import hu.bbara.purefin.data.converter.toMovie
import hu.bbara.purefin.data.converter.toSeason
import hu.bbara.purefin.data.converter.toSeries
import hu.bbara.purefin.data.jellyfin.client.JellyfinApiClient
import hu.bbara.purefin.data.offline.cache.HomeCache
import hu.bbara.purefin.data.offline.cache.toCachedEpisode
import hu.bbara.purefin.data.offline.cache.toCachedItem
import hu.bbara.purefin.data.offline.cache.toCachedLibrary
import hu.bbara.purefin.data.offline.cache.toCachedMovie
import hu.bbara.purefin.data.offline.cache.toCachedSeries
import hu.bbara.purefin.data.offline.cache.toEpisode
import hu.bbara.purefin.data.offline.cache.toLibrary
import hu.bbara.purefin.data.offline.cache.toMedia
import hu.bbara.purefin.data.offline.cache.toMovie
import hu.bbara.purefin.data.offline.cache.toSeries
import hu.bbara.purefin.model.Library
import hu.bbara.purefin.model.LibraryKind
import hu.bbara.purefin.model.Media
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@Singleton
class InMemoryAppContentRepository @Inject constructor(
    val userSessionRepository: UserSessionRepository,
    val jellyfinApiClient: JellyfinApiClient,
    private val homeCacheDataStore: DataStore<HomeCache>,
    private val onlineMediaRepository: InMemoryLocalMediaRepository,
    private val networkMonitor: NetworkMonitor,
    private val singleFlight: SingleFlight,
) : HomeRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var cacheLoadJob: Job? = null
    private var refreshJob: Job? = null

    @OptIn(ExperimentalAtomicApi::class)
    private val initialized = AtomicBoolean(false)

    private val librariesState = MutableStateFlow<List<Library>>(emptyList())
    override val libraries: StateFlow<List<Library>> = librariesState.asStateFlow()

    private val suggestionsState = MutableStateFlow<List<Media>>(emptyList())
    override val suggestions: StateFlow<List<Media>> = suggestionsState.asStateFlow()

    private val continueWatchingState = MutableStateFlow<List<Media>>(emptyList())
    override val continueWatching: StateFlow<List<Media>> = continueWatchingState.asStateFlow()

    private val nextUpState = MutableStateFlow<List<Media>>(emptyList())
    override val nextUp: StateFlow<List<Media>> = nextUpState.asStateFlow()

    private val latestLibraryContentState = MutableStateFlow<Map<UUID, List<Media>>>(emptyMap())
    override val latestLibraryContent: StateFlow<Map<UUID, List<Media>>> = latestLibraryContentState.asStateFlow()

    // dateLastMediaAdded values captured at the end of the last successful
    // refresh. Used to short-circuit per-library /Items/Latest calls when
    // the timestamp hasn't moved. Persisted via HomeCache.
    private val cachedLibraryDateLastMediaAdded = mutableMapOf<UUID, String>()

    // dateLastMediaAdded values observed during the CURRENT refresh's
    // /UserViews response. Read by loadLatestLibraryContent to decide
    // which libraries need a /Items/Latest call, then merged into
    // cachedLibraryDateLastMediaAdded and persisted at the end.
    private var currentLibraryDateLastMediaAdded: Map<UUID, String> = emptyMap()

    init {
        ensureReady()
    }

    @OptIn(ExperimentalAtomicApi::class)
    override fun ensureReady() {
        if (!initialized.compareAndSet(expectedValue = false, newValue = true)) {
            return
        }
        Timber.tag(TAG).d("Initializing home repository")
        val loadJob = scope.launch { loadHomeCache() }
        cacheLoadJob = loadJob
        scope.launch {
            loadJob.join()
            refreshHomeData()
        }
    }

    override suspend fun refreshHomeData() {
        cacheLoadJob?.join()
        val job = synchronized(this) {
            refreshJob?.takeIf { it.isActive } ?: scope.launch {
                val snapshot = snapshotHomeContent()
                try {
                    Timber.tag(TAG).d("Refreshing home data")
                    if (!networkMonitor.isOnline.first()) {
                        return@launch
                    }
                    loadLibraries()
                    loadSuggestions()
                    loadContinueWatching()
                    loadNextUp()
                    loadLatestLibraryContent()
                    Timber.tag(TAG).d("Home refresh successful")
                    persistHomeCache()
                } catch (error: CancellationException) {
                    throw error
                } catch (error: HomeRefreshFailedException) {
                    restoreHomeContent(snapshot)
                    Timber.tag(TAG).w(error.cause, "Home refresh failed; keeping cached content")
                } catch (error: Exception) {
                    restoreHomeContent(snapshot)
                    Timber.tag(TAG).w(error, "Home refresh failed; keeping cached content")
                    networkMonitor.checkConnection()
                }
            }.also { refreshJob = it }
        }
        job.join()
    }

    private suspend fun loadHomeCache() {
        Timber.tag(TAG).d("Loading home cache")
        val cache = homeCacheDataStore.data.first()
        // Hydrate the dateLastMediaAdded map from disk before the first
        // refresh runs, so the per-library short-circuit has a baseline.
        cachedLibraryDateLastMediaAdded.clear()
        cache.libraryDateLastMediaAdded.forEach { (key, date) ->
            runCatching { UUID.fromString(key) }.getOrNull()?.let { uuid ->
                cachedLibraryDateLastMediaAdded[uuid] = date
            }
        }
        if (cache.libraries.isNotEmpty()) {
            val libraries = cache.libraries.mapNotNull { it.toLibrary() }
            librariesState.value = libraries
            onlineMediaRepository.upsertMovies(
                libraries.filter { it.type == LibraryKind.MOVIES }.flatMap { it.movies.orEmpty() }
            )
            onlineMediaRepository.upsertSeries(
                libraries.filter { it.type == LibraryKind.SERIES }.flatMap { it.series.orEmpty() }
            )
        }
        if (cache.suggestions.isNotEmpty()) {
            suggestionsState.value = cache.suggestions.mapNotNull { it.toMedia() }
        }
        if (cache.continueWatching.isNotEmpty()) {
            continueWatchingState.value = cache.continueWatching.mapNotNull { it.toMedia() }
        }
        if (cache.nextUp.isNotEmpty()) {
            nextUpState.value = cache.nextUp.mapNotNull { it.toMedia() }
        }
        if (cache.latestLibraryContent.isNotEmpty()) {
            latestLibraryContentState.value = cache.latestLibraryContent.mapNotNull { (key, items) ->
                val uuid = runCatching { UUID.fromString(key) }.getOrNull() ?: return@mapNotNull null
                uuid to items.mapNotNull { it.toMedia() }
            }.toMap()
        }
        if (cache.movies.isNotEmpty()) {
            onlineMediaRepository.upsertMovies(cache.movies.mapNotNull { it.toMovie() })
        }
        if (cache.series.isNotEmpty()) {
            onlineMediaRepository.upsertSeries(cache.series.mapNotNull { it.toSeries() })
        }
        if (cache.episodes.isNotEmpty()) {
            onlineMediaRepository.upsertEpisodes(cache.episodes.mapNotNull { it.toEpisode() })
        }
        Timber.tag(TAG).d("Home cache loaded")
    }

    private suspend fun persistHomeCache() {
        val referencedMediaIds = collectReferencedMediaIds()
        val movies = onlineMediaRepository.movies.value
        val series = onlineMediaRepository.series.value
        val episodes = onlineMediaRepository.episodes.value
        // Persist this refresh's dateLastMediaAdded as the new baseline
        // for the next refresh's per-library short-circuit.
        cachedLibraryDateLastMediaAdded.clear()
        currentLibraryDateLastMediaAdded.forEach { (id, date) ->
            cachedLibraryDateLastMediaAdded[id] = date
        }
        val cache = HomeCache(
            suggestions = suggestionsState.value.map { it.toCachedItem() },
            continueWatching = continueWatchingState.value.map { it.toCachedItem() },
            nextUp = nextUpState.value.map { it.toCachedItem() },
            latestLibraryContent = latestLibraryContentState.value.map { (uuid, items) ->
                uuid.toString() to items.map { it.toCachedItem() }
            }.toMap(),
            libraries = librariesState.value.map { it.toCachedLibrary() },
            libraryDateLastMediaAdded = cachedLibraryDateLastMediaAdded.mapKeys { it.key.toString() },
            movies = referencedMediaIds.movieIds.mapNotNull { movies[it] }.map { it.toCachedMovie() },
            series = referencedMediaIds.seriesIds.mapNotNull { series[it] }.map { it.toCachedSeries() },
            episodes = referencedMediaIds.episodeIds.mapNotNull { episodes[it] }.map { it.toCachedEpisode() },
        )
        homeCacheDataStore.updateData { cache }
    }

    private fun collectReferencedMediaIds(): ReferencedHomeMediaIds {
        val movieIds = mutableSetOf<UUID>()
        val seriesIds = mutableSetOf<UUID>()
        val episodeIds = mutableSetOf<UUID>()
        val referencedMedia = buildList {
            addAll(suggestionsState.value)
            addAll(continueWatchingState.value)
            addAll(nextUpState.value)
            latestLibraryContentState.value.values.forEach { addAll(it) }
        }

        referencedMedia.forEach { media ->
            when (media) {
                is Media.MovieMedia -> movieIds += media.movieId
                is Media.SeriesMedia -> seriesIds += media.seriesId
                is Media.SeasonMedia -> seriesIds += media.seriesId
                is Media.EpisodeMedia -> episodeIds += media.episodeId
            }
        }

        return ReferencedHomeMediaIds(
            movieIds = movieIds,
            seriesIds = seriesIds,
            episodeIds = episodeIds,
        )
    }

    private suspend fun loadLibraries() = singleFlight.run("AppContent:loadLibraries") {
        val librariesItem = runCatching { jellyfinApiClient.getLibraries() }
            .getOrElse { error ->
                handleRefreshFailure(error, "Unable to load libraries")
            }

        // Build the new library list in a single pass, so collectors of
        // `librariesState` only ever see the final value. When /UserViews
        // returns 304 we already have the BaseItemDto list in memory, so
        // we just rebuild from it. Otherwise we have the new DTOs.
        val filledLibraries: List<Library> = if (librariesItem == null) {
            // ETag 304 on /UserViews — re-fetch the count per library
            // against the existing in-memory state. The size fallback
            // (`?: library.size`) handles a 304 on /Items too, so a
            // 304 on both endpoints leaves Library.size untouched.
            librariesState.value.map { library ->
                val count = jellyfinApiClient.getLibraryItemCount(library.id) ?: library.size
                when (library.type) {
                    LibraryKind.MOVIES -> library.copy(movies = emptyList(), size = count)
                    LibraryKind.SERIES -> library.copy(series = emptyList(), size = count)
                }
            }
        } else {
            // Capture the server-reported "last media added" timestamp
            // per library for this refresh. loadLatestLibraryContent
            // reads this map to decide which libraries need a
            // /Items/Latest call, and persistHomeCache writes it to
            // HomeCache as the new "cached" baseline.
            currentLibraryDateLastMediaAdded = librariesItem
                .mapNotNull { dto ->
                    dto.dateLastMediaAdded?.let { dto.id to it.toString() }
                }
                .toMap()

            // Count-only refresh: ask the server for the number of items
            // in each library instead of enumerating them. The home
            // dashboard only uses Library.size for the library cards;
            // the full movies/series lists are fetched on demand by the
            // library detail screen via loadLibraryContent, and on the
            // home rows by loadSuggestions / loadContinueWatching /
            // loadLatestLibraryContent.
            librariesItem.mapNotNull { dto ->
                val library = dto.toLibrary(serverUrl()) ?: return@mapNotNull null
                val count = jellyfinApiClient.getLibraryItemCount(library.id) ?: library.size
                when (library.type) {
                    LibraryKind.MOVIES -> library.copy(movies = emptyList(), size = count)
                    LibraryKind.SERIES -> library.copy(series = emptyList(), size = count)
                }
            }
        }
        librariesState.value = filledLibraries
    }

    override suspend fun loadLibraryContent(libraryId: UUID): List<MediaUiModel>? =
        singleFlight.run("AppContent:loadLibraryContent:$libraryId") {
            val library = librariesState.value.find { it.id == libraryId } ?: return@run emptyList()
            // ETag hit — return null so the caller can keep its previously
            // fetched copy. The library detail viewmodel uses this signal to
            // preserve its in-memory cache across re-selections.
            val items = jellyfinApiClient.getLibraryContent(libraryId) ?: return@run null
            val url = serverUrl()
            when (library.type) {
                LibraryKind.MOVIES -> items.map { MovieUiModel(it.toMovie(url)) }
                LibraryKind.SERIES -> items.map { SeriesUiModel(it.toSeries(url)) }
            }
        }

    private suspend fun loadSuggestions() = singleFlight.run("AppContent:loadSuggestions") {
        val url = serverUrl()
        // Head-first fetch: ask the server for just the first 2 items and
        // compare to the cached head. If the head is unchanged, the rest
        // of the row is also unchanged (the home screen shows the head
        // prominently and only rotates when new content arrives at the
        // top), so we can skip the full request entirely.
        val headItems = runCatching { jellyfinApiClient.getSuggestionsHead() }
            .getOrElse { error ->
                handleRefreshFailure(error, "Unable to load suggestions head")
            }
        if (headItems == null) return@run
        if (headMatches(headItems, suggestionsState.value)) return@run

        // Head changed (or first refresh) — fetch the full list.
        val suggestionsItems = runCatching { jellyfinApiClient.getSuggestions() }
            .getOrElse { error ->
                handleRefreshFailure(error, "Unable to load suggestions")
            }
        if (suggestionsItems == null) return@run

        suggestionsState.value = suggestionsItems.mapNotNull { item ->
            when (item.type) {
                BaseItemKind.MOVIE -> Media.MovieMedia(movieId = item.id)
                BaseItemKind.EPISODE -> Media.EpisodeMedia(episodeId = item.id, seriesId = item.seriesId!!)
                else -> throw UnsupportedOperationException("Unsupported item type: ${item.type}")
            }
        }

        // Upsert full details so the home viewmodel can look up each item.
        suggestionsItems.forEach { item ->
            when (item.type) {
                BaseItemKind.MOVIE -> onlineMediaRepository.upsertMovies(listOf(item.toMovie(url)))
                BaseItemKind.EPISODE -> onlineMediaRepository.upsertEpisodes(listOf(item.toEpisode(url)))
                else -> {}
            }
        }
    }

    private suspend fun loadContinueWatching() = singleFlight.run("AppContent:loadContinueWatching") {
        val url = serverUrl()
        val headItems = runCatching { jellyfinApiClient.getContinueWatchingHead() }
            .getOrElse { error ->
                handleRefreshFailure(error, "Unable to load continue watching head")
            }
        if (headItems == null) return@run
        if (headMatches(headItems, continueWatchingState.value)) return@run

        val continueWatchingItems = runCatching { jellyfinApiClient.getContinueWatching() }
            .getOrElse { error ->
                handleRefreshFailure(error, "Unable to load continue watching")
            }
        if (continueWatchingItems == null) return@run

        continueWatchingState.value = continueWatchingItems.mapNotNull { item ->
            when (item.type) {
                BaseItemKind.MOVIE -> Media.MovieMedia(movieId = item.id)
                BaseItemKind.EPISODE -> Media.EpisodeMedia(episodeId = item.id, seriesId = item.seriesId!!)
                else -> throw UnsupportedOperationException("Unsupported item type: ${item.type}")
            }
        }

        continueWatchingItems.forEach { item ->
            when (item.type) {
                BaseItemKind.MOVIE -> onlineMediaRepository.upsertMovies(listOf(item.toMovie(url)))
                BaseItemKind.EPISODE -> onlineMediaRepository.upsertEpisodes(listOf(item.toEpisode(url)))
                else -> {}
            }
        }
    }

    private suspend fun loadNextUp() = singleFlight.run("AppContent:loadNextUp") {
        val url = serverUrl()
        val headItems = runCatching { jellyfinApiClient.getNextUpHead() }
            .getOrElse { error ->
                handleRefreshFailure(error, "Unable to load next up head")
            }
        if (headItems == null) return@run
        if (headMatches(headItems, nextUpState.value)) return@run

        val nextUpItems = runCatching { jellyfinApiClient.getNextUpEpisodes() }
            .getOrElse { error ->
                handleRefreshFailure(error, "Unable to load next up")
            }
        if (nextUpItems == null) return@run

        nextUpState.value = nextUpItems.map { item ->
            Media.EpisodeMedia(episodeId = item.id, seriesId = item.seriesId!!)
        }

        nextUpItems.forEach { item ->
            onlineMediaRepository.upsertEpisodes(listOf(item.toEpisode(url)))
        }
    }

    private suspend fun loadLatestLibraryContent() = singleFlight.run("AppContent:loadLatestLibraryContent") {
        // Reuse the libraries already loaded by loadLibraries() so we don't refetch
        // the same /Users/{userId}/Views response a second time per refresh.
        val filteredLibraries = librariesState.value
        val url = serverUrl()
        val latestLibraryContents = filteredLibraries.associate { library ->
            // Layer 1: skip when the server-reported "last media added"
            // timestamp is unchanged since the last refresh. This is the
            // cheapest check (no request) and handles the common "no new
            // content" case.
            val cachedDate = cachedLibraryDateLastMediaAdded[library.id]
            val currentDate = currentLibraryDateLastMediaAdded[library.id]
            if (cachedDate != null && cachedDate == currentDate) {
                library.id to (latestLibraryContentState.value[library.id] ?: emptyList())
            } else {
                // Layer 2: head-only fetch. If the first 2 items of the
                // latest row are the same as the cached head, the rest of
                // the row is also unchanged (the home row only rotates at
                // the top) and we can skip the full request.
                val headDtos = runCatching { jellyfinApiClient.getLatestFromLibraryHead(library.id) }
                    .getOrElse { error ->
                        handleRefreshFailure(error, "Unable to load latest head for library ${library.id}")
                    }
                val cachedSlice = latestLibraryContentState.value[library.id].orEmpty()
                if (headDtos == null || headMatches(headDtos, cachedSlice)) {
                    library.id to cachedSlice
                } else {
                    // Layer 3: head changed — fetch the full row.
                    val latestFromLibrary = runCatching { jellyfinApiClient.getLatestFromLibrary(library.id) }
                        .getOrElse { error ->
                            handleRefreshFailure(error, "Unable to load latest items for library ${library.id}")
                        }
                    if (latestFromLibrary == null) {
                        library.id to cachedSlice
                    } else {
                        val media = when (library.type) {
                            LibraryKind.MOVIES -> latestFromLibrary.map {
                                val movie = it.toMovie(url)
                                onlineMediaRepository.upsertMovies(listOf(movie))
                                Media.MovieMedia(movieId = movie.id)
                            }
                            LibraryKind.SERIES -> latestFromLibrary.map { dto ->
                                when (dto.type) {
                                    BaseItemKind.SERIES -> {
                                        val series = dto.toSeries(url)
                                        onlineMediaRepository.upsertSeries(listOf(series))
                                        Media.SeriesMedia(seriesId = series.id)
                                    }
                                    BaseItemKind.SEASON -> {
                                        val season = dto.toSeason()
                                        Media.SeasonMedia(seasonId = season.id, seriesId = season.seriesId)
                                    }
                                    BaseItemKind.EPISODE -> {
                                        val episode = dto.toEpisode(url)
                                        onlineMediaRepository.upsertEpisodes(listOf(episode))
                                        Media.EpisodeMedia(episodeId = episode.id, seriesId = episode.seriesId)
                                    }
                                    else -> throw UnsupportedOperationException("Unsupported item type: ${dto.type}")
                                }
                            }
                        }
                        library.id to media
                    }
                }
            }
        }
        latestLibraryContentState.value = latestLibraryContents
    }

    /**
     * Returns true when the first [JellyfinApiClient.HEAD_LIMIT] IDs of the
     * freshly fetched [headDtos] match the first
     * [JellyfinApiClient.HEAD_LIMIT] IDs of the cached [cachedMedia]. Used
     * by the home row refreshers to short-circuit the full request when
     * the head of the row is unchanged.
     *
     * Order-sensitive: the comparison is positional. The Jellyfin server
     * can reorder rows (e.g., Suggestions reorders on a relevance
     * recompute even when the item set is unchanged), which will report
     * a false "head changed" and pay for the full request. The reverse
     * false negative — head order unchanged but a mid-list item rotated —
     * is a known limitation of the head-diff heuristic.
     */
    private fun headMatches(
        headDtos: List<BaseItemDto>,
        cachedMedia: List<Media>,
    ): Boolean {
        val headIds = headDtos.take(JellyfinApiClient.HEAD_LIMIT).map { it.id }
        val cachedIds = cachedMedia.take(JellyfinApiClient.HEAD_LIMIT).map { it.id }
        return headIds == cachedIds
    }

    private suspend fun serverUrl(): String {
        return userSessionRepository.serverUrl.first()
    }

    private fun snapshotHomeContent(): HomeContentSnapshot = HomeContentSnapshot(
        libraries = librariesState.value,
        suggestions = suggestionsState.value,
        continueWatching = continueWatchingState.value,
        nextUp = nextUpState.value,
        latestLibraryContent = latestLibraryContentState.value,
    )

    private fun restoreHomeContent(snapshot: HomeContentSnapshot) {
        librariesState.value = snapshot.libraries
        suggestionsState.value = snapshot.suggestions
        continueWatchingState.value = snapshot.continueWatching
        nextUpState.value = snapshot.nextUp
        latestLibraryContentState.value = snapshot.latestLibraryContent
    }

    private suspend fun handleRefreshFailure(error: Throwable, message: String): Nothing {
        if (error is CancellationException) {
            throw error
        }
        Timber.tag(TAG).w(error, message)
        networkMonitor.checkConnection()
        throw HomeRefreshFailedException(error)
    }

    companion object {
        private const val TAG = "InMemoryAppContentRepo"
    }
}

private data class ReferencedHomeMediaIds(
    val movieIds: Set<UUID>,
    val seriesIds: Set<UUID>,
    val episodeIds: Set<UUID>,
)

private data class HomeContentSnapshot(
    val libraries: List<Library>,
    val suggestions: List<Media>,
    val continueWatching: List<Media>,
    val nextUp: List<Media>,
    val latestLibraryContent: Map<UUID, List<Media>>,
)

private class HomeRefreshFailedException(cause: Throwable) : RuntimeException(cause)