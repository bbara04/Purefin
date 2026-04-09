package hu.bbara.purefin.core.data.client

import androidx.media3.common.MimeTypes
import org.jellyfin.sdk.model.ServerVersion
import org.jellyfin.sdk.model.api.CodecType
import org.jellyfin.sdk.model.api.DeviceProfile
import org.jellyfin.sdk.model.api.DlnaProfileType
import org.jellyfin.sdk.model.api.EncodingContext
import org.jellyfin.sdk.model.api.MediaStreamProtocol
import org.jellyfin.sdk.model.api.ProfileConditionValue
import org.jellyfin.sdk.model.api.SubtitleDeliveryMethod
import org.jellyfin.sdk.model.api.VideoRangeType
import org.jellyfin.sdk.model.deviceprofile.DeviceProfileBuilder
import org.jellyfin.sdk.model.deviceprofile.buildDeviceProfile

internal data class JellyfinAndroidTvProfileConfig(
    val maxBitrate: Int = 100_000_000,
    val isAc3Enabled: Boolean = true,
    val downMixAudio: Boolean = false,
    val assDirectPlay: Boolean = true,
    val pgsDirectPlay: Boolean = true,
)

internal object JellyfinAndroidTvDeviceProfile {
    val fixedConfig = JellyfinAndroidTvProfileConfig()
    private val extendedRangeTypesServerVersion = ServerVersion(10, 11, 0)

    private val downmixSupportedAudioCodecs = arrayOf(
        Codec.Audio.AAC,
        Codec.Audio.MP2,
        Codec.Audio.MP3,
    )

    private val supportedAudioCodecs = arrayOf(
        Codec.Audio.AAC,
        Codec.Audio.AAC_LATM,
        Codec.Audio.AC3,
        Codec.Audio.ALAC,
        Codec.Audio.DCA,
        Codec.Audio.DTS,
        Codec.Audio.EAC3,
        Codec.Audio.FLAC,
        Codec.Audio.MLP,
        Codec.Audio.MP2,
        Codec.Audio.MP3,
        Codec.Audio.OPUS,
        Codec.Audio.PCM_ALAW,
        Codec.Audio.PCM_MULAW,
        Codec.Audio.PCM_S16LE,
        Codec.Audio.PCM_S20LE,
        Codec.Audio.PCM_S24LE,
        Codec.Audio.TRUEHD,
        Codec.Audio.VORBIS,
    )

    private val hlsMpegTsAudioCodecs = arrayOf(
        Codec.Audio.AAC,
        Codec.Audio.AC3,
        Codec.Audio.EAC3,
        Codec.Audio.MP3,
    )

    private val hlsFmp4AudioCodecs = arrayOf(
        Codec.Audio.AAC,
        Codec.Audio.AC3,
        Codec.Audio.EAC3,
        Codec.Audio.MP3,
        Codec.Audio.ALAC,
        Codec.Audio.FLAC,
        Codec.Audio.OPUS,
        Codec.Audio.DTS,
        Codec.Audio.TRUEHD,
    )

