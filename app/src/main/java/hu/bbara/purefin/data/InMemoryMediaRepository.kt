package hu.bbara.purefin.data

import androidx.datastore.core.DataStore
import hu.bbara.purefin.client.JellyfinApiClient
import hu.bbara.purefin.data.cache.CachedMediaItem
import hu.bbara.purefin.data.cache.HomeCache
import hu.bbara.purefin.data.local.room.OfflineDatabase
import hu.bbara.purefin.data.local.room.OfflineRoomMediaLocalDataSource
import hu.bbara.purefin.data.local.room.RoomMediaLocalDataSource
import hu.bbara.purefin.data.model.Episode
import hu.bbara.purefin.data.model.Library
import hu.bbara.purefin.data.model.Media
import hu.bbara.purefin.data.model.Movie
import hu.bbara.purefin.data.model.Season
import hu.bbara.purefin.data.model.Series
import hu.bbara.purefin.image.JellyfinImageHelper
import hu.bbara.purefin.session.UserSessionRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.CollectionType
import org.jellyfin.sdk.model.api.ImageType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryMediaRepository @Inject constructor(
    val userSessionRepository: UserSessionRepository,
    val jellyfinApiClient: JellyfinApiClient,
    private val localDataSource: RoomMediaLocalDataSource,
    @OfflineDatabase private val offlineDataSource: OfflineRoomMediaLocalDataSource,
    private val homeCacheDataStore: DataStore<HomeCache>
) : MediaRepository {

    private val ready = CompletableDeferred<Unit>()
    private val readyMutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var initialLoadTimestamp = 0L

    private val _state: MutableStateFlow<MediaRepositoryState> = MutableStateFlow(MediaRepositoryState.Loading)
    override val state: StateFlow<MediaRepositoryState> = _state.asStateFlow()

    override val libraries: StateFlow<List<Library>> = localDataSource.librariesFlow
        .stateIn(scope, SharingStarted.Eagerly, emptyList())
    override val movies: StateFlow<Map<UUID, Movie>> = localDataSource.moviesFlow
        .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    override val series: StateFlow<Map<UUID, Series>> = localDataSource.seriesFlow
        .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    override val episodes: StateFlow<Map<UUID, Episode>> = localDataSource.episodesFlow
        .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    override fun observeSeriesWithContent(seriesId: UUID): Flow<Series?> {
        scope.launch {
            awaitReady()
            ensureSeriesContentLoaded(seriesId)
        }
        return localDataSource.observeSeriesWithContent(seriesId)
    }

    private val _continueWatching: MutableStateFlow<List<Media>> = MutableStateFlow(emptyList())
    override val continueWatching: StateFlow<List<Media>> = _continueWatching.asStateFlow()

    private val _latestLibraryContent: MutableStateFlow<Map<UUID, List<Media>>> = MutableStateFlow(emptyMap())
    override val latestLibraryContent: StateFlow<Map<UUID, List<Media>>> = _latestLibraryContent.asStateFlow()

    init {
        scope.launch {
            loadFromCache()
            runCatching { ensureReady() }
        }
    }

    private suspend fun loadFromCache() {
        val cache = homeCacheDataStore.data.first()
        if (cache.continueWatching.isNotEmpty()) {
            _continueWatching.value = cache.continueWatching.mapNotNull { it.toMedia() }
        }
        if (cache.latestLibraryContent.isNotEmpty()) {
            _latestLibraryContent.value = cache.latestLibraryContent.mapNotNull { (key, items) ->
                val uuid = runCatching { UUID.fromString(key) }.getOrNull() ?: return@mapNotNull null
                uuid to items.mapNotNull { it.toMedia() }
            }.toMap()
        }
    }

    private suspend fun persistHomeCache() {
        val cache = HomeCache(
            continueWatching = _continueWatching.value.map { it.toCachedItem() },
            latestLibraryContent = _latestLibraryContent.value.map { (uuid, items) ->
                uuid.toString() to items.map { it.toCachedItem() }
            }.toMap()
        )
        homeCacheDataStore.updateData { cache }
    }

    private fun Media.toCachedItem(): CachedMediaItem = when (this) {
        is Media.MovieMedia -> CachedMediaItem(type = "MOVIE", id = movieId.toString())
        is Media.SeriesMedia -> CachedMediaItem(type = "SERIES", id = seriesId.toString())
        is Media.SeasonMedia -> CachedMediaItem(type = "SEASON", id = seasonId.toString(), mediaId = seriesId.toString())
        is Media.EpisodeMedia -> CachedMediaItem(type = "EPISODE", id = episodeId.toString(), mediaId = seriesId.toString())
    }

    private fun CachedMediaItem.toMedia(): Media? {
        val uuid = runCatching { UUID.fromString(id) }.getOrNull() ?: return null
        val seriesUuid = mediaId?.let { runCatching { UUID.fromString(it) }.getOrNull() }
        return when (type) {
            "MOVIE" -> Media.MovieMedia(movieId = uuid)
            "SERIES" -> Media.SeriesMedia(seriesId = uuid)
            "SEASON" -> Media.SeasonMedia(seasonId = uuid, seriesId = seriesUuid ?: return null)
            "EPISODE" -> Media.EpisodeMedia(episodeId = uuid, seriesId = seriesUuid ?: return null)
            else -> null
        }
    }

    override suspend fun ensureReady() {
        if (ready.isCompleted) {
            ready.await() // rethrows if completed exceptionally
            return
        }

        // Only the first caller runs the loading logic; others wait on the deferred.
        if (readyMutex.tryLock()) {
            try {
                if (ready.isCompleted) {
                    ready.await()
                    return
                }
                loadLibraries()
                loadContinueWatching()
                loadLatestLibraryContent()
                persistHomeCache()
                _state.value = MediaRepositoryState.Ready
                initialLoadTimestamp = System.currentTimeMillis()
                ready.complete(Unit)
            } catch (t: Throwable) {
                _state.value = MediaRepositoryState.Error(t)
                ready.completeExceptionally(t)
                throw t
            } finally {
                readyMutex.unlock()
            }
        } else {
            ready.await()
        }
    }

    private suspend fun awaitReady() {
        ready.await()
    }

    suspend fun loadLibraries() {
        val librariesItem = jellyfinApiClient.getLibraries()
        //TODO add support for playlists
        val filteredLibraries =
            librariesItem.filter { it.collectionType == CollectionType.MOVIES || it.collectionType == CollectionType.TVSHOWS }
        val emptyLibraries = filteredLibraries.map {
            it.toLibrary()
        }
        localDataSource.saveLibraries(emptyLibraries)
        offlineDataSource.saveLibraries(emptyLibraries)

        val filledLibraries = emptyLibraries.map { library ->
            return@map loadLibrary(library)
        }
        val movies = filledLibraries.filter { it.type == CollectionType.MOVIES }.flatMap { it.movies.orEmpty() }
        localDataSource.saveMovies(movies)

        val series = filledLibraries.filter { it.type == CollectionType.TVSHOWS }.flatMap { it.series.orEmpty() }
        localDataSource.saveSeries(series)
    }

    suspend fun loadLibrary(library: Library): Library {
        val contentItem = jellyfinApiClient.getLibraryContent(library.id)
        when (library.type) {
            CollectionType.MOVIES -> {
                val movies = contentItem.map { it.toMovie(serverUrl(), library.id) }
                return library.copy(movies = movies)
            }
            CollectionType.TVSHOWS -> {
                val series = contentItem.map { it.toSeries(serverUrl(), library.id) }
                return library.copy(series = series)
            }
            else -> throw UnsupportedOperationException("Unsupported library type: ${library.type}")
        }
    }

    suspend fun loadMovie(movie: Movie) : Movie {
        val movieItem = jellyfinApiClient.getItemInfo(movie.id)
            ?: throw RuntimeException("Movie not found")
        val updatedMovie = movieItem.toMovie(serverUrl(), movie.libraryId)
        localDataSource.saveMovies(listOf(updatedMovie))
        return updatedMovie
    }

    suspend fun loadSeries(series: Series) : Series {
        val seriesItem = jellyfinApiClient.getItemInfo(series.id)
            ?: throw RuntimeException("Series not found")
        val updatedSeries = seriesItem.toSeries(serverUrl(), series.libraryId)
        localDataSource.saveSeries(listOf(updatedSeries))
        return updatedSeries
    }

    suspend fun loadContinueWatching() {
        val continueWatchingItems = jellyfinApiClient.getNextUpEpisodes()
        val items = continueWatchingItems.mapNotNull { item ->
            when (item.type) {
                BaseItemKind.MOVIE -> Media.MovieMedia(movieId = item.id)
                BaseItemKind.EPISODE -> Media.EpisodeMedia(
                    episodeId = item.id,
                    seriesId = item.seriesId!!
                )
                else -> throw UnsupportedOperationException("Unsupported item type: ${item.type}")
            }
        }
        _continueWatching.value = items

        //Load episodes, Movies are already loaded at this point.
        continueWatchingItems.forEach { item ->
            when (item.type) {
                BaseItemKind.EPISODE -> {
                    val episode = item.toEpisode(serverUrl())
                    localDataSource.saveEpisode(episode)
                }
                else -> { /* Do nothing */ }
            }
        }
    }

    suspend fun loadLatestLibraryContent() {
        // TODO Make libraries accessible in a field or something that is not this ugly.
        val librariesItem = jellyfinApiClient.getLibraries()
        val filterLibraries =
            librariesItem.filter { it.collectionType == CollectionType.MOVIES || it.collectionType == CollectionType.TVSHOWS }
        val latestLibraryContents = filterLibraries.associate { library ->
            val latestFromLibrary = jellyfinApiClient.getLatestFromLibrary(library.id)
            library.id to when (library.collectionType) {
                CollectionType.MOVIES -> {
                    latestFromLibrary.map {
                        val movie = it.toMovie(serverUrl(), library.id)
                        Media.MovieMedia(movieId = movie.id)
                    }
                }
                CollectionType.TVSHOWS -> {
                    latestFromLibrary.map {
                        when (it.type) {
                            BaseItemKind.SERIES -> {
                                val series = it.toSeries(serverUrl(), library.id)
                                Media.SeriesMedia(seriesId = series.id)
                            }
                            BaseItemKind.SEASON -> {
                                val season = it.toSeason(serverUrl())
                                Media.SeasonMedia(seasonId = season.id, seriesId = season.seriesId)
                            }
                            BaseItemKind.EPISODE -> {
                                val episode = it.toEpisode(serverUrl())
                                Media.EpisodeMedia(episodeId = episode.id, seriesId = episode.seriesId)
                            } else -> throw UnsupportedOperationException("Unsupported item type: ${it.type}")
                        }
                    }
                }
                else -> throw UnsupportedOperationException("Unsupported library type: ${library.collectionType}")
            }
        }
        _latestLibraryContent.value = latestLibraryContents

        //TODO Load seasons and episodes, other types are already loaded at this point.
    }

    private suspend fun ensureSeriesContentLoaded(seriesId: UUID) {
        awaitReady()
        // Skip if content is already cached in Room
        localDataSource.getSeriesWithContent(seriesId)?.takeIf { it.seasons.isNotEmpty() }?.let {
            return
        }

        val series = this.series.value[seriesId] ?: throw RuntimeException("Series not found")

        val emptySeasonsItem = jellyfinApiClient.getSeasons(seriesId)
        val emptySeasons = emptySeasonsItem.map { it.toSeason(serverUrl()) }
        val filledSeasons = emptySeasons.map { season ->
            val episodesItem = jellyfinApiClient.getEpisodesInSeason(seriesId, season.id)
            val episodes = episodesItem.map { it.toEpisode(serverUrl()) }
            season.copy(episodes = episodes)
        }
        val updatedSeries = series.copy(seasons = filledSeasons)
        localDataSource.saveSeries(listOf(updatedSeries))
        localDataSource.saveSeriesContent(updatedSeries)
    }

    override suspend fun updateWatchProgress(mediaId: UUID, positionMs: Long, durationMs: Long) {
        if (durationMs <= 0) return
        val progressPercent = (positionMs.toDouble() / durationMs.toDouble()) * 100.0
        val watched = progressPercent >= 90.0
        // Write to Room — the reactive Flows propagate changes to UI automatically
        localDataSource.updateWatchProgress(mediaId, progressPercent, watched)
    }

    companion object {
        private const val REFRESH_MIN_INTERVAL_MS = 30_000L
    }

    override suspend fun refreshHomeData() {
        awaitReady()
        // Skip refresh if the initial load (or last refresh) just happened
        val elapsed = System.currentTimeMillis() - initialLoadTimestamp
        if (elapsed < REFRESH_MIN_INTERVAL_MS) return

        loadLibraries()
        loadContinueWatching()
        loadLatestLibraryContent()
        persistHomeCache()
        initialLoadTimestamp = System.currentTimeMillis()
    }

    private suspend fun serverUrl(): String {
        return userSessionRepository.serverUrl.first()
    }

    private fun BaseItemDto.toLibrary(): Library {
        return when (this.collectionType) {
            CollectionType.MOVIES -> Library(
                id = this.id,
                name = this.name!!,
                type = CollectionType.MOVIES,
                movies = emptyList()
            )
            CollectionType.TVSHOWS -> Library(
                id = this.id,
                name = this.name!!,
                type = CollectionType.TVSHOWS,
                series = emptyList()
            )
            else -> throw UnsupportedOperationException("Unsupported library type: ${this.collectionType}")
        }
    }

    private fun BaseItemDto.toMovie(serverUrl: String, libraryId: UUID) : Movie {
        return Movie(
            id = this.id,
            libraryId = libraryId,
            title = this.name ?: "Unknown title",
            progress = this.userData!!.playedPercentage,
            watched = this.userData!!.played,
            year = this.productionYear?.toString() ?: premiereDate?.year?.toString().orEmpty(),
            rating = this.officialRating
                ?: "NR",
            runtime = formatRuntime(this.runTimeTicks),
            synopsis = this.overview ?: "No synopsis available",
            format = container?.uppercase() ?: "VIDEO",
            heroImageUrl = JellyfinImageHelper.toImageUrl(
                url = serverUrl,
                itemId = this.id,
                type = ImageType.PRIMARY
            ),
            subtitles = "ENG",
            audioTrack = "ENG",
            cast = emptyList()
        )
    }

    private fun BaseItemDto.toSeries(serverUrl: String, libraryId: UUID): Series {
        return Series(
            id = this.id,
            libraryId = libraryId,
            name = this.name ?: "Unknown",
            synopsis = this.overview ?: "No synopsis available",
            year = this.productionYear?.toString()
                ?: this.premiereDate?.year?.toString().orEmpty(),
            heroImageUrl = JellyfinImageHelper.toImageUrl(
                url = serverUrl,
                itemId = this.id,
                type = ImageType.PRIMARY
            ),
            unwatchedEpisodeCount = this.userData!!.unplayedItemCount!!,
            seasonCount = this.childCount!!,
            seasons = emptyList(),
            cast = emptyList()
        )
    }

    private fun BaseItemDto.toSeason(serverUrl: String): Season {
        return Season(
            id = this.id,
            seriesId = this.seriesId!!,
            name = this.name ?: "Unknown",
            index = this.indexNumber ?: 0,
            unwatchedEpisodeCount = this.userData!!.unplayedItemCount!!,
            episodeCount = this.childCount!!,
            episodes = emptyList()
        )
    }

    private fun BaseItemDto.toEpisode(serverUrl: String): Episode {
        val releaseDate = formatReleaseDate(premiereDate, productionYear)
        val rating = officialRating ?: "NR"
        val runtime = formatRuntime(runTimeTicks)
        val format = container?.uppercase() ?: "VIDEO"
        val synopsis = overview ?: "No synopsis available."
        val heroImageUrl = id?.let { itemId ->
            JellyfinImageHelper.toImageUrl(
                url = serverUrl,
                itemId = itemId,
                type = ImageType.PRIMARY
            )
        } ?: ""
        return Episode(
            id = id,
            seriesId = seriesId!!,
            seasonId = parentId!!,
            title = name ?: "Unknown title",
            index = indexNumber!!,
            releaseDate = releaseDate,
            rating = rating,
            runtime = runtime,
            progress = userData!!.playedPercentage,
            watched = userData!!.played,
            format = format,
            synopsis = synopsis,
            heroImageUrl = heroImageUrl,
            cast = emptyList()
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
        return if (hours > 0) {
            "${hours}h ${minutes}m"
        } else {
            "${minutes}m"
        }
    }
}
