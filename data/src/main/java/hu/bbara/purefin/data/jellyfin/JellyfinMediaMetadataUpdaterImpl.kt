package hu.bbara.purefin.data.jellyfin

import hu.bbara.purefin.core.data.LocalMediaRepository
import hu.bbara.purefin.core.data.MediaMetadataUpdater
import hu.bbara.purefin.data.jellyfin.client.JellyfinApiClient
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JellyfinMediaMetadataUpdaterImpl @Inject constructor(
    private val jellyfinApiClient: JellyfinApiClient,
    private val localMediaRepository: LocalMediaRepository,
) : MediaMetadataUpdater {

    override suspend fun markAsWatched(mediaId: UUID, watched: Boolean) {
        if (watched) {
            jellyfinApiClient.markAsWatched(mediaId)
        } else {
            jellyfinApiClient.markAsUnwatched(mediaId)
        }
        localMediaRepository.markAsWatched(mediaId, watched)
    }

    override suspend fun updateWatchProgress(mediaId: UUID, positionMs: Long, durationMs: Long) {
        localMediaRepository.updateWatchProgress(mediaId, positionMs, durationMs)
    }

    override suspend fun updateWatchProgressPercent(mediaId: UUID, progressPercent: Double) {
        localMediaRepository.updateWatchProgressPercent(mediaId, progressPercent)
    }

    override suspend fun updatePlaybackPosition(
        mediaId: UUID,
        playbackPositionTicks: Long,
        runtimeTicks: Long,
    ) {
        jellyfinApiClient.updatePlaybackPosition(mediaId, playbackPositionTicks, runtimeTicks)
    }
}
