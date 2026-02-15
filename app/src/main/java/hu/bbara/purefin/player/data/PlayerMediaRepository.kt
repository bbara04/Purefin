package hu.bbara.purefin.player.data

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import dagger.hilt.android.scopes.ViewModelScoped
import hu.bbara.purefin.client.JellyfinApiClient
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.MediaSourceInfo
import java.util.UUID
import javax.inject.Inject
import androidx.core.net.toUri
import hu.bbara.purefin.image.JellyfinImageHelper
import hu.bbara.purefin.session.UserSessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.jellyfin.sdk.model.api.ImageType

@ViewModelScoped
class PlayerMediaRepository @Inject constructor(
    private val jellyfinApiClient: JellyfinApiClient,
    private val userSessionRepository: UserSessionRepository
) {

    suspend fun getMediaItem(mediaId: UUID): Pair<MediaItem, Long?>? = withContext(Dispatchers.IO) {
        val mediaSources = jellyfinApiClient.getMediaSources(mediaId)
        val selectedMediaSource = mediaSources.firstOrNull() ?: return@withContext null
        val playbackUrl = jellyfinApiClient.getMediaPlaybackUrl(
            mediaId = mediaId,
            mediaSourceId = selectedMediaSource.id
        ) ?: return@withContext null
        val baseItem = jellyfinApiClient.getItemInfo(mediaId)

        // Calculate resume position
        val resumePositionMs = calculateResumePosition(baseItem, selectedMediaSource)

        val serverUrl = userSessionRepository.serverUrl.first()
        val artworkUrl = JellyfinImageHelper.toImageUrl(serverUrl, mediaId, ImageType.PRIMARY)

        val mediaItem = createMediaItem(
            mediaId = mediaId.toString(),
            playbackUrl = playbackUrl,
            title = baseItem?.name ?: selectedMediaSource.name!!,
            subtitle = "S${baseItem!!.parentIndexNumber}:E${baseItem.indexNumber}",
            artworkUrl = artworkUrl
        )

        Pair(mediaItem, resumePositionMs)
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

    suspend fun getNextUpMediaItems(episodeId: UUID, existingIds: Set<String>, count: Int = 5): List<MediaItem> = withContext(Dispatchers.IO) {
        val serverUrl = userSessionRepository.serverUrl.first()
        val episodes = jellyfinApiClient.getNextEpisodes(episodeId = episodeId, count = count)
        episodes.mapNotNull { episode ->
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
            val artworkUrl = JellyfinImageHelper.toImageUrl(serverUrl, id, ImageType.PRIMARY)
            createMediaItem(
                mediaId = stringId,
                playbackUrl = playbackUrl,
                title = episode.name ?: selectedMediaSource.name!!,
                subtitle = "S${episode.parentIndexNumber}:E${episode.indexNumber}",
                artworkUrl = artworkUrl
            )
        }
    }

    private fun createMediaItem(
        mediaId: String,
        playbackUrl: String,
        title: String,
        subtitle: String?,
        artworkUrl: String
    ): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setArtworkUri(artworkUrl.toUri())
            .build()
        return MediaItem.Builder()
            .setUri(playbackUrl.toUri())
            .setMediaId(mediaId)
            .setMediaMetadata(metadata)
            .build()
    }
}
