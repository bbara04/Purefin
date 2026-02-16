package hu.bbara.purefin.player.data

import android.util.Log
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import dagger.hilt.android.scopes.ViewModelScoped
import hu.bbara.purefin.client.JellyfinApiClient
import hu.bbara.purefin.image.JellyfinImageHelper
import hu.bbara.purefin.session.UserSessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.ImageType
import org.jellyfin.sdk.model.api.MediaSourceInfo
import org.jellyfin.sdk.model.api.MediaStreamType
import org.jellyfin.sdk.model.api.SubtitleDeliveryMethod
import java.util.UUID
import javax.inject.Inject

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
            mediaSource = selectedMediaSource
        ) ?: return@withContext null
        val baseItem = jellyfinApiClient.getItemInfo(mediaId)

        // Calculate resume position
        val resumePositionMs = calculateResumePosition(baseItem, selectedMediaSource)

        val serverUrl = userSessionRepository.serverUrl.first()
        val artworkUrl = JellyfinImageHelper.toImageUrl(serverUrl, mediaId, ImageType.PRIMARY)

        val subtitleConfigs = buildExternalSubtitleConfigs(serverUrl, mediaId, selectedMediaSource)

        val mediaItem = createMediaItem(
            mediaId = mediaId.toString(),
            playbackUrl = playbackUrl,
            title = baseItem?.name ?: selectedMediaSource.name!!,
            subtitle = "S${baseItem!!.parentIndexNumber}:E${baseItem.indexNumber}",
            artworkUrl = artworkUrl,
            subtitleConfigurations = subtitleConfigs
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
                mediaSource = selectedMediaSource
            ) ?: return@mapNotNull null
            val artworkUrl = JellyfinImageHelper.toImageUrl(serverUrl, id, ImageType.PRIMARY)
            val subtitleConfigs = buildExternalSubtitleConfigs(serverUrl, id, selectedMediaSource)
            createMediaItem(
                mediaId = stringId,
                playbackUrl = playbackUrl,
                title = episode.name ?: selectedMediaSource.name!!,
                subtitle = "S${episode.parentIndexNumber}:E${episode.indexNumber}",
                artworkUrl = artworkUrl,
                subtitleConfigurations = subtitleConfigs
            )
        }
    }

    @OptIn(UnstableApi::class)
    private fun buildExternalSubtitleConfigs(
        serverUrl: String,
        mediaId: UUID,
        mediaSource: MediaSourceInfo
    ): List<MediaItem.SubtitleConfiguration> {
        val streams = mediaSource.mediaStreams ?: return emptyList()
        val mediaSourceId = mediaSource.id ?: return emptyList()
        val baseUrl = serverUrl.trimEnd('/')

        return streams
            .filter { it.type == MediaStreamType.SUBTITLE && it.deliveryMethod == SubtitleDeliveryMethod.EXTERNAL }
            .mapNotNull { stream ->
                val codec = stream.codec ?: return@mapNotNull null
                val mimeType = subtitleCodecToMimeType(codec) ?: return@mapNotNull null
                // Use deliveryUrl from server if available, otherwise construct it
                val url = if (!stream.deliveryUrl.isNullOrBlank()) {
                    if (stream.deliveryUrl!!.startsWith("http")) {
                        stream.deliveryUrl!!
                    } else {
                        "$baseUrl${stream.deliveryUrl}"
                    }
                } else {
                    val format = if (codec == "subrip") "srt" else codec
                    "$baseUrl/Videos/$mediaId/$mediaSourceId/Subtitles/${stream.index}/0/Stream.$format"
                }

                Log.d("PlayerMediaRepo", "External subtitle: ${stream.displayTitle} ($codec) -> $url")

                MediaItem.SubtitleConfiguration.Builder(url.toUri())
                    .setMimeType(mimeType)
                    .setLanguage(stream.language)
                    .setLabel(stream.displayTitle ?: stream.language ?: "Track ${stream.index}")
                    .setSelectionFlags(
                        if (stream.isForced) C.SELECTION_FLAG_FORCED
                        else if (stream.isDefault) C.SELECTION_FLAG_DEFAULT
                        else 0
                    )
                    .build()
            }
    }

    @OptIn(UnstableApi::class)
    private fun subtitleCodecToMimeType(codec: String): String? = when (codec.lowercase()) {
        "srt", "subrip" -> MimeTypes.APPLICATION_SUBRIP
        "ass", "ssa" -> MimeTypes.TEXT_SSA
        "vtt", "webvtt" -> MimeTypes.TEXT_VTT
        "ttml", "dfxp" -> MimeTypes.APPLICATION_TTML
        "sub", "microdvd" -> MimeTypes.APPLICATION_SUBRIP // sub often converted to srt by Jellyfin
        "pgs", "pgssub" -> MimeTypes.APPLICATION_PGS
        else -> {
            Log.w("PlayerMediaRepo", "Unknown subtitle codec: $codec")
            null
        }
    }

    private fun createMediaItem(
        mediaId: String,
        playbackUrl: String,
        title: String,
        subtitle: String?,
        artworkUrl: String,
        subtitleConfigurations: List<MediaItem.SubtitleConfiguration> = emptyList()
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
            .setSubtitleConfigurations(subtitleConfigurations)
            .build()
    }
}
