package hu.bbara.purefin.core.data

import hu.bbara.purefin.model.Episode
import hu.bbara.purefin.model.Media
import hu.bbara.purefin.model.Movie
import hu.bbara.purefin.model.Series
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

interface LocalMediaRepository : MediaMetadataUpdater {
    val movies: StateFlow<Map<UUID, Movie>>
    val series: StateFlow<Map<UUID, Series>>
    val episodes: StateFlow<Map<UUID, Episode>>
    suspend fun getTypeById(id: UUID): Media? {
        return when (val media = movies.value[id] ?: series.value[id] ?: episodes.value[id]) {
            is Movie -> Media.MovieMedia(movieId = media.id)
            is Series -> Media.SeriesMedia(seriesId = media.id)
            is Episode -> Media.EpisodeMedia(seriesId = media.seriesId, episodeId = media.id)
            else -> null
        }
    }
    suspend fun getMovie(id: UUID): Flow<Movie?>
    suspend fun getSeries(id: UUID): Flow<Series?>
    suspend fun getEpisode(id: UUID): Flow<Episode?>
    suspend fun loadMovie(id: UUID)
    suspend fun loadSeries(id: UUID)
    suspend fun loadEpisode(id: UUID)
    suspend fun loadSeasons(seriesId: UUID)
    suspend fun loadSeasonEpisodes(seriesId: UUID, seasonId: UUID)
}
