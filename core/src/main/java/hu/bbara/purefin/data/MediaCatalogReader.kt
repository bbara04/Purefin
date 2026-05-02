package hu.bbara.purefin.data

import hu.bbara.purefin.model.Episode
import hu.bbara.purefin.model.Genre
import hu.bbara.purefin.model.Movie
import hu.bbara.purefin.model.Series
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

//TODO rename MediaRepository
interface MediaCatalogReader {
    val movies: StateFlow<Map<UUID, Movie>>
    val series: StateFlow<Map<UUID, Series>>
    val episodes: StateFlow<Map<UUID, Episode>>
    val genres: StateFlow<Set<Genre>>
    suspend fun getMovie(id: UUID): Flow<Movie?>
    suspend fun getSeries(id: UUID): Flow<Series?>
    suspend fun getEpisode(id: UUID): Flow<Episode?>
    fun observeSeriesWithContent(seriesId: UUID): Flow<Series?>
}
