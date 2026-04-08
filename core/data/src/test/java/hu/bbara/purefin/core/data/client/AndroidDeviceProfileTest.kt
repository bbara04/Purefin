package hu.bbara.purefin.core.data.client

import androidx.media3.common.MimeTypes
import org.jellyfin.sdk.model.ServerVersion
import org.jellyfin.sdk.model.api.CodecType
import org.jellyfin.sdk.model.api.DlnaProfileType
import org.jellyfin.sdk.model.api.MediaStreamProtocol
import org.jellyfin.sdk.model.api.ProfileCondition
import org.jellyfin.sdk.model.api.ProfileConditionType
import org.jellyfin.sdk.model.api.ProfileConditionValue
import org.jellyfin.sdk.model.api.SubtitleDeliveryMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JellyfinAndroidTvDeviceProfileTest {
    @Test
    fun `official defaults build expected profile structure`() {
        val profile = JellyfinAndroidTvDeviceProfile.create(
            capabilities = FakeDeviceProfileCapabilities(),
            serverVersion = ServerVersion(10, 11, 0),
        )

        assertEquals("AndroidTV-Default", profile.name)
        assertEquals(100_000_000, profile.maxStreamingBitrate)
        assertEquals(100_000_000, profile.maxStaticBitrate)
        assertEquals(3, profile.transcodingProfiles.size)

        val videoProfiles = profile.transcodingProfiles.filter { it.type == DlnaProfileType.VIDEO }
        assertEquals(setOf("mp4", "ts"), videoProfiles.map { it.container }.toSet())
        assertTrue(videoProfiles.all { it.protocol == MediaStreamProtocol.HLS })
        assertTrue(videoProfiles.all { it.enableSubtitlesInManifest })

        val audioProfile = profile.transcodingProfiles.single { it.type == DlnaProfileType.AUDIO }
        assertEquals("ts", audioProfile.container)
        assertEquals("aac", audioProfile.audioCodec)

        val videoDirectPlay = profile.directPlayProfiles.single { it.type == DlnaProfileType.VIDEO }
        assertTrue(videoDirectPlay.videoCodec.orEmpty().split(",").contains("vc1"))
        assertTrue(videoDirectPlay.audioCodec.orEmpty().split(",").contains("ac3"))

        assertTrue(
            profile.subtitleProfiles.any {
                it.format == "pgs" && it.method == SubtitleDeliveryMethod.EMBED
            }
        )
        assertTrue(
            profile.subtitleProfiles.any {
                it.format == "webvtt" && it.method == SubtitleDeliveryMethod.HLS
            }
        )
    }

    @Test
    fun `ac3 disabled removes ac3 and eac3 from advertised codecs`() {
        val profile = JellyfinAndroidTvDeviceProfile.create(
            capabilities = FakeDeviceProfileCapabilities(),
            serverVersion = ServerVersion(10, 11, 0),
            config = JellyfinAndroidTvDeviceProfile.fixedConfig.copy(isAc3Enabled = false),
        )

        val directAudioCodecs = profile.directPlayProfiles
            .single { it.type == DlnaProfileType.AUDIO }
            .audioCodec
            .orEmpty()
            .split(",")

        val transcodingAudioCodecs = profile.transcodingProfiles
            .filter { it.type == DlnaProfileType.VIDEO }
            .flatMap { it.audioCodec.orEmpty().split(",") }

        assertFalse(directAudioCodecs.contains("ac3"))
        assertFalse(directAudioCodecs.contains("eac3"))
        assertFalse(transcodingAudioCodecs.contains("ac3"))
        assertFalse(transcodingAudioCodecs.contains("eac3"))
    }

    @Test
    fun `downmix mode limits audio codecs and channel profile`() {
        val profile = JellyfinAndroidTvDeviceProfile.create(
            capabilities = FakeDeviceProfileCapabilities(),
            serverVersion = ServerVersion(10, 11, 0),
            config = JellyfinAndroidTvDeviceProfile.fixedConfig.copy(downMixAudio = true),
        )

        val directAudioCodecs = profile.directPlayProfiles
            .single { it.type == DlnaProfileType.AUDIO }
            .audioCodec
            .orEmpty()
            .split(",")

        assertEquals(listOf("aac", "mp2", "mp3"), directAudioCodecs)

        val audioChannelProfile = profile.codecProfiles.single { it.type == CodecType.VIDEO_AUDIO }
        assertEquals("2", audioChannelProfile.conditions.single().value)
    }

    @Test
    fun `older servers omit extended dolby vision range types`() {
        val capabilities = FakeDeviceProfileCapabilities(
            supportsAv1DolbyVision = false,
            supportsAv1Hdr10 = false,
            supportsAv1Hdr10Plus = false,
            supportsHevcDolbyVision = false,
            supportsHevcDolbyVisionEl = false,
            supportsHevcHdr10 = false,
            supportsHevcHdr10Plus = false,
        )

        val olderServerProfile = JellyfinAndroidTvDeviceProfile.create(
            capabilities = capabilities,
            serverVersion = ServerVersion(10, 10, 0),
        )
        val newerServerProfile = JellyfinAndroidTvDeviceProfile.create(
            capabilities = capabilities,
            serverVersion = ServerVersion(10, 11, 0),
        )

        val olderRangeTypes = codecRangeTypes(olderServerProfile.codecProfiles)
        val newerRangeTypes = codecRangeTypes(newerServerProfile.codecProfiles)

        assertFalse(olderRangeTypes.contains("DOVIWithEL"))
        assertFalse(olderRangeTypes.contains("DOVIWithHDR10Plus"))
        assertFalse(olderRangeTypes.contains("DOVIWithELHDR10Plus"))
        assertFalse(olderRangeTypes.contains("DOVIInvalid"))

        assertTrue(newerRangeTypes.contains("DOVIWithEL"))
        assertTrue(newerRangeTypes.contains("DOVIWithHDR10Plus"))
        assertTrue(newerRangeTypes.contains("DOVIWithELHDR10Plus"))
        assertTrue(newerRangeTypes.contains("DOVIInvalid"))
    }

    @Test
    fun `h264 high 10 is advertised as high 10`() {
        val profile = JellyfinAndroidTvDeviceProfile.create(
            capabilities = FakeDeviceProfileCapabilities(supportsAvcHigh10 = true),
            serverVersion = ServerVersion(10, 11, 0),
        )

        val h264Values = profile.codecProfiles
            .filter { it.codec == "h264" }
            .flatMap { it.conditions + it.applyConditions }
            .mapNotNull(ProfileCondition::value)

        assertTrue(h264Values.any { "high 10" in it })
        assertFalse(h264Values.any { "main 10" in it })
    }

    @Test
    fun `vc1 support condition changes with codec support`() {
        val unsupportedProfile = JellyfinAndroidTvDeviceProfile.create(
            capabilities = FakeDeviceProfileCapabilities(supportsVc1 = false),
            serverVersion = ServerVersion(10, 11, 0),
        )
        val supportedProfile = JellyfinAndroidTvDeviceProfile.create(
            capabilities = FakeDeviceProfileCapabilities(supportsVc1 = true),
            serverVersion = ServerVersion(10, 11, 0),
        )

        val unsupportedCondition = vc1SupportCondition(unsupportedProfile.codecProfiles)
        val supportedCondition = vc1SupportCondition(supportedProfile.codecProfiles)

        assertEquals(ProfileConditionType.EQUALS, unsupportedCondition.condition)
        assertEquals("none", unsupportedCondition.value)
        assertEquals(ProfileConditionType.NOT_EQUALS, supportedCondition.condition)
        assertEquals("none", supportedCondition.value)
    }
}

