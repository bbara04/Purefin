package hu.bbara.purefin.core.data

import hu.bbara.purefin.model.Episode
import hu.bbara.purefin.model.Movie
import hu.bbara.purefin.model.Series
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

interface LocalMediaRepository : LocalMediaUpdater {
    val movies: StateFlow<Map<UUID, Movie>>
    val series: StateFlow<Map<UUID, Series>>
    val episodes: StateFlow<Map<UUID, Episode>>
    suspend fun getMovie(id: UUID): Flow<Movie?>
    suspend fun getSeries(id: UUID): Flow<Series?>
    suspend fun getEpisode(id: UUID): Flow<Episode?>
    suspend fun loadSeasons(seriesId: UUID)
    suspend fun loadSeasonEpisodes(seriesId: UUID, seasonId: UUID)
}
