package hu.bbara.purefin.data.catalog

import android.util.Log
import hu.bbara.purefin.data.LocalMediaRepository
import hu.bbara.purefin.data.UserSessionRepository
import hu.bbara.purefin.data.converter.toEpisode
import hu.bbara.purefin.data.converter.toMovie
import hu.bbara.purefin.data.converter.toSeason
import hu.bbara.purefin.data.converter.toSeries
import hu.bbara.purefin.data.jellyfin.client.JellyfinApiClient
import hu.bbara.purefin.model.Episode
import hu.bbara.purefin.model.Genre
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jellyfin.sdk.model.api.BaseItemKind
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

    private val genresState = MutableStateFlow<Set<Genre>>(emptySet())
    override var genres: StateFlow<Set<Genre>> = genresState.asStateFlow()

    override suspend fun getMovie(id: UUID): Flow<Movie?> {
        if (!moviesState.value.containsKey(id)) {
            jellyfinApiClient.getItemInfo(id)?.let { item ->
                if (item.type != BaseItemKind.MOVIE) {
                    Log.d("InMemoryMediaRepository", "Item is not an movie: ${item.type}")
                    return flowOf(null)
                }
                val movie = item.toMovie(serverUrl.first())
                moviesState.update { current -> current + (movie.id to movie) }
            }
        }
        return moviesState.map { it[id] }
    }

    override suspend fun getSeries(id: UUID): Flow<Series?> {
        if (!seriesState.value.containsKey(id)) {
            jellyfinApiClient.getItemInfo(id)?.let { item ->
                if (item.type != BaseItemKind.SERIES) {
                    Log.d("InMemoryMediaRepository", "Item is not an series: ${item.type}")
                    return flowOf(null)
                }
                val series = item.toSeries(serverUrl.first())
                seriesState.update { current -> current + (series.id to series) }
            }
        }
        return seriesState.map { it[id] }
    }

    override suspend fun getEpisode(id: UUID): Flow<Episode?> {
        if (!episodesState.value.containsKey(id)) {
            jellyfinApiClient.getItemInfo(id)?.let { item ->
                if (item.type != BaseItemKind.EPISODE) {
                    Log.d("InMemoryMediaRepository", "Item is not an episode: ${item.type}")
                    return flowOf(null)
                }
                val episode = item.toEpisode(serverUrl.first())
                episodesState.update { current -> current + (episode.id to episode) }
            }
        }
        val episode = episodesState.value[id] ?: return flowOf(null)
        observeSeriesWithContent(seriesId = episode.seriesId)
        return episodesState.map { it[id] }
    }

    fun upsertGenres(genres: Set<Genre>) {
        genresState.update { current -> current + genres }
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

    override fun observeSeriesWithContent(seriesId: UUID): Flow<Series?> {
        scope.launch {
            try {
                ensureSeriesContentLoaded(seriesId)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.e("InMemoryMediaRepository", "Failed to load content for series $seriesId", error)
                throw error
            }
        }
        return seriesState.map { it[seriesId] }
    }

    override suspend fun updateWatchProgress(mediaId: UUID, positionMs: Long, durationMs: Long) {
        if (durationMs <= 0) return
        val progressPercent = (positionMs.toDouble() / durationMs.toDouble()) * 100.0
        val watched = progressPercent >= 90.0

        if (moviesState.value.containsKey(mediaId)) {
            moviesState.update { current ->
                val movie = current[mediaId] ?: return@update current
                current + (mediaId to movie.copy(progress = progressPercent, watched = watched))
            }
            return
        }
        if (episodesState.value.containsKey(mediaId)) {
            episodesState.update { current ->
                val episode = current[mediaId] ?: return@update current
                current + (mediaId to episode.copy(progress = progressPercent, watched = watched))
            }
        }
    }

    private suspend fun ensureSeriesContentLoaded(seriesId: UUID) {
        seriesState.value[seriesId]?.takeIf { it.seasons.isNotEmpty() }?.let { return }

        val series = seriesState.value[seriesId] ?: throw RuntimeException("Series not found")
        val serverUrl = userSessionRepository.serverUrl.first()

        val emptySeasons = jellyfinApiClient.getSeasons(seriesId).map { it.toSeason() }
        val filledSeasons = emptySeasons.map { season ->
            val episodes = jellyfinApiClient.getEpisodesInSeason(seriesId, season.id).map { it.toEpisode(serverUrl) }
            season.copy(episodes = episodes)
        }

        val updatedSeries = series.copy(seasons = filledSeasons)
        seriesState.update { it + (updatedSeries.id to updatedSeries) }

        val allEpisodes = filledSeasons.flatMap { it.episodes }
        episodesState.update { current -> current + allEpisodes.associateBy { it.id } }
    }
}
