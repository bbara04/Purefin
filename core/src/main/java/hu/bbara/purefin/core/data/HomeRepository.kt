package hu.bbara.purefin.core.data

import hu.bbara.purefin.core.model.MediaUiModel
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

    /**
     * Fetches the full content (movies or series) of a single library on
     * demand. Used by the library detail screen; the home page no longer
     * pays for full library content on every refresh.
     *
     * Returns `null` when the server returned 304 Not Modified for the
     * per-library ETag — the caller should keep its previously fetched
     * copy. An empty list is a legitimate "the library is empty" result.
     */
    suspend fun loadLibraryContent(libraryId: UUID): List<MediaUiModel>?
}
