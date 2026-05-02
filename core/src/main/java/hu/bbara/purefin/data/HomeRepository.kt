package hu.bbara.purefin.data

import hu.bbara.purefin.model.Library
import hu.bbara.purefin.model.Media
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

interface HomeRepository {
    val libraries: StateFlow<List<Library>>
    val suggestions: StateFlow<List<Media>>
    val continueWatching: StateFlow<List<Media>>
    val nextUp: StateFlow<List<Media>>
    val latestLibraryContent: StateFlow<Map<UUID, List<Media>>>
    fun ensureReady()
    suspend fun refreshHomeData()
}
