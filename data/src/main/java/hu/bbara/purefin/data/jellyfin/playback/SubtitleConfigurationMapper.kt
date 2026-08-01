package hu.bbara.purefin.data.jellyfin.playback

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import hu.bbara.purefin.model.ExternalSubtitle
import org.jellyfin.sdk.model.api.MediaSourceInfo
import org.jellyfin.sdk.model.api.MediaStreamType
import org.jellyfin.sdk.model.api.SubtitleDeliveryMethod
import javax.inject.Inject

class SubtitleConfigurationMapper @Inject constructor() {

    @OptIn(UnstableApi::class)
    fun createSubtitleConfigurations(
        mediaSource: MediaSourceInfo,
        serverUrl: String,
    ): List<MediaItem.SubtitleConfiguration> {
        val subtitles = extractExternalSubtitles(mediaSource, serverUrl)
        if (subtitles.isEmpty()) return emptyList()
        return toSubtitleConfigurations(subtitles) { url -> android.net.Uri.parse(url) }
    }

    /**
     * Extracts the external (sidecar) subtitle streams from [mediaSource] as
     * transport-agnostic [ExternalSubtitle] descriptors. Embedded subtitles
     * are ignored — they are delivered inside the media container.
     */
    fun extractExternalSubtitles(
        mediaSource: MediaSourceInfo,
        serverUrl: String,
    ): List<ExternalSubtitle> {
        val streams = mediaSource.mediaStreams.orEmpty()
        if (streams.isEmpty()) return emptyList()

        return streams.mapNotNull { stream ->
            if (stream.type != MediaStreamType.SUBTITLE) return@mapNotNull null
            if (stream.deliveryMethod != SubtitleDeliveryMethod.EXTERNAL) return@mapNotNull null

            val deliveryUrl = stream.deliveryUrl?.trim()?.takeIf { it.isNotBlank() }
                ?: return@mapNotNull null

            val mimeType = mimeTypeForCodec(stream.codec)
                ?: return@mapNotNull null

            val resolvedUri = resolveUrl(serverUrl, deliveryUrl)

            ExternalSubtitle(
                index = stream.index,
                language = stream.language,
                label = stream.displayTitle,
                mimeType = mimeType,
                forced = stream.isForced == true,
                defaultTrack = stream.isDefault == true,
                remoteUrl = resolvedUri,
            )
        }
    }

    /**
     * Builds ExoPlayer [MediaItem.SubtitleConfiguration]s from a list of
     * subtitle descriptors. The [uriTransform] lets callers point at either a
     * remote URL (online playback) or a local file (offline playback).
     */
    @OptIn(UnstableApi::class)
    fun toSubtitleConfigurations(
        subtitles: List<ExternalSubtitle>,
        uriTransform: (String) -> android.net.Uri,
    ): List<MediaItem.SubtitleConfiguration> {
        if (subtitles.isEmpty()) return emptyList()
        return subtitles.map { sub ->
            MediaItem.SubtitleConfiguration.Builder(uriTransform(sub.remoteUrl))
                .setMimeType(sub.mimeType)
                .setLanguage(sub.language)
                .setLabel(sub.label)
                .setSelectionFlags(buildSelectionFlags(sub.forced, sub.defaultTrack))
                .build()
        }
    }

    @OptIn(UnstableApi::class)
    private fun mimeTypeForCodec(codec: String?): String? {
        if (codec.isNullOrBlank()) return null
        return when (codec.lowercase().trim()) {
            "srt", "subrip" -> MimeTypes.APPLICATION_SUBRIP
            "vtt", "webvtt" -> MimeTypes.TEXT_VTT
            "ttml" -> MimeTypes.APPLICATION_TTML
            "ass", "ssa" -> MimeTypes.TEXT_SSA
            else -> null
        }
    }

    @OptIn(UnstableApi::class)
    private fun buildSelectionFlags(isForced: Boolean, isDefault: Boolean): Int {
        var flags = 0
        if (isForced) flags = flags or C.SELECTION_FLAG_FORCED
        if (isDefault) flags = flags or C.SELECTION_FLAG_DEFAULT
        return flags
    }

    private fun resolveUrl(serverUrl: String, url: String): String {
        if (
            url.startsWith("http://", ignoreCase = true) ||
            url.startsWith("https://", ignoreCase = true)
        ) {
            return url
        }
        return "${serverUrl.trimEnd('/')}/${url.trimStart('/')}"
    }
}