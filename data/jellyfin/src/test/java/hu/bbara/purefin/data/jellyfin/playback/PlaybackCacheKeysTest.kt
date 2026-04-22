package hu.bbara.purefin.data.jellyfin.playback

import hu.bbara.purefin.core.data.PlaybackMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlaybackCacheKeysTest {

    @Test
    fun `progressive direct play gets stable cache key`() {
        assertEquals(
            "movie-1",
            playbackCustomCacheKey(
                mediaId = "movie-1",
                playbackUrl = "https://example.com/Videos/movie-1.mp4",
                playMethod = PlaybackMethod.DIRECT_PLAY
            )
        )
    }

    @Test
    fun `adaptive direct stream skips stable cache key`() {
        assertNull(
            playbackCustomCacheKey(
                mediaId = "episode-1",
                playbackUrl = "https://example.com/Videos/episode-1/master.m3u8",
                playMethod = PlaybackMethod.DIRECT_STREAM
            )
        )
    }

    @Test
    fun `manifest-looking direct play still skips stable cache key`() {
        assertNull(
            playbackCustomCacheKey(
                mediaId = "episode-2",
                playbackUrl = "https://example.com/Videos/episode-2/master.m3u8",
                playMethod = PlaybackMethod.DIRECT_PLAY
            )
        )
    }
}
