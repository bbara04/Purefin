package hu.bbara.purefin.data.catalog

import android.util.Log
import androidx.datastore.core.DataStore
import hu.bbara.purefin.core.data.HomeRepository
import hu.bbara.purefin.core.data.NetworkMonitor
import hu.bbara.purefin.core.image.ArtworkKind
import hu.bbara.purefin.data.offline.cache.CachedMediaItem
import hu.bbara.purefin.data.offline.cache.HomeCache
import hu.bbara.purefin.data.jellyfin.client.JellyfinApiClient
import hu.bbara.purefin.core.image.ImageUrlBuilder
import hu.bbara.purefin.core.data.session.UserSessionRepository
import hu.bbara.purefin.core.model.Episode
import hu.bbara.purefin.core.model.Library
import hu.bbara.purefin.core.model.LibraryKind
import hu.bbara.purefin.core.model.Media
import hu.bbara.purefin.core.model.Movie
import hu.bbara.purefin.core.model.Season
import hu.bbara.purefin.core.model.Series
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.CollectionType

@Singleton
class InMemoryAppContentRepository @Inject constructor(
    val userSessionRepository: UserSessionRepository,
    val jellyfinApiClient: JellyfinApiClient,
    private val homeCacheDataStore: DataStore<HomeCache>,
    private val onlineMediaRepository: InMemoryMediaRepository,
    private val networkMonitor: NetworkMonitor,
) : HomeRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val ensureReadyMutex = Mutex()
    private var refreshJob: Job? = null
    private var cacheHydrated = false
    private var initialized = false

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

    init {
        scope.launch {
            ensureReady()
        }
    }

    override suspend fun ensureReady() {
        ensureReadyMutex.withLock {
            hydrateCacheIfNeeded()
            if (initialized) {
                return
            }
            initialized = true
        }
        refreshHomeData()
    }

    override suspend fun refreshHomeData() {
        ensureReadyMutex.withLock {
            hydrateCacheIfNeeded()
        }
        if (refreshJob?.isActive == true) {
            refreshJob?.join()
            return
        }
        val job = scope.launch {
            runCatching {
                if (!networkMonitor.isOnline.first()) {
                    return@runCatching
                }
                loadLibraries()
                loadSuggestions()
                loadContinueWatching()
                loadNextUp()
                loadLatestLibraryContent()
                persistHomeCache()
            }.onFailure { error ->
                Log.w(TAG, "Home refresh failed; keeping cached content", error)
            }
        }
        refreshJob = job
        job.join()
    }

    private suspend fun hydrateCacheIfNeeded() {
        if (cacheHydrated) return
        loadFromCache()
        cacheHydrated = true
    }

    private suspend fun loadFromCache() {
        val cache = homeCacheDataStore.data.first()
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
    }

    private suspend fun persistHomeCache() {
        val cache = HomeCache(
            suggestions = suggestionsState.value.map { it.toCachedItem() },
            continueWatching = continueWatchingState.value.map { it.toCachedItem() },
            nextUp = nextUpState.value.map { it.toCachedItem() },
            latestLibraryContent = latestLibraryContentState.value.map { (uuid, items) ->
                uuid.toString() to items.map { it.toCachedItem() }
            }.toMap(),
        )
        homeCacheDataStore.updateData { cache }
    }

    private fun Media.toCachedItem(): CachedMediaItem = when (this) {
        is Media.MovieMedia -> CachedMediaItem(type = "MOVIE", id = movieId.toString())
        is Media.SeriesMedia -> CachedMediaItem(type = "SERIES", id = seriesId.toString())
        is Media.SeasonMedia -> CachedMediaItem(type = "SEASON", id = seasonId.toString(), seriesId = seriesId.toString())
        is Media.EpisodeMedia -> CachedMediaItem(type = "EPISODE", id = episodeId.toString(), seriesId = seriesId.toString())
    }

    private fun CachedMediaItem.toMedia(): Media? {
        val uuid = runCatching { UUID.fromString(id) }.getOrNull() ?: return null
        val seriesUuid = seriesId?.let { runCatching { UUID.fromString(it) }.getOrNull() }
        return when (type) {
            "MOVIE" -> Media.MovieMedia(movieId = uuid)
            "SERIES" -> Media.SeriesMedia(seriesId = uuid)
            "SEASON" -> Media.SeasonMedia(seasonId = uuid, seriesId = seriesUuid ?: return null)
            "EPISODE" -> Media.EpisodeMedia(episodeId = uuid, seriesId = seriesUuid ?: return null)
            else -> null
        }
    }

    suspend fun loadLibraries() {
        val librariesItem = runCatching { jellyfinApiClient.getLibraries() }
            .getOrElse { error ->
                Log.w(TAG, "Unable to load libraries", error)
                return
            }
        val filteredLibraries = librariesItem.filter {
            it.collectionType == CollectionType.MOVIES || it.collectionType == CollectionType.TVSHOWS
        }
        val emptyLibraries = filteredLibraries.map { it.toLibrary(serverUrl()) }
        librariesState.value = emptyLibraries

        val filledLibraries = emptyLibraries.map { loadLibrary(it) }
        librariesState.value = filledLibraries

        val movies = filledLibraries.filter { it.type == LibraryKind.MOVIES }.flatMap { it.movies.orEmpty() }
        onlineMediaRepository.upsertMovies(movies)

        val series = filledLibraries.filter { it.type == LibraryKind.SERIES }.flatMap { it.series.orEmpty() }
        onlineMediaRepository.upsertSeries(series)
    }

    suspend fun loadLibrary(library: Library): Library {
        val contentItem = runCatching { jellyfinApiClient.getLibraryContent(library.id) }
            .getOrElse { error ->
                Log.w(TAG, "Unable to load library ${library.id}", error)
                return library
            }
        return when (library.type) {
            LibraryKind.MOVIES -> library.copy(
                movies = contentItem.map { it.toMovie(serverUrl(), library.id) },
            )
            LibraryKind.SERIES -> library.copy(
                series = contentItem.map { it.toSeries(serverUrl(), library.id) },
            )
        }
    }

    suspend fun loadSuggestions() {
        val suggestionsItems = runCatching { jellyfinApiClient.getSuggestions() }
            .getOrElse { error ->
                Log.w(TAG, "Unable to load suggestions", error)
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

    suspend fun loadContinueWatching() {
        val continueWatchingItems = runCatching { jellyfinApiClient.getContinueWatching() }
            .getOrElse { error ->
                Log.w(TAG, "Unable to load continue watching", error)
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

    suspend fun loadNextUp() {
        val nextUpItems = runCatching { jellyfinApiClient.getNextUpEpisodes() }
            .getOrElse { error ->
                Log.w(TAG, "Unable to load next up", error)
                return
            }
        nextUpState.value = nextUpItems.map { item ->
            Media.EpisodeMedia(episodeId = item.id, seriesId = item.seriesId!!)
        }

        nextUpItems.forEach { item ->
            onlineMediaRepository.upsertEpisodes(listOf(item.toEpisode(serverUrl())))
        }
    }

    suspend fun loadLatestLibraryContent() {
        val librariesItem = runCatching { jellyfinApiClient.getLibraries() }
            .getOrElse { error ->
                Log.w(TAG, "Unable to load latest library content", error)
                return
            }
        val filteredLibraries = librariesItem.filter {
            it.collectionType == CollectionType.MOVIES || it.collectionType == CollectionType.TVSHOWS
        }
        val latestLibraryContents = filteredLibraries.associate { library ->
            val latestFromLibrary = runCatching { jellyfinApiClient.getLatestFromLibrary(library.id) }
                .getOrElse { error ->
                    Log.w(TAG, "Unable to load latest items for library ${library.id}", error)
                    emptyList()
                }
            library.id to when (library.collectionType) {
                CollectionType.MOVIES -> latestFromLibrary.map {
                    val movie = it.toMovie(serverUrl(), library.id)
                    Media.MovieMedia(movieId = movie.id)
                }
                CollectionType.TVSHOWS -> latestFromLibrary.map {
                    when (it.type) {
                        BaseItemKind.SERIES -> {
                            val series = it.toSeries(serverUrl(), library.id)
                            Media.SeriesMedia(seriesId = series.id)
                        }
                        BaseItemKind.SEASON -> {
                            val season = it.toSeason()
                            Media.SeasonMedia(seasonId = season.id, seriesId = season.seriesId)
                        }
                        BaseItemKind.EPISODE -> {
                            val episode = it.toEpisode(serverUrl())
                            Media.EpisodeMedia(episodeId = episode.id, seriesId = episode.seriesId)
                        }
                        else -> throw UnsupportedOperationException("Unsupported item type: ${it.type}")
                    }
                }
                else -> throw UnsupportedOperationException("Unsupported library type: ${library.collectionType}")
            }
        }
        latestLibraryContentState.value = latestLibraryContents
    }

    private suspend fun serverUrl(): String {
        return userSessionRepository.serverUrl.first()
    }

    private fun BaseItemDto.toLibrary(serverUrl: String): Library {
        return when (collectionType) {
            CollectionType.MOVIES -> Library(
                id = id,
                name = name!!,
                posterUrl = ImageUrlBuilder.toImageUrl(url = serverUrl, itemId = id, artworkKind = ArtworkKind.PRIMARY),
                type = LibraryKind.MOVIES,
                movies = emptyList(),
            )
            CollectionType.TVSHOWS -> Library(
                id = id,
                name = name!!,
                posterUrl = ImageUrlBuilder.toImageUrl(url = serverUrl, itemId = id, artworkKind = ArtworkKind.PRIMARY),
                type = LibraryKind.SERIES,
                series = emptyList(),
            )
            else -> throw UnsupportedOperationException("Unsupported library type: $collectionType")
        }
    }

    private fun BaseItemDto.toMovie(serverUrl: String, libraryId: UUID): Movie {
        return Movie(
            id = id,
            libraryId = libraryId,
            title = name ?: "Unknown title",
            progress = userData!!.playedPercentage,
            watched = userData!!.played,
            year = productionYear?.toString() ?: premiereDate?.year?.toString().orEmpty(),
            rating = officialRating ?: "NR",
            runtime = formatRuntime(runTimeTicks),
            synopsis = overview ?: "No synopsis available",
            format = container?.uppercase() ?: "VIDEO",
            imageUrlPrefix = ImageUrlBuilder.toPrefixImageUrl(url = serverUrl, itemId = id),
            subtitles = "ENG",
            audioTrack = "ENG",
            cast = emptyList(),
        )
    }

    private fun BaseItemDto.toSeries(serverUrl: String, libraryId: UUID): Series {
        return Series(
            id = id,
            libraryId = libraryId,
            name = name ?: "Unknown",
            synopsis = overview ?: "No synopsis available",
            year = productionYear?.toString() ?: premiereDate?.year?.toString().orEmpty(),
            imageUrlPrefix = ImageUrlBuilder.toPrefixImageUrl(url = serverUrl, itemId = id),
            unwatchedEpisodeCount = userData!!.unplayedItemCount!!,
            seasonCount = childCount!!,
            seasons = emptyList(),
            cast = emptyList(),
        )
    }

    private fun BaseItemDto.toSeason(): Season {
        return Season(
            id = id,
            seriesId = seriesId!!,
            name = name ?: "Unknown",
            index = indexNumber ?: 0,
            unwatchedEpisodeCount = userData!!.unplayedItemCount!!,
            episodeCount = childCount!!,
            episodes = emptyList(),
        )
    }

    private fun BaseItemDto.toEpisode(serverUrl: String): Episode {
        val releaseDate = formatReleaseDate(premiereDate, productionYear)
        val imageUrlPrefix = id?.let { itemId ->
            ImageUrlBuilder.toPrefixImageUrl(url = serverUrl, itemId = itemId)
        } ?: ""
        return Episode(
            id = id,
            seriesId = seriesId!!,
            seasonId = parentId!!,
            title = name ?: "Unknown title",
            index = indexNumber!!,
            releaseDate = releaseDate,
            rating = officialRating ?: "NR",
            runtime = formatRuntime(runTimeTicks),
            progress = userData!!.playedPercentage,
            watched = userData!!.played,
            format = container?.uppercase() ?: "VIDEO",
            synopsis = overview ?: "No synopsis available.",
            imageUrlPrefix = imageUrlPrefix,
            cast = emptyList(),
        )
    }

    private fun formatReleaseDate(date: LocalDateTime?, fallbackYear: Int?): String {
        if (date == null) {
            return fallbackYear?.toString() ?: "—"
        }
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
        return date.toLocalDate().format(formatter)
    }

    private fun formatRuntime(ticks: Long?): String {
        if (ticks == null || ticks <= 0) return "—"
        val totalSeconds = ticks / 10_000_000
        val hours = TimeUnit.SECONDS.toHours(totalSeconds)
        val minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) % 60
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }

    companion object {
        private const val TAG = "InMemoryAppContentRepo"
    }
}
