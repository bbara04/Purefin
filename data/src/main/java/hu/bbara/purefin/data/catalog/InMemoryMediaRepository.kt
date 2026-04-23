package hu.bbara.purefin.data.catalog

import hu.bbara.purefin.core.data.MediaCatalogReader
import hu.bbara.purefin.core.data.MediaProgressWriter
import hu.bbara.purefin.data.jellyfin.client.JellyfinApiClient
import hu.bbara.purefin.core.image.ImageUrlBuilder
import hu.bbara.purefin.core.data.session.UserSessionRepository
import hu.bbara.purefin.core.model.Episode
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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jellyfin.sdk.model.api.BaseItemDto

@Singleton
class InMemoryMediaRepository @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val jellyfinApiClient: JellyfinApiClient,
) : MediaCatalogReader, MediaProgressWriter {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val moviesState = MutableStateFlow<Map<UUID, Movie>>(emptyMap())
    override val movies: StateFlow<Map<UUID, Movie>> = moviesState.asStateFlow()

    private val seriesState = MutableStateFlow<Map<UUID, Series>>(emptyMap())
    override val series: StateFlow<Map<UUID, Series>> = seriesState.asStateFlow()

    private val episodesState = MutableStateFlow<Map<UUID, Episode>>(emptyMap())
    override val episodes: StateFlow<Map<UUID, Episode>> = episodesState.asStateFlow()

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
            ensureSeriesContentLoaded(seriesId)
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
        if (date == null) return fallbackYear?.toString() ?: "—"
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
}
