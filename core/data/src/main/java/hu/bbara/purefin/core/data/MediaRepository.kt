package hu.bbara.purefin.core.data

import hu.bbara.purefin.core.model.Episode
import hu.bbara.purefin.core.model.Movie
import hu.bbara.purefin.core.model.Series
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

interface MediaRepository {
    val ready: StateFlow<Boolean>
    val movies: StateFlow<Map<UUID, Movie>>
    val series: StateFlow<Map<UUID, Series>>
    val episodes: StateFlow<Map<UUID, Episode>>
    fun upsertMovies(movies: List<Movie>) {
    }
    fun upsertSeries(series: List<Series>) {
    }
    fun upsertEpisodes(episodes: List<Episode>) {
    }
    fun observeSeriesWithContent(seriesId: UUID): Flow<Series?>
    fun setReady() {
    }
    suspend fun updateWatchProgress(mediaId: UUID, positionMs: Long, durationMs: Long)
}