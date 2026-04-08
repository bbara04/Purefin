package hu.bbara.purefin.core.data.client

import android.media.MediaCodecInfo
import android.media.MediaCodecInfo.CodecProfileLevel
import android.media.MediaFormat

internal data class AndroidCodecCatalog(
    val supportedVideoCodecs: Set<String>,
    val supportedAudioCodecs: Set<String>,
    val videoCodecProfiles: Map<String, Set<String>>,
) {
    companion object {
        fun create(codecInfos: Array<MediaCodecInfo>): AndroidCodecCatalog {
            val supportedVideoCodecs = linkedSetOf<String>()
            val supportedAudioCodecs = linkedSetOf<String>()
            val videoCodecProfiles = linkedMapOf<String, MutableSet<String>>()

            for (codecInfo in codecInfos) {
                if (codecInfo.isEncoder) continue

                for (mimeType in codecInfo.supportedTypes) {
                    val capabilities = try {
                        codecInfo.getCapabilitiesForType(mimeType)
                    } catch (_: IllegalArgumentException) {
                        continue
                    }

                    androidVideoCodecName(mimeType)?.let { codec ->
                        supportedVideoCodecs += codec
                        val profiles = videoCodecProfiles.getOrPut(codec) { linkedSetOf() }
                        capabilities.profileLevels
                            .mapNotNull { androidVideoProfileName(codec, it.profile) }
                            .forEach(profiles::add)
                    }

                    androidAudioCodecName(mimeType)?.let(supportedAudioCodecs::add)
                }
            }

            return AndroidCodecCatalog(
                supportedVideoCodecs = supportedVideoCodecs,
                supportedAudioCodecs = supportedAudioCodecs,
                videoCodecProfiles = videoCodecProfiles.mapValues { it.value.toSet() },
            )
        }
    }
}

private fun androidVideoCodecName(mimeType: String): String? = when (mimeType) {
    MediaFormat.MIMETYPE_VIDEO_MPEG2 -> "mpeg2video"
    MediaFormat.MIMETYPE_VIDEO_H263 -> "h263"
    MediaFormat.MIMETYPE_VIDEO_MPEG4 -> "mpeg4"
    MediaFormat.MIMETYPE_VIDEO_AVC -> "h264"
    MediaFormat.MIMETYPE_VIDEO_HEVC, MediaFormat.MIMETYPE_VIDEO_DOLBY_VISION -> "hevc"
    MediaFormat.MIMETYPE_VIDEO_VP8 -> "vp8"
    MediaFormat.MIMETYPE_VIDEO_VP9 -> "vp9"
    MediaFormat.MIMETYPE_VIDEO_AV1 -> "av1"
    else -> null
}

private fun androidAudioCodecName(mimeType: String): String? = when (mimeType) {
    MediaFormat.MIMETYPE_AUDIO_AAC -> "aac"
    MediaFormat.MIMETYPE_AUDIO_AC3 -> "ac3"
    MediaFormat.MIMETYPE_AUDIO_AMR_WB, MediaFormat.MIMETYPE_AUDIO_AMR_NB -> "3gpp"
    MediaFormat.MIMETYPE_AUDIO_EAC3 -> "eac3"
    MediaFormat.MIMETYPE_AUDIO_FLAC -> "flac"
    MediaFormat.MIMETYPE_AUDIO_MPEG -> "mp3"
    MediaFormat.MIMETYPE_AUDIO_OPUS -> "opus"
    MediaFormat.MIMETYPE_AUDIO_RAW -> "raw"
    MediaFormat.MIMETYPE_AUDIO_VORBIS -> "vorbis"
    MediaFormat.MIMETYPE_AUDIO_QCELP,
    MediaFormat.MIMETYPE_AUDIO_MSGSM,
    MediaFormat.MIMETYPE_AUDIO_G711_MLAW,
    MediaFormat.MIMETYPE_AUDIO_G711_ALAW,
    -> null
    else -> null
}

private fun androidVideoProfileName(codec: String, profile: Int): String? = when (codec) {
    "mpeg2video" -> mpeg2ProfileName(profile)
    "h263" -> h263ProfileName(profile)
    "mpeg4" -> mpeg4ProfileName(profile)
    "h264" -> avcProfileName(profile)
    "hevc" -> hevcProfileName(profile)
    "vp8" -> vp8ProfileName(profile)
    "vp9" -> vp9ProfileName(profile)
    else -> null
}

