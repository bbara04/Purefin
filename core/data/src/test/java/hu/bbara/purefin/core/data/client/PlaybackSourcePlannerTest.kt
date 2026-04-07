package hu.bbara.purefin.core.data.client

import org.jellyfin.sdk.model.api.MediaProtocol
import org.jellyfin.sdk.model.api.MediaSourceInfo
import org.jellyfin.sdk.model.api.MediaSourceType
import org.jellyfin.sdk.model.api.MediaStreamProtocol
import org.jellyfin.sdk.model.api.PlayMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackSourcePlannerTest {
    @Test
    fun `transcode-only source is selected for playback`() {
        val mediaSource = mediaSource(id = "transcode-only", supportsTranscoding = true)

        val plan = requireNotNull(
            PlaybackSourcePlanner.plan(
                mediaSources = listOf(mediaSource),
                forceTranscode = false
            )
        )

        assertSame(mediaSource, plan.mediaSource)
        assertEquals(PlaybackSourcePlanner.UrlStrategy.TRANSCODE, plan.urlStrategy)
        assertEquals(PlayMethod.TRANSCODE, plan.playMethod)
        assertFalse(plan.canRetryWithTranscoding)
        assertFalse(plan.usedFallback)
    }

    @Test
    fun `first source falls back to direct stream when Jellyfin flags are inconsistent`() {
        val mediaSource = mediaSource(id = "flagless")

        val plan = requireNotNull(
            PlaybackSourcePlanner.plan(
                mediaSources = listOf(mediaSource),
                forceTranscode = false
            )
        )

        assertSame(mediaSource, plan.mediaSource)
        assertEquals(PlaybackSourcePlanner.UrlStrategy.DIRECT_STREAM, plan.urlStrategy)
        assertEquals(PlayMethod.DIRECT_STREAM, plan.playMethod)
        assertFalse(plan.canRetryWithTranscoding)
        assertTrue(plan.usedFallback)
    }

    @Test
    fun `retry with transcoding stays enabled when another source can transcode`() {
        val directPlaySource = mediaSource(id = "direct-play", supportsDirectPlay = true)
        val transcodeSource = mediaSource(id = "transcode", supportsTranscoding = true)

        val plan = requireNotNull(
            PlaybackSourcePlanner.plan(
                mediaSources = listOf(directPlaySource, transcodeSource),
                forceTranscode = false
            )
        )

        assertSame(directPlaySource, plan.mediaSource)
        assertEquals(PlayMethod.DIRECT_PLAY, plan.playMethod)
        assertTrue(plan.canRetryWithTranscoding)
        assertFalse(plan.usedFallback)
    }

    @Test
    fun `force transcode does not fall back to unusable source`() {
        val mediaSource = mediaSource(id = "flagless")

        val plan = PlaybackSourcePlanner.plan(
            mediaSources = listOf(mediaSource),
            forceTranscode = true
        )

        assertNull(plan)
    }

    private fun mediaSource(
        id: String,
        supportsDirectPlay: Boolean = false,
        supportsDirectStream: Boolean = false,
        supportsTranscoding: Boolean = false,
        container: String? = "mkv"
    ): MediaSourceInfo {
        return MediaSourceInfo(
            protocol = MediaProtocol.FILE,
            id = id,
            type = MediaSourceType.DEFAULT,
            container = container,
            isRemote = false,
            readAtNativeFramerate = true,
            ignoreDts = false,
            ignoreIndex = false,
            genPtsInput = false,
            supportsTranscoding = supportsTranscoding,
            supportsDirectStream = supportsDirectStream,
            supportsDirectPlay = supportsDirectPlay,
            isInfiniteStream = false,
            requiresOpening = false,
            requiresClosing = false,
            requiresLooping = false,
            supportsProbing = true,
            transcodingSubProtocol = MediaStreamProtocol.HTTP,
            hasSegments = false
        )
    }
}
