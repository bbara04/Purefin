package hu.bbara.purefin.data.catalog

import androidx.datastore.core.DataStore
import hu.bbara.purefin.core.data.HomeRepository
import hu.bbara.purefin.core.data.NetworkMonitor
import hu.bbara.purefin.core.data.UserSessionRepository
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
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.CollectionType
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import timber.log.Timber

@Singleton
class InMemoryAppContentRepository @Inject constructor(
    val userSessionRepository: UserSessionRepository,
    val jellyfinApiClient: JellyfinApiClient,
    private val homeCacheDataStore: DataStore<HomeCache>,
    private val onlineMediaRepository: InMemoryLocalMediaRepository,
    private val networkMonitor: NetworkMonitor,
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

    private suspend fun loadLibraries() {
        val librariesItem = runCatching { jellyfinApiClient.getLibraries() }
            .getOrElse { error ->
                handleRefreshFailure(error, "Unable to load libraries")
            }
        // Decide which libraries to refresh content for. When the libraries
        // endpoint returns 304 (ETag hit), the library list hasn't changed,
        // but per-library content is a separate query with its own ETag, so
        // we still need to call getLibraryContent for each library to pick
        // up content additions that the libraries endpoint doesn't surface.
        val librariesToProcess: List<Library> = if (librariesItem == null) {
            librariesState.value
        } else {
            val filtered = librariesItem.filter {
                it.collectionType == CollectionType.MOVIES || it.collectionType == CollectionType.TVSHOWS
            }
            // Capture the server-reported "last media added" timestamp per
            // library for this refresh. loadLatestLibraryContent reads
            // this map to decide which libraries need a /Items/Latest call,
            // and persistHomeCache writes it to HomeCache as the new
            // "cached" baseline for the next refresh.
            currentLibraryDateLastMediaAdded = filtered
                .mapNotNull { dto ->
                    dto.dateLastMediaAdded?.let { dto.id to it.toString() }
                }
                .toMap()
            val emptyLibraries = filtered.map { it.toLibrary(serverUrl()) }
            librariesState.value = emptyLibraries
            emptyLibraries
        }

        val filledLibraries = librariesToProcess.map { loadLibrary(it) }
        librariesState.value = filledLibraries

        val movies = filledLibraries.filter { it.type == LibraryKind.MOVIES }.flatMap { it.movies.orEmpty() }
        onlineMediaRepository.upsertMovies(movies)

        val series = filledLibraries.filter { it.type == LibraryKind.SERIES }.flatMap { it.series.orEmpty() }
        onlineMediaRepository.upsertSeries(series)
    }

    private suspend fun loadLibrary(library: Library): Library {
        val contentItem = runCatching { jellyfinApiClient.getLibraryContent(library.id) }
            .getOrElse { error ->
                handleRefreshFailure(error, "Unable to load library ${library.id}")
            }
        // ETag hit — the library's content is unchanged. Return the original
        // library so the existing movies/series/size stay in state.
        if (contentItem == null) {
            return library
        }
        return when (library.type) {
            LibraryKind.MOVIES -> library.copy(
                movies = contentItem.map { it.toMovie(serverUrl()) },
                size = contentItem.size,
            )
            LibraryKind.SERIES -> library.copy(
                series = contentItem.map { it.toSeries(serverUrl()) },
                size = contentItem.size,
            )
        }
    }

    private suspend fun loadSuggestions() {
        val suggestionsItems = runCatching { jellyfinApiClient.getSuggestions() }
            .getOrElse { error ->
                handleRefreshFailure(error, "Unable to load suggestions")
            }
        // ETag hit — suggestions are unchanged, keep the existing state.
        if (suggestionsItems == null) {
            return
        }
        suggestionsState.value = suggestionsItems.mapNotNull { item ->
            when (item.type) {
                BaseItemKind.MOVIE -> Media.MovieMedia(movieId = item.id)
                BaseItemKind.EPISODE -> Media.EpisodeMedia(episodeId = item.id, seriesId = item.seriesId!!)
                else -> throw UnsupportedOperationException("Unsupported item type: ${item.type}")
            }
        }

        suggestionsItems.forEach { item ->
            if (item.type == BaseItemKind.EPISODE) {
                onlineMediaRepository.upsertEpisodes(listOf(item.toEpisode(serverUrl())))
            }
        }
    }

    private suspend fun loadContinueWatching() {
        val continueWatchingItems = runCatching { jellyfinApiClient.getContinueWatching() }
            .getOrElse { error ->
                handleRefreshFailure(error, "Unable to load continue watching")
            }
        // ETag hit — continue watching is unchanged, keep the existing state.
        if (continueWatchingItems == null) {
            return
        }
        continueWatchingState.value = continueWatchingItems.mapNotNull { item ->
            when (item.type) {
                BaseItemKind.MOVIE -> Media.MovieMedia(movieId = item.id)
                BaseItemKind.EPISODE -> Media.EpisodeMedia(episodeId = item.id, seriesId = item.seriesId!!)
                else -> throw UnsupportedOperationException("Unsupported item type: ${item.type}")
            }
        }

        continueWatchingItems.forEach { item ->
            if (item.type == BaseItemKind.EPISODE) {
                onlineMediaRepository.upsertEpisodes(listOf(item.toEpisode(serverUrl())))
            }
        }
    }

    private suspend fun loadNextUp() {
        val nextUpItems = runCatching { jellyfinApiClient.getNextUpEpisodes() }
            .getOrElse { error ->
                handleRefreshFailure(error, "Unable to load next up")
            }
        // ETag hit — next up is unchanged, keep the existing state.
        if (nextUpItems == null) {
            return
        }
        nextUpState.value = nextUpItems.map { item ->
            Media.EpisodeMedia(episodeId = item.id, seriesId = item.seriesId!!)
        }

        nextUpItems.forEach { item ->
            onlineMediaRepository.upsertEpisodes(listOf(item.toEpisode(serverUrl())))
        }
    }

    private suspend fun loadLatestLibraryContent() {
        // Reuse the libraries already loaded by loadLibraries() so we don't refetch
        // the same /Users/{userId}/Views response a second time per refresh.
        val filteredLibraries = librariesState.value
        val url = serverUrl()
        val latestLibraryContents = filteredLibraries.associate { library ->
            // Skip the per-library GET when the server-reported "last media added"
            // timestamp is identical to the one we saw last refresh. This is
            // Jellyfin's stable signal that no new content was added, and it lets
            // us avoid one request per library on the common case. ETag on
            // /Items/Latest remains the fallback for changes that don't bump the
            // timestamp (e.g. metadata refreshes that don't add media).
            val cachedDate = cachedLibraryDateLastMediaAdded[library.id]
            val currentDate = currentLibraryDateLastMediaAdded[library.id]
            if (cachedDate != null && cachedDate == currentDate) {
                library.id to (latestLibraryContentState.value[library.id] ?: emptyList())
            } else {
                val latestFromLibrary = runCatching { jellyfinApiClient.getLatestFromLibrary(library.id) }
                    .getOrElse { error ->
                        handleRefreshFailure(error, "Unable to load latest items for library ${library.id}")
                    }
                // ETag hit — /Items/Latest is unchanged, keep the previous slice.
                if (latestFromLibrary == null) {
                    library.id to (latestLibraryContentState.value[library.id] ?: emptyList())
                } else {
                    library.id to when (library.type) {
                        LibraryKind.MOVIES -> latestFromLibrary.map {
                            val movie = it.toMovie(url)
                            Media.MovieMedia(movieId = movie.id)
                        }
                        LibraryKind.SERIES -> latestFromLibrary.map {
                            when (it.type) {
                                BaseItemKind.SERIES -> {
                                    val series = it.toSeries(url)
                                    Media.SeriesMedia(seriesId = series.id)
                                }
                                BaseItemKind.SEASON -> {
                                    val season = it.toSeason()
                                    Media.SeasonMedia(seasonId = season.id, seriesId = season.seriesId)
                                }
                                BaseItemKind.EPISODE -> {
                                    val episode = it.toEpisode(url)
                                    Media.EpisodeMedia(episodeId = episode.id, seriesId = episode.seriesId)
                                }
                                else -> throw UnsupportedOperationException("Unsupported item type: ${it.type}")
                            }
                        }
                    }
                }
            }
        }
        latestLibraryContentState.value = latestLibraryContents
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