class JellyfinAndroidMobileDeviceProfileTest {
    @Test
    fun `official mobile defaults build expected profile structure`() {
        val profile = JellyfinAndroidMobileDeviceProfile.create(
            capabilities = FakeDeviceProfileCapabilities(),
        )

        assertEquals("Purefin", profile.name)
        assertEquals(120_000_000, profile.maxStreamingBitrate)
        assertEquals(100_000_000, profile.maxStaticBitrate)
        assertEquals(384_000, profile.musicStreamingTranscodingBitrate)

        val videoProfiles = profile.transcodingProfiles.filter { it.type == DlnaProfileType.VIDEO }
        assertEquals(setOf("mkv", "ts"), videoProfiles.map { it.container }.toSet())
        assertTrue(videoProfiles.all { it.protocol == MediaStreamProtocol.HLS })

        val audioProfile = profile.transcodingProfiles.single { it.type == DlnaProfileType.AUDIO }
        assertEquals("mp3", audioProfile.container)
        assertEquals(MediaStreamProtocol.HTTP, audioProfile.protocol)
        assertEquals("mp3", audioProfile.audioCodec)

        assertTrue(
            profile.directPlayProfiles.any {
                it.type == DlnaProfileType.VIDEO && it.container == "mp4"
            }
        )
        assertTrue(
            profile.subtitleProfiles.any {
                it.format == "pgssub" && it.method == SubtitleDeliveryMethod.EMBED
            }
        )
        assertFalse(
            profile.subtitleProfiles.any {
                it.format == "ass"
            }
        )
    }