private fun mpeg2ProfileName(profile: Int): String? = when (profile) {
    CodecProfileLevel.MPEG2ProfileSimple -> "simple profile"
    CodecProfileLevel.MPEG2ProfileMain -> "main profile"
    CodecProfileLevel.MPEG2Profile422 -> "422 profile"
    CodecProfileLevel.MPEG2ProfileSNR -> "snr profile"
    CodecProfileLevel.MPEG2ProfileSpatial -> "spatial profile"
    CodecProfileLevel.MPEG2ProfileHigh -> "high profile"
    else -> null
}

private fun h263ProfileName(profile: Int): String? = when (profile) {
    CodecProfileLevel.H263ProfileBaseline -> "baseline"
    CodecProfileLevel.H263ProfileH320Coding -> "h320 coding"
    CodecProfileLevel.H263ProfileBackwardCompatible -> "backward compatible"
    CodecProfileLevel.H263ProfileISWV2 -> "isw v2"
    CodecProfileLevel.H263ProfileISWV3 -> "isw v3"
    CodecProfileLevel.H263ProfileHighCompression -> "high compression"
    CodecProfileLevel.H263ProfileInternet -> "internet"
    CodecProfileLevel.H263ProfileInterlace -> "interlace"
    CodecProfileLevel.H263ProfileHighLatency -> "high latency"
    else -> null
}

private fun mpeg4ProfileName(profile: Int): String? = when (profile) {
    CodecProfileLevel.MPEG4ProfileAdvancedCoding -> "advanced coding profile"
    CodecProfileLevel.MPEG4ProfileAdvancedCore -> "advanced core profile"
    CodecProfileLevel.MPEG4ProfileAdvancedRealTime -> "advanced realtime profile"
    CodecProfileLevel.MPEG4ProfileAdvancedSimple -> "advanced simple profile"
    CodecProfileLevel.MPEG4ProfileBasicAnimated -> "basic animated profile"
    CodecProfileLevel.MPEG4ProfileCore -> "core profile"
    CodecProfileLevel.MPEG4ProfileCoreScalable -> "core scalable profile"
    CodecProfileLevel.MPEG4ProfileHybrid -> "hybrid profile"
    CodecProfileLevel.MPEG4ProfileNbit -> "nbit profile"
    CodecProfileLevel.MPEG4ProfileScalableTexture -> "scalable texture profile"
    CodecProfileLevel.MPEG4ProfileSimple -> "simple profile"
    CodecProfileLevel.MPEG4ProfileSimpleFBA -> "simple fba profile"
    CodecProfileLevel.MPEG4ProfileSimpleFace -> "simple face profile"
    CodecProfileLevel.MPEG4ProfileSimpleScalable -> "simple scalable profile"
    CodecProfileLevel.MPEG4ProfileMain -> "main profile"
    else -> null
}

private fun avcProfileName(profile: Int): String? = when (profile) {
    CodecProfileLevel.AVCProfileBaseline -> "baseline"
    CodecProfileLevel.AVCProfileMain -> "main"
    CodecProfileLevel.AVCProfileExtended -> "extended"
    CodecProfileLevel.AVCProfileHigh -> "high"
    CodecProfileLevel.AVCProfileHigh10 -> "high 10"
    CodecProfileLevel.AVCProfileHigh422 -> "high 422"
    CodecProfileLevel.AVCProfileHigh444 -> "high 444"
    CodecProfileLevel.AVCProfileConstrainedBaseline -> "constrained baseline"
    CodecProfileLevel.AVCProfileConstrainedHigh -> "constrained high"
    else -> null
}

private fun hevcProfileName(profile: Int): String? = when (profile) {
    CodecProfileLevel.HEVCProfileMain -> "Main"
    CodecProfileLevel.HEVCProfileMain10 -> "Main 10"
    CodecProfileLevel.HEVCProfileMain10HDR10 -> "Main 10 HDR 10"
    CodecProfileLevel.HEVCProfileMain10HDR10Plus -> "Main 10 HDR 10 Plus"
    CodecProfileLevel.HEVCProfileMainStill -> "Main Still"
    else -> null
}

private fun vp8ProfileName(profile: Int): String? = when (profile) {
    CodecProfileLevel.VP8ProfileMain -> "main"
    else -> null
}

private fun vp9ProfileName(profile: Int): String? = when (profile) {
    CodecProfileLevel.VP9Profile0 -> "Profile 0"
    CodecProfileLevel.VP9Profile1 -> "Profile 1"
    CodecProfileLevel.VP9Profile2,
    CodecProfileLevel.VP9Profile2HDR,
    -> "Profile 2"
    CodecProfileLevel.VP9Profile3,
    CodecProfileLevel.VP9Profile3HDR,
    -> "Profile 3"
    else -> null
}
