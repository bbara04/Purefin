package hu.bbara.purefin.core.data.client

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class JellyfinApiClientTest {
    @Test
    fun `direct play requests do not apply HLS stability options`() {
        val options = JellyfinApiClient.streamingHlsOptionsFor(PlaybackSourcePlanner.UrlStrategy.DIRECT_PLAY)

        assertNull(options)
    }

    @Test
    fun `direct stream requests use conservative HLS stability options`() {
        val options = JellyfinApiClient.streamingHlsOptionsFor(PlaybackSourcePlanner.UrlStrategy.DIRECT_STREAM)

        assertEquals(HlsPlaybackStability.conservativeHlsOptions, options)
    }

    @Test
    fun `transcode requests use the same HLS stability options as direct stream`() {
        val directStreamOptions = JellyfinApiClient.streamingHlsOptionsFor(PlaybackSourcePlanner.UrlStrategy.DIRECT_STREAM)
        val transcodeOptions = JellyfinApiClient.streamingHlsOptionsFor(PlaybackSourcePlanner.UrlStrategy.TRANSCODE)

        assertEquals(directStreamOptions, transcodeOptions)
    }
}
