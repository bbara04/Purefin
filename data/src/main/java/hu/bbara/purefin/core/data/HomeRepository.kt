package hu.bbara.purefin.core.data

import hu.bbara.purefin.core.model.Library
import hu.bbara.purefin.core.model.Media
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

interface HomeRepository {
    val libraries: StateFlow<List<Library>>
    val suggestions: StateFlow<List<Media>>
    val continueWatching: StateFlow<List<Media>>
    val nextUp: StateFlow<List<Media>>
    val latestLibraryContent: StateFlow<Map<UUID, List<Media>>>
    fun ensureReady()
    fun refreshHomeData()
}
