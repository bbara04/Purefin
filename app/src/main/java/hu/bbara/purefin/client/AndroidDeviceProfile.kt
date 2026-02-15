package hu.bbara.purefin.client

import android.media.MediaCodecList
import android.util.Log
import org.jellyfin.sdk.model.api.DeviceProfile
import org.jellyfin.sdk.model.api.DirectPlayProfile
import org.jellyfin.sdk.model.api.DlnaProfileType
import org.jellyfin.sdk.model.api.SubtitleDeliveryMethod
import org.jellyfin.sdk.model.api.SubtitleProfile

/**
 * Creates a DeviceProfile for Android devices with proper codec support detection.
 * This prevents playback failures by requesting transcoding for unsupported formats like DTS-HD.
 */
object AndroidDeviceProfile {

    fun create(): DeviceProfile {
        // Debug: Log all available decoders
        CodecDebugHelper.logAvailableDecoders()

        val audioCodecs = getAudioCodecs()
        val videoCodecs = getVideoCodecs()

        Log.d("AndroidDeviceProfile", "Supported audio codecs: ${audioCodecs.joinToString()}")
        Log.d("AndroidDeviceProfile", "Supported video codecs: ${videoCodecs.joinToString()}")

        // Check specifically for DTS
        val hasDTS = CodecDebugHelper.hasDecoderFor("audio/vnd.dts")
        val hasDTSHD = CodecDebugHelper.hasDecoderFor("audio/vnd.dts.hd")
        Log.d("AndroidDeviceProfile", "Has DTS decoder: $hasDTS, Has DTS-HD decoder: $hasDTSHD")

        return DeviceProfile(
            name = "Android",
            maxStaticBitrate = 100_000_000,
            maxStreamingBitrate = 100_000_000,

            // Direct play profiles - what we can play natively
            // By specifying supported codecs, Jellyfin will transcode unsupported formats like DTS-HD
            directPlayProfiles = listOf(
                DirectPlayProfile(
                    type = DlnaProfileType.VIDEO,
                    container = "mp4,m4v,mkv,webm",
                    videoCodec = videoCodecs.joinToString(","),
                    audioCodec = audioCodecs.joinToString(",")
                )
            ),

            // Empty transcoding profiles - Jellyfin will use its defaults
            transcodingProfiles = emptyList(),

            codecProfiles = emptyList(),

            subtitleProfiles = listOf(
                SubtitleProfile("srt", SubtitleDeliveryMethod.EXTERNAL),
                SubtitleProfile("ass", SubtitleDeliveryMethod.EXTERNAL),
                SubtitleProfile("ssa", SubtitleDeliveryMethod.EXTERNAL),
                SubtitleProfile("vtt", SubtitleDeliveryMethod.EXTERNAL),
                SubtitleProfile("sub", SubtitleDeliveryMethod.EXTERNAL)
            ),

            containerProfiles = emptyList()
        )
    }

    /**
     * Get list of supported audio codecs on this device.
     * Excludes unsupported formats like DTS, DTS-HD, TrueHD which commonly cause playback failures.
     */
    private fun getAudioCodecs(): List<String> {
        val supportedCodecs = mutableListOf<String>()

        // Common codecs supported on most Android devices
        val commonCodecs = listOf(
            "aac" to android.media.MediaFormat.MIMETYPE_AUDIO_AAC,
            "mp3" to android.media.MediaFormat.MIMETYPE_AUDIO_MPEG,
            "ac3" to android.media.MediaFormat.MIMETYPE_AUDIO_AC3,
            "eac3" to android.media.MediaFormat.MIMETYPE_AUDIO_EAC3,
            "flac" to android.media.MediaFormat.MIMETYPE_AUDIO_FLAC,
            "vorbis" to android.media.MediaFormat.MIMETYPE_AUDIO_VORBIS,
            "opus" to android.media.MediaFormat.MIMETYPE_AUDIO_OPUS
        )

        for ((codecName, mimeType) in commonCodecs) {
            if (isCodecSupported(mimeType)) {
                supportedCodecs.add(codecName)
            }
        }

        // AAC is mandatory on Android - ensure it's always included
        if (!supportedCodecs.contains("aac")) {
            supportedCodecs.add("aac")
        }

        return supportedCodecs
    }

    /**
     * Get list of supported video codecs on this device.
     */
    private fun getVideoCodecs(): List<String> {
        val supportedCodecs = mutableListOf<String>()

        val commonCodecs = listOf(
            "h264" to android.media.MediaFormat.MIMETYPE_VIDEO_AVC,
            "hevc" to android.media.MediaFormat.MIMETYPE_VIDEO_HEVC,
            "vp9" to android.media.MediaFormat.MIMETYPE_VIDEO_VP9,
            "vp8" to android.media.MediaFormat.MIMETYPE_VIDEO_VP8,
            "mpeg4" to android.media.MediaFormat.MIMETYPE_VIDEO_MPEG4,
            "av1" to android.media.MediaFormat.MIMETYPE_VIDEO_AV1
        )

        for ((codecName, mimeType) in commonCodecs) {
            if (isCodecSupported(mimeType)) {
                supportedCodecs.add(codecName)
            }
        }

        // H.264 is mandatory on Android - ensure it's always included
        if (!supportedCodecs.contains("h264")) {
            supportedCodecs.add("h264")
        }

        return supportedCodecs
    }

    /**
     * Check if a specific decoder (not encoder) is supported on this device.
     */
    private fun isCodecSupported(mimeType: String): Boolean {
        return try {
            val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
            codecList.codecInfos.any { codecInfo ->
                !codecInfo.isEncoder &&
                codecInfo.supportedTypes.any { it.equals(mimeType, ignoreCase = true) }
            }
        } catch (_: Exception) {
            // If we can't determine, assume not supported
            false
        }
    }
}
