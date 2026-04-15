package hu.bbara.purefin.core.data

import java.util.UUID

interface PlaybackProgressReporter {
    suspend fun reportPlaybackStart(itemId: UUID, positionTicks: Long, reportContext: PlaybackReportContext)
    suspend fun reportPlaybackProgress(
        itemId: UUID,
        positionTicks: Long,
        isPaused: Boolean,
        reportContext: PlaybackReportContext,
    )
    suspend fun reportPlaybackStopped(itemId: UUID, positionTicks: Long, reportContext: PlaybackReportContext)
}
