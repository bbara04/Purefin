package hu.bbara.purefin.core.data

import hu.bbara.purefin.core.model.Episode
import hu.bbara.purefin.core.model.Library
import hu.bbara.purefin.core.model.Media
import hu.bbara.purefin.core.model.MediaRepositoryState
import hu.bbara.purefin.core.model.Movie
import hu.bbara.purefin.core.model.Series
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

interface MediaRepository {

    val libraries: StateFlow<List<Library>>
    val movies: StateFlow<Map<UUID, Movie>>
    val series: StateFlow<Map<UUID, Series>>
    val episodes: StateFlow<Map<UUID, Episode>>
    val state: StateFlow<MediaRepositoryState>

    val continueWatching: StateFlow<List<Media>>
    val nextUp: StateFlow<List<Media>>
    val latestLibraryContent: StateFlow<Map<UUID, List<Media>>>

    fun observeSeriesWithContent(seriesId: UUID): Flow<Series?>

    suspend fun ensureReady()

    suspend fun updateWatchProgress(mediaId: UUID, positionMs: Long, durationMs: Long)
    suspend fun refreshHomeData()
}
