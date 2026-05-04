package hu.bbara.purefin.data

import java.util.UUID

interface LocalMediaUpdater {
    suspend fun updateWatchProgress(mediaId: UUID, positionMs: Long, durationMs: Long)
    suspend fun markAsWatched(mediaId: UUID, watched: Boolean)
}
