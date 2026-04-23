package hu.bbara.purefin.data.jellyfin.playback

import org.jellyfin.sdk.model.api.CodecProfile
import org.jellyfin.sdk.model.api.CodecType
import org.jellyfin.sdk.model.api.ContainerProfile
import org.jellyfin.sdk.model.api.DeviceProfile
import org.jellyfin.sdk.model.api.DirectPlayProfile
import org.jellyfin.sdk.model.api.DlnaProfileType
import org.jellyfin.sdk.model.api.MediaStreamProtocol
import org.jellyfin.sdk.model.api.ProfileCondition
import org.jellyfin.sdk.model.api.ProfileConditionType
import org.jellyfin.sdk.model.api.ProfileConditionValue
import org.jellyfin.sdk.model.api.SubtitleDeliveryMethod
import org.jellyfin.sdk.model.api.SubtitleProfile
import org.jellyfin.sdk.model.api.TranscodingProfile

internal data class JellyfinAndroidMobileProfileConfig(
    val assDirectPlay: Boolean = false,
)

internal object JellyfinAndroidMobileDeviceProfile {
    val fixedConfig = JellyfinAndroidMobileProfileConfig()

    private const val profileName = "Purefin"
    private const val maxStreamingBitrate = 120_000_000
    private const val maxStaticBitrate = 100_000_000
    private const val maxMusicTranscodingBitrate = 384_000

    private val supportedContainerFormats = arrayOf(
        "mp4", "fmp4", "webm", "mkv", "mp3", "ogg", "wav", "mpegts", "flv", "aac", "flac", "3gp",
    )

    private val availableVideoCodecs = arrayOf(
        arrayOf("mpeg1video", "mpeg2video", "h263", "mpeg4", "h264", "hevc", "av1", "vp9"),
        arrayOf("mpeg1video", "mpeg2video", "h263", "mpeg4", "h264", "hevc", "av1", "vp9"),
        arrayOf("vp8", "vp9", "av1"),
        arrayOf("mpeg1video", "mpeg2video", "h263", "mpeg4", "h264", "hevc", "av1", "vp8", "vp9"),
        emptyArray(),
        emptyArray(),
        emptyArray(),
        arrayOf("mpeg1video", "mpeg2video", "mpeg4", "h264", "hevc"),
        arrayOf("mpeg4", "h264"),
        emptyArray(),
        emptyArray(),
        arrayOf("h263", "mpeg4", "h264", "hevc"),
    )

    private val pcmCodecs = arrayOf(
        "pcm_s8",
        "pcm_s16be",
        "pcm_s16le",
        "pcm_s24le",
        "pcm_s32le",
        "pcm_f32le",
        "pcm_alaw",
        "pcm_mulaw",
    )

    private val availableAudioCodecs = arrayOf(
        arrayOf("mp1", "mp2", "mp3", "aac", "alac", "ac3", "opus"),
        arrayOf("mp3", "aac", "ac3", "eac3"),
        arrayOf("vorbis", "opus"),
        arrayOf(*pcmCodecs, "mp1", "mp2", "mp3", "aac", "vorbis", "opus", "flac", "alac", "ac3", "eac3", "dts", "mlp", "truehd"),
        arrayOf("mp3"),
        arrayOf("vorbis", "opus", "flac"),
        pcmCodecs,
        arrayOf(*pcmCodecs, "mp1", "mp2", "mp3", "aac", "ac3", "eac3", "dts", "mlp", "truehd"),
        arrayOf("mp3", "aac"),
        arrayOf("aac"),
        arrayOf("flac"),
        arrayOf("3gpp", "aac", "flac"),
    )

    private val forcedAudioCodecs = setOf(*pcmCodecs, "alac", "aac", "ac3", "eac3", "dts", "mlp", "truehd")

    private val exoEmbeddedSubtitles = arrayOf("dvbsub", "pgssub", "srt", "subrip", "ttml")
    private val exoExternalSubtitles = arrayOf("srt", "subrip", "ttml", "vtt", "webvtt")
    private val ssaSubtitles = arrayOf("ssa", "ass")