    @Test
    fun `mobile profile advertises forced audio codecs even without codec support`() {
        val profile = JellyfinAndroidMobileDeviceProfile.create(
            capabilities = FakeDeviceProfileCapabilities(
                supportedAudioCodecs = emptySet(),
                supportedVideoCodecs = setOf("h264"),
                videoProfiles = mapOf("h264" to setOf("high")),
            ),
        )

        val mkvAudioProfile = profile.directPlayProfiles.single {
            it.type == DlnaProfileType.AUDIO && it.container == "mkv"
        }

        val codecs = mkvAudioProfile.audioCodec.orEmpty().split(",")
        assertTrue(codecs.contains("aac"))
        assertTrue(codecs.contains("ac3"))
        assertTrue(codecs.contains("truehd"))
        assertTrue(codecs.contains("pcm_s16le"))
    }

    @Test
    fun `mobile ass direct play opt in adds ass and ssa subtitles`() {
        val profile = JellyfinAndroidMobileDeviceProfile.create(
            capabilities = FakeDeviceProfileCapabilities(),
            config = JellyfinAndroidMobileDeviceProfile.fixedConfig.copy(assDirectPlay = true),
        )

        assertTrue(
            profile.subtitleProfiles.any {
                it.format == "ass" && it.method == SubtitleDeliveryMethod.EMBED
            }
        )
        assertTrue(
            profile.subtitleProfiles.any {
                it.format == "ssa" && it.method == SubtitleDeliveryMethod.EXTERNAL
            }
        )
    }

    @Test
    fun `mobile profile omits tv hdr range conditions`() {
        val profile = JellyfinAndroidMobileDeviceProfile.create(
            capabilities = FakeDeviceProfileCapabilities(),
        )

        assertFalse(
            profile.codecProfiles.any { codecProfile ->
                (codecProfile.conditions + codecProfile.applyConditions).any {
                    it.property == ProfileConditionValue.VIDEO_RANGE_TYPE
                }
            }
        )
    }
}

private fun codecRangeTypes(codecProfiles: List<org.jellyfin.sdk.model.api.CodecProfile>): Set<String> =
    codecProfiles
        .filter { it.propertyValuesContain(ProfileConditionValue.VIDEO_RANGE_TYPE) }
        .flatMap { (it.conditions + it.applyConditions).mapNotNull(ProfileCondition::value) }
        .flatMap { it.split("|") }
        .toSet()

private fun vc1SupportCondition(codecProfiles: List<org.jellyfin.sdk.model.api.CodecProfile>): ProfileCondition =
    codecProfiles
        .single {
            it.codec == "vc1" && it.conditions.any { condition ->
                condition.property == ProfileConditionValue.VIDEO_PROFILE
            }
        }
        .conditions
        .single()

private fun org.jellyfin.sdk.model.api.CodecProfile.propertyValuesContain(value: ProfileConditionValue): Boolean =
    (conditions + applyConditions).any { it.property == value }

