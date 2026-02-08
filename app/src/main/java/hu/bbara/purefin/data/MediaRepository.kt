package hu.bbara.purefin.data

import hu.bbara.purefin.data.model.Episode
import hu.bbara.purefin.data.model.Movie
import hu.bbara.purefin.data.model.Series
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

interface MediaRepository {

    val movies: StateFlow<Map<UUID, Movie>>
    val series: StateFlow<Map<UUID, Series>>
    val episodes: StateFlow<Map<UUID, Episode>>
    val state: StateFlow<MediaRepositoryState>

    fun observeSeriesWithContent(seriesId: UUID): Flow<Series?>

    suspend fun ensureReady()

    suspend fun updateWatchProgress(mediaId: UUID, positionMs: Long, durationMs: Long)
    suspend fun refreshHomeData()
}
