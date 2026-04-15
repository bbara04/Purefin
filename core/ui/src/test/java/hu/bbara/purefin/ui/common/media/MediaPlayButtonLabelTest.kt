package hu.bbara.purefin.ui.common.media

import org.junit.Assert.assertEquals
import org.junit.Test

class MediaPlayButtonLabelTest {
    @Test
    fun `returns Resume when playback started and item not watched`() {
        assertEquals("Resume", mediaPlayButtonText(progressPercent = 37.5, watched = false))
    }

    @Test
    fun `returns Play when playback missing or item watched`() {
        assertEquals("Play", mediaPlayButtonText(progressPercent = null, watched = false))
        assertEquals("Play", mediaPlayButtonText(progressPercent = 37.5, watched = true))
    }

    @Test
    fun `converts percent to normalized progress`() {
        assertEquals(0.375f, mediaPlaybackProgress(37.5), 0.0001f)
        assertEquals(0f, mediaPlaybackProgress(null), 0.0001f)
    }
}
