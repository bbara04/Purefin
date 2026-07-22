package hu.bbara.purefin.data.catalog

import hu.bbara.purefin.core.concurrency.SingleFlight
import hu.bbara.purefin.core.data.LocalMediaRepository
import hu.bbara.purefin.core.data.UserSessionRepository
import hu.bbara.purefin.data.converter.isUncategorizedEpisode
import hu.bbara.purefin.data.converter.toEpisode
import hu.bbara.purefin.data.converter.toMovie
import hu.bbara.purefin.data.converter.toSeason
import hu.bbara.purefin.data.converter.toSeries
import hu.bbara.purefin.data.jellyfin.client.JellyfinApiClient
import hu.bbara.purefin.model.Episode
import hu.bbara.purefin.model.Movie
import hu.bbara.purefin.model.Series
import hu.bbara.purefin.model.UNCATEGORIZED_SEASON_ID
import kotlinx.coroutines.CancellationException
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
    private val singleFlight: SingleFlight,
) : LocalMediaRepository {

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

    override suspend fun loadMovie(id: UUID) = singleFlight.run("LocalMedia:loadMovie:$id") {
        try {
            jellyfinApiClient.getItemInfo(id)?.let { item ->
                if (item.type != BaseItemKind.MOVIE) {
                    Timber.tag(TAG).d("Item is not an movie: ${item.type}")
                    return@run
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

    override suspend fun loadSeries(id: UUID) = singleFlight.run("LocalMedia:loadSeries:$id") {
        try {
            jellyfinApiClient.getItemInfo(id)?.let { item ->
                if (item.type != BaseItemKind.SERIES) {
                    Timber.tag(TAG).d("Item is not an series: ${item.type}")
                    return@run
                }
                val series = item.toSeries(serverUrl.first())
                seriesState.update { current ->
                    val existing = current[series.id]
                    val merged = if (existing != null && existing.seasons.isNotEmpty() && series.seasons.isEmpty()) {
                        series.copy(
                            seasons = existing.seasons,
                            uncategorizedEpisodes = existing.uncategorizedEpisodes,
                        )
                    } else {
                        series
                    }
                    current + (series.id to merged)
                }
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Timber.tag(TAG).e(error, "Failed to load series $id")
            throw error
        }
    }

    override suspend fun loadEpisode(id: UUID) = singleFlight.run("LocalMedia:loadEpisode:$id") {
        try {
            jellyfinApiClient.getItemInfo(id)?.let { item ->
                if (item.type != BaseItemKind.EPISODE) {
                    Timber.tag(TAG).d("Item is not an episode: ${item.type}")
                    return@run
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
        seriesState.update { current ->
            current + series.associateBy { it.id }.mapValues { (id, newSeries) ->
                val existing = current[id]
                if (existing != null && existing.seasons.isNotEmpty() && newSeries.seasons.isEmpty()) {
                    newSeries.copy(
                        seasons = existing.seasons,
                        uncategorizedEpisodes = existing.uncategorizedEpisodes,
                    )
                } else {
                    newSeries
                }
            }
        }
    }

    fun upsertEpisodes(episodes: List<Episode>) {
        episodesState.update { current -> current + episodes.associateBy { it.id } }
    }

    override suspend fun loadSeasons(seriesId: UUID) = singleFlight.run("LocalMedia:loadSeasons:$seriesId") {
        try {
            // Ensure the series is loaded
            var series = seriesState.value[seriesId]
            if (series == null) {
                loadSeries(seriesId)
                series = seriesState.first()[seriesId] ?: throw RuntimeException("Series not found")
            }

            val existingSeasonsById = series.seasons.associateBy { it.id }
            val updatedSeries = series.copy(
                seasons = jellyfinApiClient.getSeasons(seriesId).map { it.toSeason() }.map { fresh ->
                    val existing = existingSeasonsById[fresh.id]
                    if (existing != null && existing.episodes.isNotEmpty()) {
                        fresh.copy(episodes = existing.episodes)
                    } else {
                        fresh
                    }
                }
            )
            seriesState.update { it + (updatedSeries.id to updatedSeries) }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Timber.tag(TAG).e(error, "Failed to load seasons for series $seriesId")
            throw error
        }
    }

    override suspend fun loadSeasonEpisodes(seriesId: UUID, seasonId: UUID) =
        singleFlight.run("LocalMedia:loadSeasonEpisodes:$seriesId:$seasonId") {
            try {
                // The "Uncategorized" season is synthetic; its episodes are
                // populated as a side effect of loading real seasons, so
                // selecting that tab must not trigger a Jellyfin API call.
                if (seasonId == UNCATEGORIZED_SEASON_ID) return@run

                // Ensure the series and season is loaded
                var series = seriesState.value[seriesId]
                if (series == null) {
                    loadSeries(seriesId)
                    series = seriesState.first()[seriesId] ?: throw RuntimeException("Series not found")
                }
                // The season we are about to load episodes for must be present
                // in `series.seasons` for the `seriesState.update { ... }` block
                // below to attach the fetched episodes to it; otherwise the
                // `map { ... }` iterates over an empty list and the freshly
                // fetched episodes are silently dropped, leaving the series
                // detail screen stuck on "Loading seasons…". If the seasons
                // have not been hydrated yet (e.g. `loadSeasons` from
                // `selectSeries` is still racing with us), load them first.
                if (series.seasons.none { it.id == seasonId }) {
                    loadSeasons(seriesId)
                }

                val serverUrl = userSessionRepository.serverUrl.first()
                val seriesName = seriesState.value[seriesId]?.name
                val (categorized, uncategorized) = jellyfinApiClient
                    .getEpisodesInSeason(seriesId, seasonId)
                    .partition { !it.isUncategorizedEpisode() }
                val categorizedEpisodes = categorized.map {
                    it.toEpisode(serverUrl, fallbackSeriesId = seriesId, fallbackSeriesName = seriesName)
                }
                val uncategorizedEpisodes = uncategorized.map {
                    it.toEpisode(serverUrl, fallbackSeriesId = seriesId, fallbackSeriesName = seriesName)
                }
                seriesState.update { current ->
                    val currentSeries = current[seriesId] ?: return@update current
                    val updatedSeries = currentSeries.copy(
                        seasons = currentSeries.seasons.map {
                            if (it.id == seasonId && it.episodes.isEmpty()) {
                                it.copy(episodes = categorizedEpisodes)
                            } else {
                                it
                            }
                        },
                        uncategorizedEpisodes = (
                            currentSeries.uncategorizedEpisodes + uncategorizedEpisodes
                        ).distinctBy { it.id }
                    )
                    current + (updatedSeries.id to updatedSeries)
                }
                episodesState.update { current ->
                    current + (categorizedEpisodes + uncategorizedEpisodes).associateBy { it.id }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Timber.tag(TAG).e(error, "Failed to load episodes for season $seasonId")
                throw error
            }
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
            val uncategorizedEpisodes = if (
                series.uncategorizedEpisodes.none { it.id == updatedEpisode.id }
            ) {
                series.uncategorizedEpisodes
            } else {
                changed = true
                series.uncategorizedEpisodes.map { episode ->
                    if (episode.id == updatedEpisode.id) updatedEpisode else episode
                }
            }
            if (changed) {
                current + (series.id to series.copy(
                    seasons = seasons,
                    uncategorizedEpisodes = uncategorizedEpisodes,
                ))
            } else {
                current
            }
        }
    }

    private companion object {
        const val TAG = "InMemoryMediaRepository"
    }
}
