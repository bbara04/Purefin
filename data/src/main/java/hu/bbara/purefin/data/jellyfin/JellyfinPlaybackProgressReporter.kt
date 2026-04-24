package hu.bbara.purefin.data.jellyfin

import hu.bbara.purefin.data.PlaybackProgressReporter
import hu.bbara.purefin.data.jellyfin.client.JellyfinApiClient
import hu.bbara.purefin.data.PlaybackReportContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JellyfinPlaybackProgressReporter @Inject constructor(
    private val jellyfinApiClient: JellyfinApiClient,
) : PlaybackProgressReporter {
    override suspend fun reportPlaybackStart(itemId: UUID, positionTicks: Long, reportContext: PlaybackReportContext) {
        jellyfinApiClient.reportPlaybackStart(itemId, positionTicks, reportContext)
    }

    override suspend fun reportPlaybackProgress(
        itemId: UUID,
        positionTicks: Long,
        isPaused: Boolean,
        reportContext: PlaybackReportContext,
    ) {
        jellyfinApiClient.reportPlaybackProgress(itemId, positionTicks, isPaused, reportContext)
    }

    override suspend fun reportPlaybackStopped(itemId: UUID, positionTicks: Long, reportContext: PlaybackReportContext) {
        jellyfinApiClient.reportPlaybackStopped(itemId, positionTicks, reportContext)
    }
}
