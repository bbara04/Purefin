package hu.bbara.purefin.data

import hu.bbara.purefin.data.model.Episode
import hu.bbara.purefin.data.model.Movie
import hu.bbara.purefin.data.model.Season
import hu.bbara.purefin.data.model.Series
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

interface MediaRepository {

    val movies: StateFlow<Map<UUID, Movie>>
    val series: StateFlow<Map<UUID, Series>>
    val state: StateFlow<MediaRepositoryState>

    suspend fun ensureReady()

    suspend fun getMovie(movieId: UUID) : Movie
    suspend fun getSeries(seriesId: UUID) : Series
    suspend fun getSeriesWithContent(seriesId: UUID) : Series
    suspend fun getSeasons(seriesId: UUID) : List<Season>
    suspend fun getSeason(seriesId: UUID, seasonId: UUID) : Season
    suspend fun getEpisodes(seriesId: UUID) : List<Episode>
    suspend fun getEpisodes(seriesId: UUID, seasonId: UUID) : List<Episode>
    suspend fun getEpisode(seriesId: UUID, seasonId: UUID, episodeId: UUID) : Episode
    suspend fun getEpisode(seriesId: UUID, episodeId: UUID) : Episode

    suspend fun updateWatchProgress(mediaId: UUID, positionMs: Long, durationMs: Long)
    suspend fun refreshHomeData()
}
