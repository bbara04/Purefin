package hu.bbara.purefin.core.data.client

import org.jellyfin.sdk.model.api.DlnaProfileType
import org.jellyfin.sdk.model.api.EncodingContext
import org.jellyfin.sdk.model.api.MediaStreamProtocol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidDeviceProfileTest {
    @Test
    fun `device profile includes explicit video transcoding support`() {
        val profile = AndroidDeviceProfile.createDeviceProfile(
            audioCodecs = listOf("aac", "ac3"),
            videoCodecs = listOf("h264", "hevc"),
            maxAudioChannels = 6
        )

        val transcodingProfile = profile.transcodingProfiles.single()

        assertEquals(DlnaProfileType.VIDEO, transcodingProfile.type)
        assertEquals("ts", transcodingProfile.container)
        assertEquals("h264", transcodingProfile.videoCodec)
        assertEquals("aac", transcodingProfile.audioCodec)
        assertEquals(MediaStreamProtocol.HLS, transcodingProfile.protocol)
        assertEquals(EncodingContext.STREAMING, transcodingProfile.context)
        assertEquals("6", transcodingProfile.maxAudioChannels)
        assertEquals(2, transcodingProfile.minSegments)
        assertTrue(transcodingProfile.breakOnNonKeyFrames)
    }
}
