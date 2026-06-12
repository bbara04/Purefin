package hu.bbara.purefin.core.data

import java.util.UUID

/**
 * Unified interface for updating media metadata (watched status, watch progress,
 * and server-side playback position).
 *
 * Implementations coordinate both local cache updates and remote server calls
 * so that callers have a single point of entry for all metadata mutations.
 */
interface MediaMetadataUpdater {
    /**
     * Mark the given media item as watched or unwatched.
     * Updates both the local cache and the remote server.
     */
    suspend fun markAsWatched(mediaId: UUID, watched: Boolean)

    /**
     * Compute progress from absolute position/duration and persist it
     * to the local cache (in-memory and/or Room).
     */
    suspend fun updateWatchProgress(mediaId: UUID, positionMs: Long, durationMs: Long)

    /**
     * Persist a raw progress percentage (0.0 – 100.0) to the local cache.
     */
    suspend fun updateWatchProgressPercent(mediaId: UUID, progressPercent: Double)

    /**
     * Update the server-side playback position (in ticks) and optionally
     * mark the item as played when past the threshold (≥ 80 %).
     */
    suspend fun updatePlaybackPosition(
        mediaId: UUID,
        playbackPositionTicks: Long,
        runtimeTicks: Long,
    )
}