private data class FakeDeviceProfileCapabilities(
    val supportsAv1: Boolean = true,
    val supportsAv1Main10: Boolean = true,
    val supportsAv1DolbyVision: Boolean = true,
    val supportsAv1Hdr10: Boolean = true,
    val supportsAv1Hdr10Plus: Boolean = true,
    val supportsAvc: Boolean = true,
    val supportsAvcHigh10: Boolean = false,
    val avcMainLevel: Int = 41,
    val avcHigh10Level: Int = 41,
    val supportsHevc: Boolean = true,
    val supportsHevcMain10: Boolean = true,
    val supportsHevcDolbyVision: Boolean = true,
    val supportsHevcDolbyVisionEl: Boolean = true,
    val supportsHevcHdr10: Boolean = true,
    val supportsHevcHdr10Plus: Boolean = true,
    val hevcMainLevel: Int = 123,
    val hevcMain10Level: Int = 123,
    val supportsVc1: Boolean = true,
    val maxResolutionAvc: ProfileResolution = ProfileResolution(3840, 2160),
    val maxResolutionHevc: ProfileResolution = ProfileResolution(3840, 2160),
    val maxResolutionAv1: ProfileResolution = ProfileResolution(3840, 2160),
    val maxResolutionVc1: ProfileResolution = ProfileResolution(1920, 1080),
    val supportedVideoCodecs: Set<String> = setOf(
        "h263",
        "mpeg2video",
        "mpeg4",
        "h264",
        "hevc",
        "vp8",
        "vp9",
        "av1",
    ),
    val supportedAudioCodecs: Set<String> = setOf(
        "aac",
        "ac3",
        "eac3",
        "flac",
        "mp3",
        "opus",
        "vorbis",
        "3gpp",
    ),
    val videoProfiles: Map<String, Set<String>> = mapOf(
        "h263" to setOf("baseline"),
        "mpeg2video" to setOf("main profile"),
        "mpeg4" to setOf("simple profile"),
        "h264" to setOf("high", "main", "baseline"),
        "hevc" to setOf("Main", "Main 10"),
        "vp8" to setOf("main"),
        "vp9" to setOf("Profile 0"),
    ),
    override val hevcDoviHdr10PlusBug: Boolean = false,
) : DeviceProfileCapabilities {
    override fun supportsAv1(): Boolean = supportsAv1
    override fun supportsAv1Main10(): Boolean = supportsAv1Main10
    override fun supportsAv1DolbyVision(): Boolean = supportsAv1DolbyVision
    override fun supportsAv1Hdr10(): Boolean = supportsAv1Hdr10
    override fun supportsAv1Hdr10Plus(): Boolean = supportsAv1Hdr10Plus
    override fun supportsAvc(): Boolean = supportsAvc
    override fun supportsAvcHigh10(): Boolean = supportsAvcHigh10
    override fun avcMainLevel(): Int = avcMainLevel
    override fun avcHigh10Level(): Int = avcHigh10Level
    override fun supportsHevc(): Boolean = supportsHevc
    override fun supportsHevcMain10(): Boolean = supportsHevcMain10
    override fun supportsHevcDolbyVision(): Boolean = supportsHevcDolbyVision
    override fun supportsHevcDolbyVisionEl(): Boolean = supportsHevcDolbyVisionEl
    override fun supportsHevcHdr10(): Boolean = supportsHevcHdr10
    override fun supportsHevcHdr10Plus(): Boolean = supportsHevcHdr10Plus
    override fun hevcMainLevel(): Int = hevcMainLevel
    override fun hevcMain10Level(): Int = hevcMain10Level
    override fun supportsVc1(): Boolean = supportsVc1

    override fun maxResolution(mimeType: String): ProfileResolution = when (mimeType) {
        MimeTypes.VIDEO_H264 -> maxResolutionAvc
        MimeTypes.VIDEO_H265 -> maxResolutionHevc
        MimeTypes.VIDEO_AV1 -> maxResolutionAv1
        MimeTypes.VIDEO_VC1 -> maxResolutionVc1
        else -> ProfileResolution(0, 0)
    }

    override fun supportsVideoCodec(codec: String): Boolean = supportedVideoCodecs.contains(codec)

    override fun supportsAudioCodec(codec: String): Boolean = supportedAudioCodecs.contains(codec)

    override fun videoCodecProfiles(codec: String): Set<String> = videoProfiles[codec].orEmpty()
}
