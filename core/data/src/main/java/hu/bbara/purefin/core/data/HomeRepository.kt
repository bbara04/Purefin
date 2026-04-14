package hu.bbara.purefin.core.data

import hu.bbara.purefin.core.model.Library
import hu.bbara.purefin.core.model.Media
import java.util.UUID
import kotlinx.coroutines.flow.StateFlow

interface HomeRepository {
    val libraries: StateFlow<List<Library>>
    val suggestions: StateFlow<List<Media>>
    val continueWatching: StateFlow<List<Media>>
    val nextUp: StateFlow<List<Media>>
    val latestLibraryContent: StateFlow<Map<UUID, List<Media>>>
    suspend fun ensureReady()
    suspend fun refreshHomeData()
}
