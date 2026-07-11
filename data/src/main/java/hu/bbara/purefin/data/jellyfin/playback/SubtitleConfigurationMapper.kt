package hu.bbara.purefin.data.jellyfin.playback

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
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

            val selectionFlags = buildSelectionFlags(stream.isForced, stream.isDefault)

            MediaItem.SubtitleConfiguration.Builder(resolvedUri.toUri())
                .setMimeType(mimeType)
                .setLanguage(stream.language)
                .setLabel(stream.displayTitle)
                .setSelectionFlags(selectionFlags)
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
    private fun buildSelectionFlags(isForced: Boolean?, isDefault: Boolean?): Int {
        var flags = 0
        if (isForced == true) flags = flags or C.SELECTION_FLAG_FORCED
        if (isDefault == true) flags = flags or C.SELECTION_FLAG_DEFAULT
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

    private fun String.toUri() = android.net.Uri.parse(this)
}