    private val transcodingProfiles = listOf(
        TranscodingProfile(
            type = DlnaProfileType.VIDEO,
            container = "ts",
            videoCodec = "h264",
            audioCodec = "mp1,mp2,mp3,aac,ac3,eac3,dts,mlp,truehd",
            protocol = MediaStreamProtocol.HLS,
            conditions = emptyList(),
        ),
        TranscodingProfile(
            type = DlnaProfileType.VIDEO,
            container = "mkv",
            videoCodec = "h264",
            audioCodec = availableAudioCodecs[supportedContainerFormats.indexOf("mkv")].joinToString(","),
            protocol = MediaStreamProtocol.HLS,
            conditions = emptyList(),
        ),
        TranscodingProfile(
            type = DlnaProfileType.AUDIO,
            container = "mp3",
            videoCodec = "",
            audioCodec = "mp3",
            protocol = MediaStreamProtocol.HTTP,
            conditions = emptyList(),
        ),
    )

    fun create(
        capabilities: DeviceProfileCapabilities,
        config: JellyfinAndroidMobileProfileConfig = fixedConfig,
    ): DeviceProfile {
        val containerProfiles = mutableListOf<ContainerProfile>()
        val directPlayProfiles = mutableListOf<DirectPlayProfile>()
        val codecProfiles = mutableListOf<CodecProfile>()

        for (i in supportedContainerFormats.indices) {
            val container = supportedContainerFormats[i]
            val supportedVideoCodecs = availableVideoCodecs[i]
                .filter(capabilities::supportsVideoCodec)
                .toTypedArray()
            val supportedAudioCodecs = availableAudioCodecs[i]
                .filter { capabilities.supportsAudioCodec(it) || it in forcedAudioCodecs }
                .toTypedArray()

            if (supportedVideoCodecs.isNotEmpty()) {
                containerProfiles += ContainerProfile(
                    type = DlnaProfileType.VIDEO,
                    container = container,
                    conditions = emptyList(),
                )
                directPlayProfiles += DirectPlayProfile(
                    type = DlnaProfileType.VIDEO,
                    container = container,
                    videoCodec = supportedVideoCodecs.joinToString(","),
                    audioCodec = supportedAudioCodecs.joinToString(","),
                )
                supportedVideoCodecs.mapNotNullTo(codecProfiles) { videoCodec ->
                    generateCodecProfile(container, videoCodec, capabilities.videoCodecProfiles(videoCodec))
                }
            }

            if (supportedAudioCodecs.isNotEmpty()) {
                containerProfiles += ContainerProfile(
                    type = DlnaProfileType.AUDIO,
                    container = container,
                    conditions = emptyList(),
                )
                directPlayProfiles += DirectPlayProfile(
                    type = DlnaProfileType.AUDIO,
                    container = container,
                    audioCodec = supportedAudioCodecs.joinToString(","),
                )
            }
        }

        val subtitleProfiles = when {
            config.assDirectPlay -> subtitleProfiles(
                embedded = exoEmbeddedSubtitles + ssaSubtitles,
                external = exoExternalSubtitles + ssaSubtitles,
            )
            else -> subtitleProfiles(
                embedded = exoEmbeddedSubtitles,
                external = exoExternalSubtitles,
            )
        }

        return DeviceProfile(
            name = profileName,
            directPlayProfiles = directPlayProfiles,
            transcodingProfiles = transcodingProfiles,
            containerProfiles = containerProfiles,
            codecProfiles = codecProfiles,
            subtitleProfiles = subtitleProfiles,
            maxStreamingBitrate = maxStreamingBitrate,
            maxStaticBitrate = maxStaticBitrate,
            musicStreamingTranscodingBitrate = maxMusicTranscodingBitrate,
        )
    }

    private fun generateCodecProfile(
        container: String,
        videoCodec: String,
        profiles: Set<String>,
    ): CodecProfile? {
        if (profiles.isEmpty()) return null

        return CodecProfile(
            type = CodecType.VIDEO,
            container = container,
            codec = videoCodec,
            applyConditions = emptyList(),
            conditions = listOf(
                ProfileCondition(
                    condition = ProfileConditionType.EQUALS_ANY,
                    property = ProfileConditionValue.VIDEO_PROFILE,
                    value = profiles.joinToString("|"),
                    isRequired = false,
                ),
            ),
        )
    }

    private fun subtitleProfiles(
        embedded: Array<String>,
        external: Array<String>,
    ): List<SubtitleProfile> = buildList {
        embedded.forEach { format ->
            add(SubtitleProfile(format = format, method = SubtitleDeliveryMethod.EMBED))
        }
        external.forEach { format ->
            add(SubtitleProfile(format = format, method = SubtitleDeliveryMethod.EXTERNAL))
        }
    }
}
