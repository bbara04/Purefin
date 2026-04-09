package hu.bbara.purefin.core.player.data

import android.util.Log
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import dagger.hilt.android.scopes.ViewModelScoped
import hu.bbara.purefin.core.data.client.JellyfinApiClient
import hu.bbara.purefin.core.data.client.PlaybackReportContext
import hu.bbara.purefin.core.data.image.JellyfinImageHelper
import hu.bbara.purefin.core.data.session.UserSessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.ImageType
import org.jellyfin.sdk.model.api.MediaSourceInfo
import java.util.UUID
import javax.inject.Inject

@ViewModelScoped
class PlayerMediaRepository @Inject constructor(
    private val jellyfinApiClient: JellyfinApiClient,
    private val userSessionRepository: UserSessionRepository
) {

    suspend fun getMediaItem(mediaId: UUID): Pair<MediaItem, Long?>? = withContext(Dispatchers.IO) {
        val playbackDecision = jellyfinApiClient.getPlaybackDecision(mediaId) ?: return@withContext null
        val baseItem = jellyfinApiClient.getItemInfo(mediaId)

        val resumePositionMs = calculateResumePosition(baseItem, playbackDecision.mediaSource)

        val serverUrl = userSessionRepository.serverUrl.first()
        val artworkUrl = JellyfinImageHelper.toImageUrl(serverUrl, mediaId, ImageType.PRIMARY)

        val mediaItem = createMediaItem(
            mediaId = mediaId.toString(),
            playbackUrl = playbackDecision.url,
            title = baseItem?.name ?: playbackDecision.mediaSource.name ?: return@withContext null,
            subtitle = seasonEpisodeLabel(baseItem),
            artworkUrl = artworkUrl,
            playbackReportContext = playbackDecision.reportContext,
        )

        Pair(mediaItem, resumePositionMs)
    }

    suspend fun getNextUpMediaItems(episodeId: UUID, existingIds: Set<String>, count: Int = 9): List<MediaItem> = withContext(Dispatchers.IO) {
        runCatching {
            val serverUrl = userSessionRepository.serverUrl.first()
            val episodes = jellyfinApiClient.getNextEpisodes(episodeId = episodeId, count = count)
            episodes.mapNotNull { episode ->
                val id = episode.id ?: return@mapNotNull null
                val stringId = id.toString()
                if (existingIds.contains(stringId)) {
                    return@mapNotNull null
                }
                val playbackDecision = jellyfinApiClient.getPlaybackDecision(id) ?: return@mapNotNull null
                val artworkUrl = JellyfinImageHelper.toImageUrl(serverUrl, id, ImageType.PRIMARY)
                createMediaItem(
                    mediaId = stringId,
                    playbackUrl = playbackDecision.url,
                    title = episode.name ?: playbackDecision.mediaSource.name ?: return@mapNotNull null,
                    subtitle = seasonEpisodeLabel(episode),
                    artworkUrl = artworkUrl,
                    playbackReportContext = playbackDecision.reportContext,
                )
            }
        }.getOrElse { error ->
            Log.w("PlayerMediaRepo", "Unable to load next-up items for $episodeId", error)
            emptyList()
        }
    }

    private fun createMediaItem(
        mediaId: String,
        playbackUrl: String,
        title: String,
        subtitle: String?,
        artworkUrl: String,
        playbackReportContext: PlaybackReportContext?,
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
            .setTag(playbackReportContext)
            .build()
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

    private fun seasonEpisodeLabel(item: BaseItemDto?): String? {
        val seasonNumber = item?.parentIndexNumber ?: return null
        val episodeNumber = item.indexNumber ?: return null
        return "S$seasonNumber:E$episodeNumber"
    }
}