    fun create(
        capabilities: DeviceProfileCapabilities,
        serverVersion: ServerVersion,
        config: JellyfinAndroidTvProfileConfig = fixedConfig,
    ): DeviceProfile = buildDeviceProfile {
        val allowedAudioCodecs = when {
            config.downMixAudio -> downmixSupportedAudioCodecs
            !config.isAc3Enabled -> supportedAudioCodecs
                .filterNot { it == Codec.Audio.AC3 || it == Codec.Audio.EAC3 }
                .toTypedArray()
            else -> supportedAudioCodecs
        }

        val supportsHevc = capabilities.supportsHevc()
        val supportsHevcMain10 = capabilities.supportsHevcMain10()
        val hevcMainLevel = capabilities.hevcMainLevel()
        val hevcMain10Level = capabilities.hevcMain10Level()
        val supportsAvc = capabilities.supportsAvc()
        val supportsAvcHigh10 = capabilities.supportsAvcHigh10()
        val avcMainLevel = capabilities.avcMainLevel()
        val avcHigh10Level = capabilities.avcHigh10Level()
        val supportsAv1 = capabilities.supportsAv1()
        val supportsAv1Main10 = capabilities.supportsAv1Main10()
        val supportsVc1 = capabilities.supportsVc1()
        val maxResolutionAvc = capabilities.maxResolution(MimeTypes.VIDEO_H264)
        val maxResolutionHevc = capabilities.maxResolution(MimeTypes.VIDEO_H265)
        val maxResolutionAv1 = capabilities.maxResolution(MimeTypes.VIDEO_AV1)
        val maxResolutionVc1 = capabilities.maxResolution(MimeTypes.VIDEO_VC1)

        val supportsAv1DolbyVision = capabilities.supportsAv1DolbyVision()
        val supportsAv1Hdr10 = capabilities.supportsAv1Hdr10()
        val supportsAv1Hdr10Plus = capabilities.supportsAv1Hdr10Plus()
        val supportsHevcDolbyVision = capabilities.supportsHevcDolbyVision()
        val supportsHevcDolbyVisionEl = capabilities.supportsHevcDolbyVisionEl()
        val supportsHevcHdr10 = capabilities.supportsHevcHdr10()
        val supportsHevcHdr10Plus = capabilities.supportsHevcHdr10Plus()
        val supportsExtendedRangeTypes = serverVersion >= extendedRangeTypesServerVersion

        name = "AndroidTV-Default"
        maxStaticBitrate = config.maxBitrate
        maxStreamingBitrate = config.maxBitrate

        val hlsVideoCodecs = listOfNotNull(
            if (supportsHevc) Codec.Video.HEVC else null,
            Codec.Video.H264,
        ).toTypedArray()

        transcodingProfile {
            type = DlnaProfileType.VIDEO
            context = EncodingContext.STREAMING
            container = Codec.Container.TS
            protocol = MediaStreamProtocol.HLS
            videoCodec(*hlsVideoCodecs)
            audioCodec(*hlsMpegTsAudioCodecs.filter(allowedAudioCodecs::contains).toTypedArray())
            copyTimestamps = false
            enableSubtitlesInManifest = true
        }

        transcodingProfile {
            type = DlnaProfileType.VIDEO
            context = EncodingContext.STREAMING
            container = Codec.Container.MP4
            protocol = MediaStreamProtocol.HLS
            videoCodec(*hlsVideoCodecs)
            audioCodec(*hlsFmp4AudioCodecs.filter(allowedAudioCodecs::contains).toTypedArray())
            copyTimestamps = false
            enableSubtitlesInManifest = true
        }

        transcodingProfile {
            type = DlnaProfileType.AUDIO
            context = EncodingContext.STREAMING
            container = Codec.Container.TS
            protocol = MediaStreamProtocol.HLS
            audioCodec(Codec.Audio.AAC)
        }

        directPlayProfile {
            type = DlnaProfileType.VIDEO
            container(
                Codec.Container.ASF,
                Codec.Container.HLS,
                Codec.Container.M4V,
                Codec.Container.MKV,
                Codec.Container.MOV,
                Codec.Container.MP4,
                Codec.Container.OGM,
                Codec.Container.OGV,
                Codec.Container.TS,
                Codec.Container.VOB,
                Codec.Container.WEBM,
                Codec.Container.WMV,
                Codec.Container.XVID,
            )
            videoCodec(
                Codec.Video.AV1,
                Codec.Video.H264,
                Codec.Video.HEVC,
                Codec.Video.MPEG,
                Codec.Video.MPEG2VIDEO,
                Codec.Video.VC1,
                Codec.Video.VP8,
                Codec.Video.VP9,
            )
            audioCodec(*allowedAudioCodecs)
        }

        directPlayProfile {
            type = DlnaProfileType.AUDIO
            audioCodec(*allowedAudioCodecs)
        }

        codecProfile {
            type = CodecType.VIDEO
            codec = Codec.Video.H264
            conditions {
                when {
                    !supportsAvc -> ProfileConditionValue.VIDEO_PROFILE equals "none"
                    else -> ProfileConditionValue.VIDEO_PROFILE inCollection listOfNotNull(
                        "high",
                        "main",
                        "baseline",
                        "constrained baseline",
                        if (supportsAvcHigh10) "high 10" else null,
                    )
                }
            }
        }
        if (supportsAvc) {
            codecProfile {
                type = CodecType.VIDEO
                codec = Codec.Video.H264
                conditions {
                    ProfileConditionValue.VIDEO_LEVEL lowerThanOrEquals avcMainLevel
                }
                applyConditions {
                    ProfileConditionValue.VIDEO_PROFILE inCollection listOf(
                        "high",
                        "main",
                        "baseline",
                        "constrained baseline",
                    )
                }
            }
        }
        if (supportsAvcHigh10) {
            codecProfile {
                type = CodecType.VIDEO
                codec = Codec.Video.H264
                conditions {
                    ProfileConditionValue.VIDEO_LEVEL lowerThanOrEquals avcHigh10Level
                }
                applyConditions {
                    ProfileConditionValue.VIDEO_PROFILE equals "high 10"
                }
            }
        }

        codecProfile {
            type = CodecType.VIDEO
            codec = Codec.Video.H264
            conditions {
                ProfileConditionValue.REF_FRAMES lowerThanOrEquals 12
            }
            applyConditions {
                ProfileConditionValue.WIDTH greaterThanOrEquals 1200
            }
        }

        codecProfile {
            type = CodecType.VIDEO
            codec = Codec.Video.H264
            conditions {
                ProfileConditionValue.REF_FRAMES lowerThanOrEquals 4
            }
            applyConditions {
                ProfileConditionValue.WIDTH greaterThanOrEquals 1900
            }
        }

        codecProfile {
            type = CodecType.VIDEO
            codec = Codec.Video.HEVC
            conditions {
                when {
                    !supportsHevc -> ProfileConditionValue.VIDEO_PROFILE equals "none"
                    else -> ProfileConditionValue.VIDEO_PROFILE inCollection listOfNotNull(
                        "main",
                        if (supportsHevcMain10) "main 10" else null,
                    )
                }
            }
        }
        if (supportsHevc) {
            codecProfile {
                type = CodecType.VIDEO
                codec = Codec.Video.HEVC
                conditions {
                    ProfileConditionValue.VIDEO_LEVEL lowerThanOrEquals hevcMainLevel
                }
                applyConditions {
                    ProfileConditionValue.VIDEO_PROFILE equals "main"
                }
            }
        }
        if (supportsHevcMain10) {
            codecProfile {
                type = CodecType.VIDEO
                codec = Codec.Video.HEVC
                conditions {
                    ProfileConditionValue.VIDEO_LEVEL lowerThanOrEquals hevcMain10Level
                }
                applyConditions {
                    ProfileConditionValue.VIDEO_PROFILE equals "main 10"
                }
            }
        }

        codecProfile {
            type = CodecType.VIDEO
            codec = Codec.Video.AV1
            conditions {
                when {
                    !supportsAv1 -> ProfileConditionValue.VIDEO_PROFILE equals "none"
                    !supportsAv1Main10 -> ProfileConditionValue.VIDEO_PROFILE notEquals "main 10"
                    else -> ProfileConditionValue.VIDEO_PROFILE notEquals "none"
                }
            }
        }

        codecProfile {
            type = CodecType.VIDEO
            codec = Codec.Video.VC1
            conditions {
                when {
                    !supportsVc1 -> ProfileConditionValue.VIDEO_PROFILE equals "none"
                    else -> ProfileConditionValue.VIDEO_PROFILE notEquals "none"
                }
            }
        }

        codecProfile {
            type = CodecType.VIDEO
            codec = Codec.Video.H264
            conditions {
                ProfileConditionValue.WIDTH lowerThanOrEquals maxResolutionAvc.width
                ProfileConditionValue.HEIGHT lowerThanOrEquals maxResolutionAvc.height
            }
        }

        codecProfile {
            type = CodecType.VIDEO
            codec = Codec.Video.HEVC
            conditions {
                ProfileConditionValue.WIDTH lowerThanOrEquals maxResolutionHevc.width
                ProfileConditionValue.HEIGHT lowerThanOrEquals maxResolutionHevc.height
            }
        }

        codecProfile {
            type = CodecType.VIDEO
            codec = Codec.Video.AV1
            conditions {
                ProfileConditionValue.WIDTH lowerThanOrEquals maxResolutionAv1.width
                ProfileConditionValue.HEIGHT lowerThanOrEquals maxResolutionAv1.height
            }
        }

        codecProfile {
            type = CodecType.VIDEO
            codec = Codec.Video.VC1
            conditions {
                ProfileConditionValue.WIDTH lowerThanOrEquals maxResolutionVc1.width
                ProfileConditionValue.HEIGHT lowerThanOrEquals maxResolutionVc1.height
            }
        }

        val unsupportedRangeTypesAv1 = buildSet {
            addRangeTypeIfSupported(VideoRangeType.DOVI_INVALID, supportsExtendedRangeTypes)
            if (!supportsAv1DolbyVision) {
                add(VideoRangeType.DOVI)
                if (!supportsAv1Hdr10) add(VideoRangeType.DOVI_WITH_HDR10)
                if (!supportsAv1Hdr10Plus) addRangeTypeIfSupported(
                    VideoRangeType.DOVI_WITH_HDR10_PLUS,
                    supportsExtendedRangeTypes,
                )
            }
            if (!supportsAv1Hdr10Plus) {
                add(VideoRangeType.HDR10_PLUS)
                if (!supportsAv1Hdr10) add(VideoRangeType.HDR10)
            }
        }

        val unsupportedRangeTypesHevc = buildSet {
            addRangeTypeIfSupported(VideoRangeType.DOVI_INVALID, supportsExtendedRangeTypes)
            if (!supportsHevcDolbyVisionEl) {
                addRangeTypeIfSupported(VideoRangeType.DOVI_WITH_EL, supportsExtendedRangeTypes)
                if (!supportsHevcHdr10Plus && !capabilities.hevcDoviHdr10PlusBug) {
                    addRangeTypeIfSupported(
                        VideoRangeType.DOVI_WITH_ELHDR10_PLUS,
                        supportsExtendedRangeTypes,
                    )
                }

                if (!supportsHevcDolbyVision) {
                    add(VideoRangeType.DOVI)
                    if (!supportsHevcHdr10) add(VideoRangeType.DOVI_WITH_HDR10)
                    if (!supportsHevcHdr10Plus && !capabilities.hevcDoviHdr10PlusBug) {
                        addRangeTypeIfSupported(
                            VideoRangeType.DOVI_WITH_HDR10_PLUS,
                            supportsExtendedRangeTypes,
                        )
                    }
                }
            }

            if (!supportsHevcHdr10Plus) {
                add(VideoRangeType.HDR10_PLUS)
                if (!supportsHevcHdr10) add(VideoRangeType.HDR10)
            }

            if (capabilities.hevcDoviHdr10PlusBug) {
                addRangeTypeIfSupported(VideoRangeType.DOVI_WITH_HDR10_PLUS, supportsExtendedRangeTypes)
                addRangeTypeIfSupported(VideoRangeType.DOVI_WITH_ELHDR10_PLUS, supportsExtendedRangeTypes)
            }
        }

        if (unsupportedRangeTypesAv1.isNotEmpty()) {
            codecProfile {
                type = CodecType.VIDEO
                codec = Codec.Video.AV1
                conditions {
                    ProfileConditionValue.VIDEO_RANGE_TYPE notEquals unsupportedRangeTypesAv1.joinToString("|") { it.serialName }
                }
                applyConditions {
                    ProfileConditionValue.VIDEO_RANGE_TYPE inCollection unsupportedRangeTypesAv1.map { it.serialName }
                }
            }
        }

        if (unsupportedRangeTypesHevc.isNotEmpty()) {
            codecProfile {
                type = CodecType.VIDEO
                codec = Codec.Video.HEVC
                conditions {
                    ProfileConditionValue.VIDEO_RANGE_TYPE notEquals unsupportedRangeTypesHevc.joinToString("|") { it.serialName }
                }
                applyConditions {
                    ProfileConditionValue.VIDEO_RANGE_TYPE inCollection unsupportedRangeTypesHevc.map { it.serialName }
                }
            }
        }

        codecProfile {
            type = CodecType.VIDEO_AUDIO
            conditions {
                ProfileConditionValue.AUDIO_CHANNELS lowerThanOrEquals if (config.downMixAudio) 2 else 8
            }
        }

        subtitleProfile(Codec.Subtitle.VTT, embedded = true, hls = true, external = true)
        subtitleProfile(Codec.Subtitle.WEBVTT, embedded = true, hls = true, external = true)
        subtitleProfile(Codec.Subtitle.SRT, embedded = true, external = true)
        subtitleProfile(Codec.Subtitle.SUBRIP, embedded = true, external = true)
        subtitleProfile(Codec.Subtitle.TTML, embedded = true, external = true)
        subtitleProfile(Codec.Subtitle.DVBSUB, embedded = true, encode = true)
        subtitleProfile(Codec.Subtitle.DVDSUB, embedded = true, encode = true)
        subtitleProfile(Codec.Subtitle.IDX, embedded = true, encode = true)
        subtitleProfile(Codec.Subtitle.PGS, embedded = config.pgsDirectPlay, encode = true)
        subtitleProfile(Codec.Subtitle.PGSSUB, embedded = config.pgsDirectPlay, encode = true)
        subtitleProfile(
            Codec.Subtitle.ASS,
            encode = true,
            embedded = config.assDirectPlay,
            external = config.assDirectPlay,
        )
        subtitleProfile(
            Codec.Subtitle.SSA,
            encode = true,
            embedded = config.assDirectPlay,
            external = config.assDirectPlay,
        )
    }
}

