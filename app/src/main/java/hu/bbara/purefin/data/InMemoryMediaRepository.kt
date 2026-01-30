package hu.bbara.purefin.data

import androidx.collection.LruCache
import hu.bbara.purefin.client.JellyfinApiClient
import hu.bbara.purefin.data.model.Episode
import hu.bbara.purefin.data.model.Season
import hu.bbara.purefin.data.model.Series
import hu.bbara.purefin.image.JellyfinImageHelper
import hu.bbara.purefin.session.UserSessionRepository
import kotlinx.coroutines.flow.first
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
    val userSessionRepository: UserSessionRepository,
    val jellyfinApiClient: JellyfinApiClient
) : MediaRepository {

    val seriesCache : LruCache<UUID, Series> = LruCache(100)

    override suspend fun getSeries(
        seriesId: UUID,
        includeContent: Boolean
    ): Series {
        val series = fetchAndUpdateSeriesIfMissing(seriesId)
        if (includeContent.not()) {
            return series.copy(seasons = emptyList())
        }

        if (hasContent(series)) {
            return series
        }

        val seasons = getSeasons(
            seriesId = seriesId,
            includeContent = true
        )
        return series.copy(seasons = seasons)
    }

    override suspend fun getSeason(
        seriesId: UUID,
        seasonId: UUID,
        includeContent: Boolean
    ): Season {
        val season = fetchAndUpdateSeasonIfMissing(seriesId, seasonId)
        if (includeContent.not()) {
            return season.copy(episodes = emptyList())
        }

        if (hasContent(season)) {
            return season
        }

        val episodes = getEpisodes(
            seriesId = seriesId,
            seasonId = seasonId
        )
        return season.copy(episodes = episodes)
    }

    override suspend fun getSeasons(
        seriesId: UUID,
        includeContent: Boolean
    ): List<Season> {
        val cachedSeasons = fetchAndUpdateSeasonsIfMissing(seriesId)
        if (includeContent.not()) {
            return cachedSeasons.map { it.copy(episodes = emptyList()) }
        }

        val hasContent = cachedSeasons.all { season ->
            hasContent(season)
        }
        if (hasContent) {
            return cachedSeasons
        }

        return cachedSeasons.map { season ->
            // TODO use batch api that gives back all of the episodes in a single request
            val episodes = getEpisodes(seriesId, season.id)
            season.copy(episodes = episodes)
        }
    }

    override suspend fun getEpisode(
        seriesId: UUID,
        seasonId: UUID,
        episodeId: UUID
    ): Episode {
        val cachedSeason = fetchAndUpdateSeasonIfMissing(seriesId, seasonId)
        cachedSeason.episodes.find { it.id == episodeId }?.let {
            return it
        }

        val episodesItemInfo = jellyfinApiClient.getEpisodesInSeason(seriesId, seasonId)
        val episodes = episodesItemInfo.map { it.toEpisode(serverUrl()) }
        val cachedSeries = seriesCache[seriesId]!!
        val season = cachedSeason.copy(episodes = episodes)
        val updatedSeasons = cachedSeries.seasons.map { if (it.id == seasonId) season else it }
        val updatedSeries = cachedSeries.copy(seasons = updatedSeasons)
        seriesCache.put(seriesId, updatedSeries)
        return episodes.find { it.id == episodeId }!!
    }

    override suspend fun getEpisodes(
        seriesId: UUID,
        seasonId: UUID
    ): List<Episode> {
        val cachedSeason = fetchAndUpdateSeasonIfMissing(seriesId, seasonId)
        if (hasContent(cachedSeason)) {
            return cachedSeason.episodes
        }

        val episodesItemInfo = jellyfinApiClient.getEpisodesInSeason(seriesId, seasonId)
        val episodes = episodesItemInfo.map { it.toEpisode(serverUrl()) }
        val cachedSeries = seriesCache[seriesId]!!
        val updatedSeason = cachedSeason.copy(episodes = episodes)
        val updateSeries = cachedSeries.copy(seasons = cachedSeries.seasons.map { if (it.id == seasonId) updatedSeason else it })
        seriesCache.put(seriesId, updateSeries)
        return episodes
    }

    override suspend fun getEpisodes(seriesId: UUID): List<Episode> {
        val cachedSeasons = fetchAndUpdateSeasonsIfMissing(seriesId)
        if (cachedSeasons.all { hasContent(it) }) {
            return cachedSeasons.flatMap { it.episodes }
        }

        return cachedSeasons.flatMap { season ->
            getEpisodes(seriesId, season.id)
        }
    }

    private suspend fun fetchAndUpdateSeriesIfMissing(seriesId: UUID): Series {
        val cachedSeries = seriesCache[seriesId]
        if (cachedSeries == null) {
            val seriesItemInfo = jellyfinApiClient.getItemInfo(seriesId)
                ?: throw RuntimeException("Series not found")
            val series = seriesItemInfo.toSeries(serverUrl())
            seriesCache.put(seriesId, series)
        }
        return seriesCache[seriesId]!!
    }

    private suspend fun fetchAndUpdateSeasonIfMissing(seriesId: UUID, seasonId: UUID): Season {
        val cachedSeries = fetchAndUpdateSeriesIfMissing(seriesId)
        cachedSeries.seasons.find { it.id == seasonId }?.let {
            return it
        }
        val seasonsItemInfo = jellyfinApiClient.getSeasons(seriesId)
        val seasons = seasonsItemInfo.map { it.toSeason(serverUrl()) }
        val series = cachedSeries.copy(
            seasons = seasons
        )
        seriesCache.put(seriesId, series)
        return seasons.find { it.id == seasonId }!!
    }

    private suspend fun fetchAndUpdateSeasonsIfMissing(seriesId: UUID): List<Season> {
        val cachedSeries = fetchAndUpdateSeriesIfMissing(seriesId)
        if (cachedSeries.seasons.size == cachedSeries.seasonCount) {
            return cachedSeries.seasons
        }

        val seasonsItemInfo = jellyfinApiClient.getSeasons(seriesId)
        val seasons = seasonsItemInfo.map { it.toSeason(serverUrl()) }
        val series = cachedSeries.copy(
            seasons = seasons
        )
        seriesCache.put(seriesId, series)
        return seasons

    }

    private fun hasContent(series: Series): Boolean {
        if (series.seasons.size != series.seasonCount) {
            return false
        }
        for (season in series.seasons) {
            if (hasContent(season).not()) {
                return false
            }
        }
        return true
    }

    private fun hasContent(season: Season) : Boolean {
        return season.episodes.size == season.episodeCount
    }

    private suspend fun serverUrl(): String {
        return userSessionRepository.serverUrl.first()
    }

    private fun BaseItemDto.toSeries(serverUrl: String): Series {
        return Series(
            id = this.id,
            name = this.name ?: "Unknown",
            synopsis = this.overview ?: "No synopsis available",
            year = this.productionYear?.toString()
                ?: this.premiereDate?.year?.toString().orEmpty(),
            heroImageUrl = JellyfinImageHelper.toImageUrl(
                url = serverUrl,
                itemId = this.id,
                type = ImageType.PRIMARY
            ),
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
            index = this.indexNumber!!,
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