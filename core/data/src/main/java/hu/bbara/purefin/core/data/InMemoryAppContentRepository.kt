package hu.bbara.purefin.core.data

import android.util.Log
import androidx.datastore.core.DataStore
import hu.bbara.purefin.core.data.cache.CachedMediaItem
import hu.bbara.purefin.core.data.cache.HomeCache
import hu.bbara.purefin.core.data.client.JellyfinApiClient
import hu.bbara.purefin.core.data.image.JellyfinImageHelper
import hu.bbara.purefin.core.data.session.UserSessionRepository
import hu.bbara.purefin.core.model.Episode
import hu.bbara.purefin.core.model.Library
import hu.bbara.purefin.core.model.Media
import hu.bbara.purefin.core.model.Movie
import hu.bbara.purefin.core.model.Season
import hu.bbara.purefin.core.model.Series
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
class InMemoryAppContentRepository @Inject constructor(
    val userSessionRepository: UserSessionRepository,
    val jellyfinApiClient: JellyfinApiClient,
    private val homeCacheDataStore: DataStore<HomeCache>,
    private val mediaRepository: CompositeMediaRepository,
    private val networkMonitor: NetworkMonitor,
) : AppContentRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var loadJob: Job? = null

    private val contentRepositoryReady : MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val ready: StateFlow<Boolean> = combine(contentRepositoryReady, mediaRepository.ready) {
        contentRepositoryReady, mediaRepositoryReady ->
        contentRepositoryReady && mediaRepositoryReady
    }.stateIn(scope, SharingStarted.Eagerly, false)

    private val _libraries: MutableStateFlow<List<Library>> = MutableStateFlow(emptyList())

    override val libraries: StateFlow<List<Library>> = _libraries.asStateFlow()
    private val _suggestions: MutableStateFlow<List<Media>> = MutableStateFlow(emptyList())

    override val suggestions: StateFlow<List<Media>> = _suggestions.asStateFlow()
    private val _continueWatching: MutableStateFlow<List<Media>> = MutableStateFlow(emptyList())

    override val continueWatching: StateFlow<List<Media>> = _continueWatching.asStateFlow()
    private val _nextUp: MutableStateFlow<List<Media>> = MutableStateFlow(emptyList())

    override val nextUp: StateFlow<List<Media>> = _nextUp.asStateFlow()
    private val _latestLibraryContent: MutableStateFlow<Map<UUID, List<Media>>> = MutableStateFlow(emptyMap())
    override val latestLibraryContent: StateFlow<Map<UUID, List<Media>>> = _latestLibraryContent.asStateFlow()

    override val movies: StateFlow<Map<UUID, Movie>> = mediaRepository.movies
    override val series: StateFlow<Map<UUID, Series>> = mediaRepository.series
    override val episodes: StateFlow<Map<UUID, Episode>> = mediaRepository.episodes

    override fun observeSeriesWithContent(seriesId: UUID): Flow<Series?> {
        return mediaRepository.observeSeriesWithContent(seriesId)
    }

    override suspend fun updateWatchProgress(
        mediaId: UUID,
        positionMs: Long,
        durationMs: Long
    ) {
        mediaRepository.updateWatchProgress(mediaId, positionMs, durationMs)
    }

    init {
        scope.launch {
            loadFromCache()
            ensureReady()
        }
    }

    private suspend fun loadFromCache() {
        val cache = homeCacheDataStore.data.first()
        if (cache.suggestions.isNotEmpty()) {
            _suggestions.value = cache.suggestions.mapNotNull { it.toMedia() }
        }
        if (cache.continueWatching.isNotEmpty()) {
            _continueWatching.value = cache.continueWatching.mapNotNull { it.toMedia() }
        }
        if (cache.nextUp.isNotEmpty()) {
            _nextUp.value = cache.nextUp.mapNotNull { it.toMedia() }
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
            suggestions = _suggestions.value.map { it.toCachedItem() },
            continueWatching = _continueWatching.value.map { it.toCachedItem() },
            nextUp = _nextUp.value.map { it.toCachedItem() },
            latestLibraryContent = _latestLibraryContent.value.map { (uuid, items) ->
                uuid.toString() to items.map { it.toCachedItem() }
            }.toMap()
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

    override suspend fun ensureReady() {
        // check for combined ready state
        if (ready.value) {
            return
        }
        if (loadJob?.isActive == true) {
            return
        }
        if (!contentRepositoryReady.value) {
            return
        }
        contentRepositoryReady.value = true
        loadJob?.cancel()
        loadJob = scope.launch {
            loadSuggestions()
            loadContinueWatching()
            loadNextUp()
            loadLatestLibraryContent()
            loadLibraries()
            mediaRepository.setReady()
            persistHomeCache()
        }
    }

    suspend fun loadLibraries() {
        val librariesItem = runCatching { jellyfinApiClient.getLibraries() }
            .getOrElse { error ->
                Log.w(TAG, "Unable to load libraries", error)
                return
            }
        //TODO add support for playlists
        val filteredLibraries =
            librariesItem.filter { it.collectionType == CollectionType.MOVIES || it.collectionType == CollectionType.TVSHOWS }
        val emptyLibraries = filteredLibraries.map { it.toLibrary(serverUrl()) }
        _libraries.value = emptyLibraries

        val filledLibraries = emptyLibraries.map { library ->
            return@map loadLibrary(library)
        }
        _libraries.value = filledLibraries

        val movies = filledLibraries.filter { it.type == CollectionType.MOVIES }.flatMap { it.movies.orEmpty() }
        mediaRepository.upsertMovies(movies)

        val series = filledLibraries.filter { it.type == CollectionType.TVSHOWS }.flatMap { it.series.orEmpty() }
        mediaRepository.upsertSeries(series)
    }

    suspend fun loadLibrary(library: Library): Library {
        val contentItem = runCatching { jellyfinApiClient.getLibraryContent(library.id) }
            .getOrElse { error ->
                Log.w(TAG, "Unable to load library ${library.id}", error)
                return library
            }
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

    suspend fun loadMovie(movieId: UUID): Movie {
        val cachedMovie = mediaRepository.movies.value[movieId]
        val movieItem = runCatching { jellyfinApiClient.getItemInfo(movieId) }
            .getOrElse { error ->
                Log.w(TAG, "Unable to load movie $movieId", error)
                null
            }
            ?: return cachedMovie ?: throw RuntimeException("Movie not found")
        val updatedMovie = movieItem.toMovie(serverUrl(), movieItem.parentId!!)
        mediaRepository.upsertMovies(listOf(updatedMovie))
        return updatedMovie
    }

    suspend fun loadSeries(seriesId: UUID): Series {
        val cachedSeries = mediaRepository.series.value[seriesId]
        val seriesItem = runCatching { jellyfinApiClient.getItemInfo(seriesId) }
            .getOrElse { error ->
                Log.w(TAG, "Unable to load series $seriesId", error)
                null
            }
            ?: return cachedSeries ?: throw RuntimeException("Series not found")
        val updatedSeries = seriesItem.toSeries(serverUrl(), seriesItem.parentId!!)
        mediaRepository.upsertSeries(listOf(updatedSeries))
        return updatedSeries
    }

    suspend fun loadSuggestions() {
        val suggestionsItems = runCatching { jellyfinApiClient.getSuggestions() }
            .getOrElse { error ->
                Log.w(TAG, "Unable to load suggestions", error)
                return
            }
        val items = suggestionsItems.mapNotNull { item ->
            when (item.type) {
                BaseItemKind.MOVIE -> Media.MovieMedia(movieId = item.id)
                BaseItemKind.EPISODE -> Media.EpisodeMedia(
                    episodeId = item.id,
                    seriesId = item.seriesId!!
                )
                else -> throw UnsupportedOperationException("Unsupported item type: ${item.type}")
            }
        }
        _suggestions.value = items

        //Load episodes, Movies are already loaded at this point
        suggestionsItems.forEach { item ->
            when (item.type) {
                BaseItemKind.EPISODE -> {
                    val episode = item.toEpisode(serverUrl())
                    mediaRepository.upsertEpisodes(listOf(episode))
                }
                else -> { /* Do nothing */ }
            }
        }
    }

    suspend fun loadContinueWatching() {
        val continueWatchingItems = runCatching { jellyfinApiClient.getContinueWatching() }
            .getOrElse { error ->
                Log.w(TAG, "Unable to load continue watching", error)
                return
            }
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
                    mediaRepository.upsertEpisodes(listOf(episode))
                }
                else -> { /* Do nothing */ }
            }
        }
    }

    suspend fun loadNextUp() {
        val nextUpItems = runCatching { jellyfinApiClient.getNextUpEpisodes() }
            .getOrElse { error ->
                Log.w(TAG, "Unable to load next up", error)
                return
            }
        val items = nextUpItems.map { item ->
            Media.EpisodeMedia(
                episodeId = item.id,
                seriesId = item.seriesId!!
            )
        }
        _nextUp.value = items

        // Load episodes
        nextUpItems.forEach { item ->
            val episode = item.toEpisode(serverUrl())
            mediaRepository.upsertEpisodes(listOf(episode))
        }
    }

    suspend fun loadLatestLibraryContent() {
        // TODO Make libraries accessible in a field or something that is not this ugly.
        val librariesItem = runCatching { jellyfinApiClient.getLibraries() }
            .getOrElse { error ->
                Log.w(TAG, "Unable to load latest library content", error)
                return
            }
        val filterLibraries =
            librariesItem.filter { it.collectionType == CollectionType.MOVIES || it.collectionType == CollectionType.TVSHOWS }
        val latestLibraryContents = filterLibraries.associate { library ->
            val latestFromLibrary = runCatching { jellyfinApiClient.getLatestFromLibrary(library.id) }
                .getOrElse { error ->
                    Log.w(TAG, "Unable to load latest items for library ${library.id}", error)
                    emptyList()
                }
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

    override suspend fun refreshHomeData() {
        if(loadJob?.isActive == true) {
            loadJob?.join()
            return
        }
        val job = scope.launch {
            runCatching {
                val isOnline = networkMonitor.isOnline.first()
                if (!isOnline) return@runCatching
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
        loadJob = job
        job.join()
    }

    private suspend fun serverUrl(): String {
        return userSessionRepository.serverUrl.first()
    }

    private fun BaseItemDto.toLibrary(serverUrl: String): Library {
        return when (this.collectionType) {
            CollectionType.MOVIES -> Library(
                id = this.id,
                name = this.name!!,
                posterUrl = JellyfinImageHelper.toImageUrl(
                    url = serverUrl,
                    itemId = this.id,
                    type = ImageType.PRIMARY
                ),
                type = CollectionType.MOVIES,
                movies = emptyList()
            )
            CollectionType.TVSHOWS -> Library(
                id = this.id,
                name = this.name!!,
                posterUrl = JellyfinImageHelper.toImageUrl(
                    url = serverUrl,
                    itemId = this.id,
                    type = ImageType.PRIMARY
                ),
                type = CollectionType.TVSHOWS,
                series = emptyList()
            )
            else -> throw UnsupportedOperationException("Unsupported library type: ${this.collectionType}")
        }
    }

    private fun BaseItemDto.toMovie(serverUrl: String, libraryId: UUID): Movie {
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
            rating = officialRating ?: "NR",
            runtime = formatRuntime(runTimeTicks),
            progress = userData!!.playedPercentage,
            watched = userData!!.played,
            format = container?.uppercase() ?: "VIDEO",
            synopsis = overview ?: "No synopsis available.",
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

    companion object {
        private const val TAG = "InMemoryAppContentRepo"
    }
}
