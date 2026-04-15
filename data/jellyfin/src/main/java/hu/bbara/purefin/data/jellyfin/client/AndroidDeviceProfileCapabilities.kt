package hu.bbara.purefin.data.jellyfin.client

import android.media.MediaCodecInfo.CodecProfileLevel
import android.media.MediaCodecList
import android.media.MediaFormat
import android.os.Build
import androidx.media3.common.MimeTypes

data class ProfileResolution(
    val width: Int,
    val height: Int,
)

interface DeviceProfileCapabilities {
    fun supportsAv1(): Boolean
    fun supportsAv1Main10(): Boolean
    fun supportsAv1DolbyVision(): Boolean
    fun supportsAv1Hdr10(): Boolean
    fun supportsAv1Hdr10Plus(): Boolean
    fun supportsAvc(): Boolean
    fun supportsAvcHigh10(): Boolean
    fun avcMainLevel(): Int
    fun avcHigh10Level(): Int
    fun supportsHevc(): Boolean
    fun supportsHevcMain10(): Boolean
    fun supportsHevcDolbyVision(): Boolean
    fun supportsHevcDolbyVisionEl(): Boolean
    fun supportsHevcHdr10(): Boolean
    fun supportsHevcHdr10Plus(): Boolean
    fun hevcMainLevel(): Int
    fun hevcMain10Level(): Int
    fun supportsVc1(): Boolean
    fun maxResolution(mimeType: String): ProfileResolution
    fun supportsVideoCodec(codec: String): Boolean
    fun supportsAudioCodec(codec: String): Boolean
    fun videoCodecProfiles(codec: String): Set<String>
    val hevcDoviHdr10PlusBug: Boolean
}

internal class AndroidDeviceProfileCapabilities : DeviceProfileCapabilities {
    private val mediaCodecList by lazy { MediaCodecList(MediaCodecList.REGULAR_CODECS) }
    private val codecCatalog by lazy { AndroidCodecCatalog.create(mediaCodecList.codecInfos) }

