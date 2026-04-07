package hu.bbara.purefin.core.player.data

import android.util.Log
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import dagger.hilt.android.scopes.ViewModelScoped
import hu.bbara.purefin.core.data.MediaRepository
import hu.bbara.purefin.core.data.client.JellyfinApiClient
import hu.bbara.purefin.core.data.client.PlaybackReportContext
import hu.bbara.purefin.core.data.image.JellyfinImageHelper
import hu.bbara.purefin.core.data.session.UserSessionRepository
import hu.bbara.purefin.core.player.model.PlayerError
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
class PlayerMediaRepository @OptIn(UnstableApi::class)
@Inject constructor(
    private val jellyfinApiClient: JellyfinApiClient,
    private val userSessionRepository: UserSessionRepository,
    private val mediaRepository: MediaRepository,
    private val downloadManager: DownloadManager
) {
    companion object {
        private const val TAG = "PlayerMediaRepo"
    }

    suspend fun getMediaItem(mediaId: UUID, forceTranscode: Boolean = false): PlayerMediaLoadResult = withContext(Dispatchers.IO) {
        when (val onlineResult = buildOnlineMediaItem(mediaId, forceTranscode)) {
            is PlayerMediaLoadResult.Success -> onlineResult
            is PlayerMediaLoadResult.Failure -> {
                when (val offlineResult = buildOfflineMediaItem(mediaId)) {
                    is OfflineLoadResult.Success -> {
                        Log.w(
                            TAG,
                            "Using offline fallback for $mediaId after online load failure (forceTranscode=$forceTranscode): ${onlineResult.error.detailText ?: onlineResult.error.summary}"
                        )
                        offlineResult.result
                    }

                    is OfflineLoadResult.Unavailable -> {
                        val finalError = onlineResult.error.withAdditionalTechnicalDetail(offlineResult.detail)
                        Log.w(
                            TAG,
                            "Unable to resolve media $mediaId (forceTranscode=$forceTranscode): ${finalError.detailText ?: finalError.summary}"
                        )
                        PlayerMediaLoadResult.Failure(finalError)
                    }
                }
            }
        }
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
                val selectedMediaSource = playbackDecision.mediaSource
                val artworkUrl = JellyfinImageHelper.toImageUrl(serverUrl, id, ImageType.PRIMARY)
                val subtitleConfigs = buildExternalSubtitleConfigs(serverUrl, id, selectedMediaSource)
                createMediaItem(
                    mediaId = stringId,
                    playbackUrl = playbackDecision.url,
                    title = episode.name ?: selectedMediaSource.name!!,
                    subtitle = "S${episode.parentIndexNumber}:E${episode.indexNumber}",
                    artworkUrl = artworkUrl,
                    subtitleConfigurations = subtitleConfigs,
                    tag = playbackDecision.reportContext
                )
            }
        }.getOrElse { error ->
            Log.w(TAG, "Unable to load next-up items for $episodeId", error)
            emptyList()
        }
    }

    private suspend fun buildOnlineMediaItem(mediaId: UUID, forceTranscode: Boolean): PlayerMediaLoadResult {
        return try {
            val playbackDecision = jellyfinApiClient.getPlaybackDecision(
                mediaId = mediaId,
                forceTranscode = forceTranscode
            ) ?: run {
                val detail = if (forceTranscode) {
                    "Jellyfin did not return a transcoded playback source."
                } else {
                    "Jellyfin did not return a playable media source."
                }
                Log.w(TAG, "No playback decision for $mediaId (forceTranscode=$forceTranscode)")
                return PlayerMediaLoadResult.Failure(PlayerError.loadFailure(technicalDetail = detail))
            }
            val selectedMediaSource = playbackDecision.mediaSource
            val baseItem = jellyfinApiClient.getItemInfo(mediaId)

            val resumePositionMs = calculateResumePosition(baseItem, selectedMediaSource)
            val serverUrl = userSessionRepository.serverUrl.first()
            val artworkUrl = JellyfinImageHelper.toImageUrl(serverUrl, mediaId, ImageType.PRIMARY)
            val subtitleConfigs = buildExternalSubtitleConfigs(serverUrl, mediaId, selectedMediaSource)

            val mediaItem = createMediaItem(
                mediaId = mediaId.toString(),
                playbackUrl = playbackDecision.url,
                title = baseItem?.name ?: selectedMediaSource.name.orEmpty(),
                subtitle = baseItem?.let { episodeSubtitle(it.parentIndexNumber, it.indexNumber) },
                artworkUrl = artworkUrl,
                subtitleConfigurations = subtitleConfigs,
                tag = playbackDecision.reportContext
            )

            PlayerMediaLoadResult.Success(mediaItem, resumePositionMs)
        } catch (error: Exception) {
            Log.w(TAG, "Online load failed for $mediaId (forceTranscode=$forceTranscode)", error)
            PlayerMediaLoadResult.Failure(PlayerError.fromThrowable(error))
        }
    }

    @OptIn(UnstableApi::class)
    private suspend fun buildOfflineMediaItem(mediaId: UUID): OfflineLoadResult {
        return try {
            val download = downloadManager.downloadIndex.getDownload(mediaId.toString())
                ?.takeIf { it.state == Download.STATE_COMPLETED }
                ?: return OfflineLoadResult.Unavailable("Offline fallback unavailable: no completed download.")

            val serverUrl = userSessionRepository.serverUrl.first()
            val movie = mediaRepository.movies.value[mediaId]
            val episode = mediaRepository.episodes.value[mediaId]

            val title = movie?.title ?: episode?.title ?: String(download.request.data, Charsets.UTF_8).ifBlank {
                "Offline media"
            }
            val subtitle = episode?.let { episodeSubtitle(null, it.index) }
            val artworkUrl = when {
                movie != null -> JellyfinImageHelper.finishImageUrl(movie.imageUrlPrefix, ImageType.PRIMARY)
                episode != null -> JellyfinImageHelper.finishImageUrl(episode.imageUrlPrefix, ImageType.PRIMARY)
                else -> JellyfinImageHelper.toImageUrl(serverUrl, mediaId, ImageType.PRIMARY)
            }
            val resumePositionMs = resumePositionFor(movie, episode)

            val mediaItem = createMediaItem(
                mediaId = mediaId.toString(),
                playbackUrl = download.request.uri.toString(),
                title = title,
                subtitle = subtitle,
                artworkUrl = artworkUrl,
            )
            OfflineLoadResult.Success(PlayerMediaLoadResult.Success(mediaItem, resumePositionMs))
        } catch (error: Exception) {
            Log.w(TAG, "Offline fallback failed for $mediaId", error)
            val technicalDetail = PlayerError.fromThrowable(error).detailText ?: error.javaClass.simpleName
            OfflineLoadResult.Unavailable(
                "Offline fallback failed: $technicalDetail"
            )
        }
    }

    private fun resumePositionFor(movie: hu.bbara.purefin.core.model.Movie?, episode: hu.bbara.purefin.core.model.Episode?): Long? {
        val progress = movie?.progress ?: episode?.progress ?: return null
        val runtime = movie?.runtime ?: episode?.runtime ?: return null
        val durationMs = parseRuntimeToMs(runtime) ?: return null
        if (durationMs <= 0L) return null
        return (durationMs * (progress / 100.0)).toLong().takeIf { it > 0L }
    }

    private fun parseRuntimeToMs(runtime: String): Long? {
        val trimmed = runtime.trim()
        if (trimmed.isBlank() || trimmed == "—") return null

        val hourMatch = Regex("(\\d+)h").find(trimmed)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
        val minuteMatch = Regex("(\\d+)m").find(trimmed)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
        val totalMinutes = hourMatch * 60L + minuteMatch
        return if (totalMinutes > 0L) totalMinutes * 60_000L else null
    }

    private fun episodeSubtitle(seasonNumber: Int?, episodeNumber: Int?): String? {
        if (seasonNumber == null && episodeNumber == null) return null
        return buildString {
            if (seasonNumber != null) append("S").append(seasonNumber)
            if (episodeNumber != null) {
                if (isNotEmpty()) append(":")
                append("E").append(episodeNumber)
            }
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

                Log.d(TAG, "External subtitle: ${stream.displayTitle} ($codec) -> $url")

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
            Log.w(TAG, "Unknown subtitle codec: $codec")
            null
        }
    }

    private fun createMediaItem(
        mediaId: String,
        playbackUrl: String,
        title: String,
        subtitle: String?,
        artworkUrl: String,
        subtitleConfigurations: List<MediaItem.SubtitleConfiguration> = emptyList(),
        tag: PlaybackReportContext? = null
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
            .setTag(tag)
            .setSubtitleConfigurations(subtitleConfigurations)
            .build()
    }

    private sealed interface OfflineLoadResult {
        data class Success(val result: PlayerMediaLoadResult.Success) : OfflineLoadResult
        data class Unavailable(val detail: String) : OfflineLoadResult
    }
}
