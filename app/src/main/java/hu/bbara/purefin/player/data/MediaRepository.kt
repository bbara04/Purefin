package hu.bbara.purefin.player.data

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import dagger.hilt.android.scopes.ViewModelScoped
import hu.bbara.purefin.client.JellyfinApiClient
import java.util.UUID
import javax.inject.Inject

@ViewModelScoped
class MediaRepository @Inject constructor(
    private val jellyfinApiClient: JellyfinApiClient
) {

    suspend fun getMediaItem(mediaId: UUID): MediaItem? {
        val mediaSources = jellyfinApiClient.getMediaSources(mediaId)
        val selectedMediaSource = mediaSources.firstOrNull() ?: return null
        val playbackUrl = jellyfinApiClient.getMediaPlaybackUrl(
            mediaId = mediaId,
            mediaSourceId = selectedMediaSource.id
        ) ?: return null
        val baseItem = jellyfinApiClient.getItemInfo(mediaId)
        return createMediaItem(
            mediaId = mediaId.toString(),
            playbackUrl = playbackUrl,
            title = baseItem?.name ?: selectedMediaSource.name,
            subtitle = "S${baseItem!!.parentIndexNumber}:E${baseItem.indexNumber}"
        )
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
