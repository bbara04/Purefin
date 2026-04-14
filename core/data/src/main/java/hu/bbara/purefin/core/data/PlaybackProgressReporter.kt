package hu.bbara.purefin.core.data

import hu.bbara.purefin.core.data.client.PlaybackReportContext
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
