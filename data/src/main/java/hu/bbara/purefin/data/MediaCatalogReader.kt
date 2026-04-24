package hu.bbara.purefin.data

import hu.bbara.purefin.model.Episode
import hu.bbara.purefin.model.Movie
import hu.bbara.purefin.model.Series
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MediaCatalogReader {
    val movies: StateFlow<Map<UUID, Movie>>
    val series: StateFlow<Map<UUID, Series>>
    val episodes: StateFlow<Map<UUID, Episode>>
    fun observeSeriesWithContent(seriesId: UUID): Flow<Series?>
}
