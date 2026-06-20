package hu.bbara.purefin.data.catalog

import hu.bbara.purefin.core.data.LocalMediaRepository
import hu.bbara.purefin.core.data.UserSessionRepository
import hu.bbara.purefin.data.converter.toEpisode
import hu.bbara.purefin.data.converter.toMovie
import hu.bbara.purefin.data.converter.toSeason
import hu.bbara.purefin.data.converter.toSeries
import hu.bbara.purefin.data.jellyfin.client.JellyfinApiClient
import hu.bbara.purefin.model.Episode
import hu.bbara.purefin.model.Movie
import hu.bbara.purefin.model.Series
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.jellyfin.sdk.model.api.BaseItemKind
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryLocalMediaRepository @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val jellyfinApiClient: JellyfinApiClient,
) : LocalMediaRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val serverUrl = userSessionRepository.serverUrl

    private val moviesState = MutableStateFlow<Map<UUID, Movie>>(emptyMap())
    override val movies: StateFlow<Map<UUID, Movie>> = moviesState.asStateFlow()

    private val seriesState = MutableStateFlow<Map<UUID, Series>>(emptyMap())
    override val series: StateFlow<Map<UUID, Series>> = seriesState.asStateFlow()

    private val episodesState = MutableStateFlow<Map<UUID, Episode>>(emptyMap())
    override val episodes: StateFlow<Map<UUID, Episode>> = episodesState.asStateFlow()

    override suspend fun getMovie(id: UUID): Flow<Movie?> =
        moviesState.map { it[id] }.distinctUntilChanged()

    override suspend fun getSeries(id: UUID): Flow<Series?> =
        seriesState.map { it[id] }.distinctUntilChanged()

    override suspend fun getEpisode(id: UUID): Flow<Episode?> =
        episodesState.map { it[id] }.distinctUntilChanged()

    override suspend fun loadMovie(id: UUID) {
        if (moviesState.value.containsKey(id)) return
        try {
            jellyfinApiClient.getItemInfo(id)?.let { item ->
                if (item.type != BaseItemKind.MOVIE) {
                    Timber.tag(TAG).d("Item is not an movie: ${item.type}")
                    return
                }
                val movie = item.toMovie(serverUrl.first())
                moviesState.update { current -> current + (movie.id to movie) }
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Timber.tag(TAG).e(error, "Failed to load movie $id")
            throw error
        }
    }

    override suspend fun loadSeries(id: UUID) {
        if (seriesState.value.containsKey(id)) return
        try {
            jellyfinApiClient.getItemInfo(id)?.let { item ->
                if (item.type != BaseItemKind.SERIES) {
                    Timber.tag(TAG).d("Item is not an series: ${item.type}")
                    return
                }
                val series = item.toSeries(serverUrl.first())
                seriesState.update { current -> current + (series.id to series) }
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Timber.tag(TAG).e(error, "Failed to load series $id")
            throw error
        }
    }

    override suspend fun loadEpisode(id: UUID) {
        if (episodesState.value.containsKey(id)) return
        try {
            jellyfinApiClient.getItemInfo(id)?.let { item ->
                if (item.type != BaseItemKind.EPISODE) {
                    Timber.tag(TAG).d("Item is not an episode: ${item.type}")
                    return
                }
                val episode = item.toEpisode(serverUrl.first())
                episodesState.update { current -> current + (episode.id to episode) }
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Timber.tag(TAG).e(error, "Failed to load episode $id")
            throw error
        }
    }

    fun upsertMovies(movies: List<Movie>) {
        moviesState.update { current -> current + movies.associateBy { it.id } }
    }

    fun upsertSeries(series: List<Series>) {
        seriesState.update { current -> current + series.associateBy { it.id } }
    }

    fun upsertEpisodes(episodes: List<Episode>) {
        episodesState.update { current -> current + episodes.associateBy { it.id } }
    }

    override suspend fun loadSeasons(seriesId: UUID) {
        try {
            loadSeasonsInternal(seriesId)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Timber.tag(TAG).e(error, "Failed to load content for series $seriesId")
            throw error
        }
    }

    private suspend fun loadSeasonsInternal(seriesId: UUID) {
        seriesState.value[seriesId]?.takeIf { it.seasons.isNotEmpty() }?.let { return }

        val seasons = jellyfinApiClient.getSeasons(seriesId).map { it.toSeason() }

        // Await the parallel loadSeries to populate the series entry.
        // Precondition: seriesId refers to a real series (selectSeries always uses a
        // SeriesDto from a real series list). If loadSeries returns without populating
        // state (null item / wrong type) this suspends until cancelled by structured
        // concurrency when a sibling load* call fails.
        seriesState.first { it.containsKey(seriesId) }

        seriesState.update { current ->
            val existing = current[seriesId] ?: return@update current
            if (existing.seasons.isNotEmpty()) return@update current
            current + (seriesId to existing.copy(seasons = seasons))
        }
    }

    override suspend fun loadSeasonEpisodes(seriesId: UUID, seasonId: UUID) {
        // Fast path: season already cached with episodes or known empty — skip the fetch.
        seriesState.value[seriesId]?.seasons?.firstOrNull { it.id == seasonId }?.let { cached ->
            if (cached.episodes.isNotEmpty() || cached.episodeCount == 0) return
        }

        // Fetch first so this runs concurrently with loadSeries/loadSeasons when launched
        // from selectSeries. Precondition: seasons are pre-loaded by selectSeries and
        // seasonId is always a valid season from a real EpisodeDto in this path.
        val serverUrl = userSessionRepository.serverUrl.first()
        val episodes = jellyfinApiClient.getEpisodesInSeason(seriesId, seasonId)
            .map { it.toEpisode(serverUrl) }

        // Await the season to be present in state (immediate when pre-loaded; waits for
        // the parallel loadSeasons otherwise).
        seriesState.first { it[seriesId]?.seasons?.any { it.id == seasonId } == true }

        // Re-check guard after await: a concurrent call may have filled episodes already.
        val series = seriesState.value[seriesId] ?: return
        val season = series.seasons.firstOrNull { it.id == seasonId } ?: return
        if (season.episodes.isNotEmpty() || season.episodeCount == 0) {
            return
        }

        seriesState.update { current ->
            val currentSeries = current[seriesId] ?: return@update current
            val updatedSeries = currentSeries.copy(
                seasons = currentSeries.seasons.map {
                    if (it.id == seasonId && it.episodes.isEmpty()) {
                        it.copy(episodes = episodes)
                    } else {
                        it
                    }
                }
            )
            current + (updatedSeries.id to updatedSeries)
        }
        episodesState.update { current -> current + episodes.associateBy { it.id } }
    }

    override suspend fun updateWatchProgress(mediaId: UUID, positionMs: Long, durationMs: Long) {
        if (durationMs <= 0) return
        val progressPercent = (positionMs.toDouble() / durationMs.toDouble()) * 100.0
        updateWatchProgressPercent(mediaId, progressPercent)
    }

    override suspend fun updateWatchProgressPercent(mediaId: UUID, progressPercent: Double) {
        if (progressPercent.isNaN()) return
        val normalizedProgressPercent = progressPercent.coerceIn(0.0, 100.0)
        val watched = normalizedProgressPercent >= 90.0

        if (moviesState.value.containsKey(mediaId)) {
            moviesState.update { current ->
                val movie = current[mediaId] ?: return@update current
                current + (mediaId to movie.copy(progress = normalizedProgressPercent, watched = watched))
            }
            return
        }
        if (episodesState.value.containsKey(mediaId)) {
            var updatedEpisode: Episode? = null
            episodesState.update { current ->
                val episode = current[mediaId] ?: return@update current
                val updated = episode.copy(progress = normalizedProgressPercent, watched = watched)
                updatedEpisode = updated
                current + (mediaId to updated)
            }
            updatedEpisode?.let(::updateLoadedSeriesEpisode)
        }
    }

    override suspend fun markAsWatched(mediaId: UUID, watched: Boolean) {
        if (moviesState.value.containsKey(mediaId)) {
            moviesState.update { current ->
                val movie = current[mediaId] ?: return@update current
                current + (mediaId to movie.copy(watched = watched))
            }
            return
        }
        if (episodesState.value.containsKey(mediaId)) {
            var updatedEpisode: Episode? = null
            episodesState.update { current ->
                val episode = current[mediaId] ?: return@update current
                val updated = episode.copy(watched = watched)
                updatedEpisode = updated
                current + (mediaId to updated)
            }
            updatedEpisode?.let(::updateLoadedSeriesEpisode)
        }
    }

    override suspend fun updatePlaybackPosition(
        mediaId: UUID,
        playbackPositionTicks: Long,
        runtimeTicks: Long,
    ) {
        // Server-side operation — not persisted locally
    }

    private fun updateLoadedSeriesEpisode(updatedEpisode: Episode) {
        seriesState.update { current ->
            val series = current[updatedEpisode.seriesId] ?: return@update current
            var changed = false
            val seasons = series.seasons.map { season ->
                if (season.episodes.none { it.id == updatedEpisode.id }) {
                    season
                } else {
                    changed = true
                    season.copy(
                        episodes = season.episodes.map { episode ->
                            if (episode.id == updatedEpisode.id) updatedEpisode else episode
                        }
                    )
                }
            }
            if (changed) {
                current + (series.id to series.copy(seasons = seasons))
            } else {
                current
            }
        }
    }

    private companion object {
        const val TAG = "InMemoryMediaRepository"
    }
}
