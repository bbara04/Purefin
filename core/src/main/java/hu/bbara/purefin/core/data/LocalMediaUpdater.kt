package hu.bbara.purefin.core.data

import java.util.UUID

interface LocalMediaUpdater {
    suspend fun updateWatchProgress(mediaId: UUID, positionMs: Long, durationMs: Long)
    suspend fun updateWatchProgressPercent(mediaId: UUID, progressPercent: Double)
    suspend fun markAsWatched(mediaId: UUID, watched: Boolean)
}
