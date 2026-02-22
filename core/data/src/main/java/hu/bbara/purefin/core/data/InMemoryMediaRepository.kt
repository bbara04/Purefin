package hu.bbara.purefin.core.data

import hu.bbara.purefin.core.data.client.JellyfinApiClient
import hu.bbara.purefin.core.data.image.JellyfinImageHelper
import hu.bbara.purefin.core.data.session.UserSessionRepository
import hu.bbara.purefin.core.model.Episode
import hu.bbara.purefin.core.model.Movie
import hu.bbara.purefin.core.model.Season
import hu.bbara.purefin.core.model.Series
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
    private val userSessionRepository: UserSessionRepository,
    private val jellyfinApiClient: JellyfinApiClient,
) : MediaRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val ready: MutableStateFlow<Boolean> = MutableStateFlow(false)

    internal val _movies: MutableStateFlow<Map<UUID, Movie>> = MutableStateFlow(emptyMap())
    override val movies: StateFlow<Map<UUID, Movie>> = _movies.asStateFlow()

    internal val _series: MutableStateFlow<Map<UUID, Series>> = MutableStateFlow(emptyMap())
    override val series: StateFlow<Map<UUID, Series>> = _series.asStateFlow()

    internal val _episodes: MutableStateFlow<Map<UUID, Episode>> = MutableStateFlow(emptyMap())
    override val episodes: StateFlow<Map<UUID, Episode>> = _episodes.asStateFlow()
    override fun upsertMovies(movies: List<Movie>) {
        _movies.update { current -> current + movies.associateBy { it.id } }
    }

    override fun upsertSeries(series: List<Series>) {
        _series.update { current -> current + series.associateBy { it.id } }
    }

    override fun upsertEpisodes(episodes: List<Episode>) {
        _episodes.update { current -> current + episodes.associateBy { it.id } }
    }

    override fun observeSeriesWithContent(seriesId: UUID): Flow<Series?> {
        scope.launch {
            ensureSeriesContentLoaded(seriesId)
        }
        return _series.map { it[seriesId] }
    }

    override fun setReady() {
        ready.value = true
    }

    private suspend fun ensureSeriesContentLoaded(seriesId: UUID) {
        _series.value[seriesId]?.takeIf { it.seasons.isNotEmpty() }?.let { return }

        val series = _series.value[seriesId] ?: throw RuntimeException("Series not found")
        val serverUrl = serverUrl()

        val emptySeasonsItem = jellyfinApiClient.getSeasons(seriesId)
        val emptySeasons = emptySeasonsItem.map { it.toSeason(serverUrl) }
        val filledSeasons = emptySeasons.map { season ->
            val episodesItem = jellyfinApiClient.getEpisodesInSeason(seriesId, season.id)
            val episodes = episodesItem.map { it.toEpisode(serverUrl) }
            season.copy(episodes = episodes)
        }
        val updatedSeries = series.copy(seasons = filledSeasons)
        _series.update { it + (updatedSeries.id to updatedSeries) }

        val allEpisodes = filledSeasons.flatMap { it.episodes }
        _episodes.update { current -> current + allEpisodes.associateBy { it.id } }
    }

    override suspend fun updateWatchProgress(mediaId: UUID, positionMs: Long, durationMs: Long) {
        if (durationMs <= 0) return
        val progressPercent = (positionMs.toDouble() / durationMs.toDouble()) * 100.0
        val watched = progressPercent >= 90.0

        if (_movies.value.containsKey(mediaId)) {
            _movies.update { current ->
                val movie = current[mediaId] ?: return@update current
                current + (mediaId to movie.copy(progress = progressPercent, watched = watched))
            }
            return
        }
        if (_episodes.value.containsKey(mediaId)) {
            _episodes.update { current ->
                val episode = current[mediaId] ?: return@update current
                current + (mediaId to episode.copy(progress = progressPercent, watched = watched))
            }
        }
    }

    private suspend fun serverUrl(): String = userSessionRepository.serverUrl.first()

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
            JellyfinImageHelper.toImageUrl(url = serverUrl, itemId = itemId, type = ImageType.PRIMARY)
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
