package hu.bbara.purefin.data

import java.util.UUID

interface MediaProgressWriter {
    suspend fun updateWatchProgress(mediaId: UUID, positionMs: Long, durationMs: Long)
}