    private object DolbyVisionProfiles {
        val profile7: Int by lazy {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                CodecProfileLevel.DolbyVisionProfileDvheDtb
            } else {
                -1
            }
        }
    }

    private object Av1ProfileLevels {
        val profileMain10: Int by lazy {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                CodecProfileLevel.AV1ProfileMain10
            } else {
                0x2
            }
        }
        val profileMain10Hdr10: Int by lazy {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                CodecProfileLevel.AV1ProfileMain10HDR10
            } else {
                0x1000
            }
        }
        val profileMain10Hdr10Plus: Int by lazy {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                CodecProfileLevel.AV1ProfileMain10HDR10Plus
            } else {
                0x2000
            }
        }
        val dolbyVisionProfile10: Int by lazy {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                CodecProfileLevel.DolbyVisionProfileDvav110
            } else {
                0x400
            }
        }
        val level5: Int by lazy {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                CodecProfileLevel.AV1Level5
            } else {
                0x1000
            }
        }
    }

    private val avcLevels = listOf(
        CodecProfileLevel.AVCLevel1b to 9,
        CodecProfileLevel.AVCLevel1 to 10,
        CodecProfileLevel.AVCLevel11 to 11,
        CodecProfileLevel.AVCLevel12 to 12,
        CodecProfileLevel.AVCLevel13 to 13,
        CodecProfileLevel.AVCLevel2 to 20,
        CodecProfileLevel.AVCLevel21 to 21,
        CodecProfileLevel.AVCLevel22 to 22,
        CodecProfileLevel.AVCLevel3 to 30,
        CodecProfileLevel.AVCLevel31 to 31,
        CodecProfileLevel.AVCLevel32 to 32,
        CodecProfileLevel.AVCLevel4 to 40,
        CodecProfileLevel.AVCLevel41 to 41,
        CodecProfileLevel.AVCLevel42 to 42,
        CodecProfileLevel.AVCLevel5 to 50,
        CodecProfileLevel.AVCLevel51 to 51,
        CodecProfileLevel.AVCLevel52 to 52,
    )

    private val hevcLevels = listOf(
        CodecProfileLevel.HEVCMainTierLevel1 to 30,
        CodecProfileLevel.HEVCMainTierLevel2 to 60,
        CodecProfileLevel.HEVCMainTierLevel21 to 63,
        CodecProfileLevel.HEVCMainTierLevel3 to 90,
        CodecProfileLevel.HEVCMainTierLevel31 to 93,
        CodecProfileLevel.HEVCMainTierLevel4 to 120,
        CodecProfileLevel.HEVCMainTierLevel41 to 123,
        CodecProfileLevel.HEVCMainTierLevel5 to 150,
        CodecProfileLevel.HEVCMainTierLevel51 to 153,
        CodecProfileLevel.HEVCMainTierLevel52 to 156,
        CodecProfileLevel.HEVCMainTierLevel6 to 180,
        CodecProfileLevel.HEVCMainTierLevel61 to 183,
        CodecProfileLevel.HEVCMainTierLevel62 to 186,
    )

    override fun supportsAv1(): Boolean = hasCodecForMime(MimeTypes.VIDEO_AV1)

    override fun supportsAv1Main10(): Boolean = hasDecoder(
        MimeTypes.VIDEO_AV1,
        Av1ProfileLevels.profileMain10,
        Av1ProfileLevels.level5,
    )

    override fun supportsAv1DolbyVision(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
        hasDecoder(
            MimeTypes.VIDEO_DOLBY_VISION,
            Av1ProfileLevels.dolbyVisionProfile10,
            CodecProfileLevel.DolbyVisionLevelHd24,
        )

    override fun supportsAv1Hdr10(): Boolean = hasDecoder(
        MimeTypes.VIDEO_AV1,
        Av1ProfileLevels.profileMain10Hdr10,
        Av1ProfileLevels.level5,
    )

    override fun supportsAv1Hdr10Plus(): Boolean = hasDecoder(
        MimeTypes.VIDEO_AV1,
        Av1ProfileLevels.profileMain10Hdr10Plus,
        Av1ProfileLevels.level5,
    )

    override fun supportsAvc(): Boolean = hasCodecForMime(MediaFormat.MIMETYPE_VIDEO_AVC)

    override fun supportsAvcHigh10(): Boolean = hasDecoder(
        MediaFormat.MIMETYPE_VIDEO_AVC,
        CodecProfileLevel.AVCProfileHigh10,
        CodecProfileLevel.AVCLevel4,
    )

    override fun avcMainLevel(): Int = avcLevel(CodecProfileLevel.AVCProfileMain)

    override fun avcHigh10Level(): Int = avcLevel(CodecProfileLevel.AVCProfileHigh10)

    override fun supportsHevc(): Boolean = hasCodecForMime(MediaFormat.MIMETYPE_VIDEO_HEVC)

    override fun supportsHevcMain10(): Boolean = hasDecoder(
        MediaFormat.MIMETYPE_VIDEO_HEVC,
        CodecProfileLevel.HEVCProfileMain10,
        CodecProfileLevel.HEVCMainTierLevel4,
    )

    override fun supportsHevcDolbyVision(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
        hasCodecForMime(MediaFormat.MIMETYPE_VIDEO_DOLBY_VISION)

    override fun supportsHevcDolbyVisionEl(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
        hasDecoder(
            MediaFormat.MIMETYPE_VIDEO_DOLBY_VISION,
            DolbyVisionProfiles.profile7,
            CodecProfileLevel.DolbyVisionLevelHd24,
        ) &&
        supportsMultiInstance(MediaFormat.MIMETYPE_VIDEO_HEVC)

    override fun supportsHevcHdr10(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
        hasDecoder(
            MediaFormat.MIMETYPE_VIDEO_HEVC,
            CodecProfileLevel.HEVCProfileMain10HDR10,
            CodecProfileLevel.HEVCMainTierLevel4,
        )

    override fun supportsHevcHdr10Plus(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
        hasDecoder(
            MediaFormat.MIMETYPE_VIDEO_HEVC,
            CodecProfileLevel.HEVCProfileMain10HDR10Plus,
            CodecProfileLevel.HEVCMainTierLevel4,
        )

    override fun hevcMainLevel(): Int = hevcLevel(CodecProfileLevel.HEVCProfileMain)

    override fun hevcMain10Level(): Int = hevcLevel(CodecProfileLevel.HEVCProfileMain10)

    override fun supportsVc1(): Boolean = hasCodecForMime(MimeTypes.VIDEO_VC1)

    override fun maxResolution(mimeType: String): ProfileResolution {
        var maxWidth = 0
        var maxHeight = 0

        for (info in mediaCodecList.codecInfos) {
            if (info.isEncoder) continue

            try {
                val videoCapabilities = info.getCapabilitiesForType(mimeType).videoCapabilities ?: continue
                val supportedWidth = videoCapabilities.supportedWidths?.upper ?: continue
                val supportedHeight = videoCapabilities.supportedHeights?.upper ?: continue

                maxWidth = maxOf(maxWidth, supportedWidth)
                maxHeight = maxOf(maxHeight, supportedHeight)
            } catch (_: IllegalArgumentException) {
                // Codec not supported.
            }
        }

        return ProfileResolution(maxWidth, maxHeight)
    }

    override fun supportsVideoCodec(codec: String): Boolean = codecCatalog.supportedVideoCodecs.contains(codec)

    override fun supportsAudioCodec(codec: String): Boolean = codecCatalog.supportedAudioCodecs.contains(codec)

    override fun videoCodecProfiles(codec: String): Set<String> = codecCatalog.videoCodecProfiles[codec].orEmpty()

    override val hevcDoviHdr10PlusBug: Boolean = Build.MODEL in modelsWithDoviHdr10PlusBug

    private fun avcLevel(profile: Int): Int {
        val decoderLevel = decoderLevel(MediaFormat.MIMETYPE_VIDEO_AVC, profile)
        return avcLevels.asReversed().find { decoderLevel >= it.first }?.second ?: 0
    }

    private fun hevcLevel(profile: Int): Int {
        val decoderLevel = decoderLevel(MediaFormat.MIMETYPE_VIDEO_HEVC, profile)
        return hevcLevels.asReversed().find { decoderLevel >= it.first }?.second ?: 0
    }

    private fun decoderLevel(mimeType: String, profile: Int): Int {
        var maxLevel = 0
        for (info in mediaCodecList.codecInfos) {
            if (info.isEncoder) continue

            try {
                val capabilities = info.getCapabilitiesForType(mimeType)
                for (profileLevel in capabilities.profileLevels) {
                    if (profileLevel.profile == profile) {
                        maxLevel = maxOf(maxLevel, profileLevel.level)
                    }
                }
            } catch (_: IllegalArgumentException) {
                // Codec not supported.
            }
        }
        return maxLevel
    }

    private fun hasDecoder(mimeType: String, profile: Int, level: Int): Boolean {
        for (info in mediaCodecList.codecInfos) {
            if (info.isEncoder) continue

            try {
                val capabilities = info.getCapabilitiesForType(mimeType)
                for (profileLevel in capabilities.profileLevels) {
                    if (profileLevel.profile != profile) continue
                    if (profileLevel.level >= level) return true
                }
            } catch (_: IllegalArgumentException) {
                // Codec not supported.
            }
        }
        return false
    }

    private fun hasCodecForMime(mimeType: String): Boolean {
        for (info in mediaCodecList.codecInfos) {
            if (info.isEncoder) continue
            if (info.supportedTypes.any { it.equals(mimeType, ignoreCase = true) }) {
                return true
            }
        }
        return false
    }

    private fun supportsMultiInstance(mimeType: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false

        for (info in mediaCodecList.codecInfos) {
            if (info.isEncoder) continue

            try {
                if (!info.supportedTypes.contains(mimeType)) continue
                if (info.getCapabilitiesForType(mimeType).maxSupportedInstances > 1) {
                    return true
                }
            } catch (_: IllegalArgumentException) {
                // Codec not supported.
            }
        }
        return false
    }
}

private val modelsWithDoviHdr10PlusBug = setOf(
    "AFTKRT",
    "AFTKA",
    "AFTKM",
)
