package hu.bbara.purefin.core.player.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerErrorTest {
    @Test
    fun `playbackFailure preserves code and detail`() {
        val playerError = PlayerError.playbackFailure(
            errorCode = 4001,
            errorCodeName = "ERROR_CODE_DECODER_INIT_FAILED",
            technicalDetail = "Decoder init failed on test device"
        )

        assertEquals("Playback error", playerError.summary)
        assertEquals(PlayerErrorSource.PLAYBACK, playerError.source)
        assertEquals(4001, playerError.errorCode)
        assertEquals("ERROR_CODE_DECODER_INIT_FAILED", playerError.errorCodeName)
        assertNotNull(playerError.detailText)
        assertTrue(playerError.detailText!!.contains("ERROR_CODE_DECODER_INIT_FAILED"))
        assertTrue(playerError.detailText!!.contains("Decoder init failed on test device"))
    }

    @Test
    fun `withAdditionalTechnicalDetail appends distinct detail`() {
        val playerError = PlayerError.loadFailure(technicalDetail = "IllegalStateException: boom")

        val mergedError = playerError.withAdditionalTechnicalDetail(
            "Offline fallback unavailable: no completed download."
        )

        assertTrue(mergedError.detailText!!.contains("IllegalStateException: boom"))
        assertTrue(mergedError.detailText!!.contains("Offline fallback unavailable: no completed download."))
    }

    @Test
    fun `invalidMediaId is a non retryable load error`() {
        val playerError = PlayerError.invalidMediaId("abc")

        assertEquals("Invalid media id", playerError.summary)
        assertEquals(PlayerErrorSource.LOAD, playerError.source)
        assertFalse(playerError.retryable)
        assertTrue(playerError.detailText!!.contains("abc"))
    }
}
