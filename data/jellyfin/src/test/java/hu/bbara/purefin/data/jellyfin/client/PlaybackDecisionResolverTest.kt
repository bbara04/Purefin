package hu.bbara.purefin.data.jellyfin.client

import hu.bbara.purefin.core.data.PlaybackMethod
import org.jellyfin.sdk.model.api.MediaProtocol
import org.jellyfin.sdk.model.api.MediaSourceInfo
import org.jellyfin.sdk.model.api.MediaSourceType
import org.jellyfin.sdk.model.api.MediaStreamProtocol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class PlaybackDecisionResolverTest {
    @Test
    fun `resolve prefers local file-backed source for direct play`() {
        val remoteSource = mediaSource(
            id = "remote",
            protocol = MediaProtocol.HTTP,
            isRemote = true,
            supportsDirectPlay = true,
        )
        val localSource = mediaSource(
            id = "local",
            supportsDirectPlay = true,
            defaultAudioStreamIndex = 2,
            defaultSubtitleStreamIndex = 4,
            liveStreamId = "live-1",
        )

        val decision = requireNotNull(
            PlaybackDecisionResolver.resolve(
                mediaSources = listOf(remoteSource, localSource),
                playSessionId = "session-1",
                serverUrl = "https://example.com/jellyfin",
            ) { mediaSource ->
                "direct://${mediaSource.id}"
            }
        )

        assertSame(localSource, decision.mediaSource)
        assertEquals("direct://local", decision.url)
        assertEquals(PlaybackMethod.DIRECT_PLAY, decision.reportContext.playMethod)
        assertEquals("local", decision.reportContext.mediaSourceId)
        assertEquals(2, decision.reportContext.audioStreamIndex)
        assertEquals(4, decision.reportContext.subtitleStreamIndex)
        assertEquals("live-1", decision.reportContext.liveStreamId)
        assertEquals("session-1", decision.reportContext.playSessionId)
    }

    @Test
    fun `resolve uses transcoding url for direct stream`() {
        val source = mediaSource(
            id = "direct-stream",
            supportsDirectStream = true,
            transcodingUrl = "/Videos/1/master.m3u8",
        )

        val decision = requireNotNull(
            PlaybackDecisionResolver.resolve(
                mediaSources = listOf(source),
                playSessionId = "session-2",
                serverUrl = "https://example.com/jellyfin",
            ) { error("direct play should not be used") }
        )

        assertEquals(PlaybackMethod.DIRECT_STREAM, decision.reportContext.playMethod)
        assertEquals("https://example.com/jellyfin/Videos/1/master.m3u8", decision.url)
    }

    @Test
    fun `resolve uses transcoding when direct stream is unavailable`() {
        val source = mediaSource(
            id = "transcode",
            supportsTranscoding = true,
            transcodingUrl = "Videos/2/master.m3u8",
        )

        val decision = requireNotNull(
            PlaybackDecisionResolver.resolve(
                mediaSources = listOf(source),
                playSessionId = "session-3",
                serverUrl = "https://example.com/jellyfin",
            ) { error("direct play should not be used") }
        )

        assertEquals(PlaybackMethod.TRANSCODE, decision.reportContext.playMethod)
        assertEquals("https://example.com/jellyfin/Videos/2/master.m3u8", decision.url)
    }

    @Test
    fun `resolve returns null when no usable source exists`() {
        val source = mediaSource(id = "unusable")

        val decision = PlaybackDecisionResolver.resolve(
            mediaSources = listOf(source),
            playSessionId = "session-4",
            serverUrl = "https://example.com/jellyfin",
        ) { error("direct play should not be used") }

        assertNull(decision)
    }

    @Test
    fun `absolute playback url preserves absolute urls`() {
        val url = PlaybackDecisionResolver.absolutePlaybackUrl(
            serverUrl = "https://example.com/jellyfin",
            playbackUrl = "https://cdn.example.com/master.m3u8",
        )

        assertEquals("https://cdn.example.com/master.m3u8", url)
    }

    private fun mediaSource(
        id: String,
        protocol: MediaProtocol = MediaProtocol.FILE,
        isRemote: Boolean = false,
        supportsDirectPlay: Boolean = false,
        supportsDirectStream: Boolean = false,
        supportsTranscoding: Boolean = false,
        transcodingUrl: String? = null,
        defaultAudioStreamIndex: Int? = null,
        defaultSubtitleStreamIndex: Int? = null,
        liveStreamId: String? = null,
    ): MediaSourceInfo {
        return MediaSourceInfo(
            protocol = protocol,
            id = id,
            type = MediaSourceType.DEFAULT,
            container = "mkv",
            isRemote = isRemote,
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
            liveStreamId = liveStreamId,
            requiresLooping = false,
            supportsProbing = true,
            transcodingUrl = transcodingUrl,
            defaultAudioStreamIndex = defaultAudioStreamIndex,
            defaultSubtitleStreamIndex = defaultSubtitleStreamIndex,
            transcodingSubProtocol = MediaStreamProtocol.HLS,
            hasSegments = false,
        )
    }
}
