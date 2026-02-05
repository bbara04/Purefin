package hu.bbara.purefin.player.data

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import dagger.hilt.android.scopes.ViewModelScoped
import hu.bbara.purefin.client.JellyfinApiClient
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.MediaSourceInfo
import java.util.UUID
import javax.inject.Inject

@ViewModelScoped
class MediaRepository @Inject constructor(
    private val jellyfinApiClient: JellyfinApiClient
) {

    suspend fun getMediaItem(mediaId: UUID): Pair<MediaItem, Long?>? {
        val mediaSources = jellyfinApiClient.getMediaSources(mediaId)
        val selectedMediaSource = mediaSources.firstOrNull() ?: return null
        val playbackUrl = jellyfinApiClient.getMediaPlaybackUrl(
            mediaId = mediaId,
            mediaSourceId = selectedMediaSource.id
        ) ?: return null
        val baseItem = jellyfinApiClient.getItemInfo(mediaId)

        // Calculate resume position
        val resumePositionMs = calculateResumePosition(baseItem, selectedMediaSource)

        val mediaItem = createMediaItem(
            mediaId = mediaId.toString(),
            playbackUrl = playbackUrl,
            title = baseItem?.name ?: selectedMediaSource.name,
            subtitle = "S${baseItem!!.parentIndexNumber}:E${baseItem.indexNumber}"
        )

        return Pair(mediaItem, resumePositionMs)
    }

    private fun calculateResumePosition(
        baseItem: BaseItemDto?,
        mediaSource: MediaSourceInfo
    ): Long? {
        val userData = baseItem?.userData ?: return null

        // Get runtime in ticks
        val runtimeTicks = mediaSource.runTimeTicks ?: baseItem.runTimeTicks ?: 0L
        if (runtimeTicks == 0L) return null

        // Get saved playback position from userData
        val playbackPositionTicks = userData.playbackPositionTicks ?: 0L
        if (playbackPositionTicks == 0L) return null

        // Convert ticks to milliseconds
        val positionMs = playbackPositionTicks / 10_000

        // Calculate percentage for threshold check
        val percentage = (playbackPositionTicks.toDouble() / runtimeTicks.toDouble()) * 100.0

        // Apply thresholds: resume only if 5% ≤ progress ≤ 95%
        return if (percentage in 5.0..95.0) positionMs else null
    }

    suspend fun getNextUpMediaItems(episodeId: UUID, existingIds: Set<String>, count: Int = 2): List<MediaItem> {
        val episodes = jellyfinApiClient.getNextEpisodes(episodeId = episodeId, count = count)
        return episodes.mapNotNull { episode ->
            val id = episode.id ?: return@mapNotNull null
            val stringId = id.toString()
            if (existingIds.contains(stringId)) {
                return@mapNotNull null
            }
            val mediaSources = jellyfinApiClient.getMediaSources(id)
            val selectedMediaSource = mediaSources.firstOrNull() ?: return@mapNotNull null
            val playbackUrl = jellyfinApiClient.getMediaPlaybackUrl(
                mediaId = id,
                mediaSourceId = selectedMediaSource.id
            ) ?: return@mapNotNull null
            createMediaItem(
                mediaId = stringId,
                playbackUrl = playbackUrl,
                title = episode.name ?: selectedMediaSource.name,
                subtitle = "S${episode.parentIndexNumber}:E${episode.indexNumber}"
            )
        }
    }

    private fun createMediaItem(
        mediaId: String,
        playbackUrl: String,
        title: String?,
        subtitle: String?
    ): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .build()
        return MediaItem.Builder()
            .setUri(Uri.parse(playbackUrl))
            .setMediaId(mediaId)
            .setMediaMetadata(metadata)
            .build()
    }
}
