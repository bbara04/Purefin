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

interface AppContentRepository : MediaRepository {

    val libraries: StateFlow<List<Library>>
    val state: StateFlow<MediaRepositoryState>
    val continueWatching: StateFlow<List<Media>>
    val nextUp: StateFlow<List<Media>>
    val latestLibraryContent: StateFlow<Map<UUID, List<Media>>>
    suspend fun ensureReady()
    suspend fun refreshHomeData()
}