private fun MutableSet<VideoRangeType>.addRangeTypeIfSupported(
    type: VideoRangeType,
    supportedByServer: Boolean,
) {
    if (supportedByServer) add(type)
}

private fun DeviceProfileBuilder.subtitleProfile(
    format: String,
    embedded: Boolean = false,
    external: Boolean = false,
    hls: Boolean = false,
    encode: Boolean = false,
) {
    if (embedded) subtitleProfile(format, SubtitleDeliveryMethod.EMBED)
    if (external) subtitleProfile(format, SubtitleDeliveryMethod.EXTERNAL)
    if (hls) subtitleProfile(format, SubtitleDeliveryMethod.HLS)
    if (encode) subtitleProfile(format, SubtitleDeliveryMethod.ENCODE)
}

private object Codec {
    object Container {
        const val ASF = "asf"
        const val HLS = "hls"
        const val M4V = "m4v"
        const val MKV = "mkv"
        const val MOV = "mov"
        const val MP4 = "mp4"
        const val OGM = "ogm"
        const val OGV = "ogv"
        const val TS = "ts"
        const val VOB = "vob"
        const val WEBM = "webm"
        const val WMV = "wmv"
        const val XVID = "xvid"
    }

    object Audio {
        const val AAC = "aac"
        const val AAC_LATM = "aac_latm"
        const val AC3 = "ac3"
        const val ALAC = "alac"
        const val DCA = "dca"
        const val DTS = "dts"
        const val EAC3 = "eac3"
        const val FLAC = "flac"
        const val MLP = "mlp"
        const val MP2 = "mp2"
        const val MP3 = "mp3"
        const val OPUS = "opus"
        const val PCM_ALAW = "pcm_alaw"
        const val PCM_MULAW = "pcm_mulaw"
        const val PCM_S16LE = "pcm_s16le"
        const val PCM_S20LE = "pcm_s20le"
        const val PCM_S24LE = "pcm_s24le"
        const val TRUEHD = "truehd"
        const val VORBIS = "vorbis"
    }

    object Video {
        const val AV1 = "av1"
        const val H264 = "h264"
        const val HEVC = "hevc"
        const val MPEG = "mpeg"
        const val MPEG2VIDEO = "mpeg2video"
        const val VC1 = "vc1"
        const val VP8 = "vp8"
        const val VP9 = "vp9"
    }

    object Subtitle {
        const val ASS = "ass"
        const val DVBSUB = "dvbsub"
        const val DVDSUB = "dvdsub"
        const val IDX = "idx"
        const val PGS = "pgs"
        const val PGSSUB = "pgssub"
        const val SRT = "srt"
        const val SSA = "ssa"
        const val SUBRIP = "subrip"
        const val TTML = "ttml"
        const val VTT = "vtt"
        const val WEBVTT = "webvtt"
    }
}
