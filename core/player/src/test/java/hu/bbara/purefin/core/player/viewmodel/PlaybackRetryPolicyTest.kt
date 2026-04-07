package hu.bbara.purefin.core.player.viewmodel

import androidx.media3.common.PlaybackException
import hu.bbara.purefin.core.data.client.PlaybackReportContext
import hu.bbara.purefin.core.player.model.PlayerError
import hu.bbara.purefin.core.player.model.PlayerErrorSource
import org.jellyfin.sdk.model.api.PlayMethod
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackRetryPolicyTest {
    @Test
    fun `decoder failures are retried when transcoding is available`() {
        val error = PlayerError(
            summary = "Playback error",
            technicalDetail = "Decoder init failed",
            source = PlayerErrorSource.PLAYBACK,
            errorCode = PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
            errorCodeName = "ERROR_CODE_DECODER_INIT_FAILED",
            retryable = true
        )

        val playbackReportContext = playbackReportContext(playMethod = PlayMethod.DIRECT_PLAY)

        assertTrue(PlaybackRetryPolicy.shouldRetryWithTranscoding(error, playbackReportContext))
    }

    @Test
    fun `transcoded playback is not retried again`() {
        val error = PlayerError(
            summary = "Playback error",
            technicalDetail = "Unsupported container",
            source = PlayerErrorSource.PLAYBACK,
            errorCodeName = "ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED",
            retryable = true
        )

        val playbackReportContext = playbackReportContext(playMethod = PlayMethod.TRANSCODE)

        assertFalse(PlaybackRetryPolicy.shouldRetryWithTranscoding(error, playbackReportContext))
    }

    @Test
    fun `load errors are never retried as transcoding fallbacks`() {
        val error = PlayerError.loadFailure(technicalDetail = "No playable source was returned.")

        val playbackReportContext = playbackReportContext(playMethod = PlayMethod.DIRECT_PLAY)

        assertFalse(PlaybackRetryPolicy.shouldRetryWithTranscoding(error, playbackReportContext))
    }

    @Test
    fun `unsupported codec detail is retryable without an error code`() {
        val error = PlayerError(
            summary = "Playback error",
            technicalDetail = "Codec unsupported by this device",
            source = PlayerErrorSource.PLAYBACK
        )

        val playbackReportContext = playbackReportContext(playMethod = PlayMethod.DIRECT_STREAM)

        assertTrue(PlaybackRetryPolicy.shouldRetryWithTranscoding(error, playbackReportContext))
    }

    @Test
    fun `audio track init failures are retried when transcoding is available`() {
        val error = PlayerError(
            summary = "Playback error",
            technicalDetail = "AudioTrack init failed",
            source = PlayerErrorSource.PLAYBACK,
            errorCode = PlaybackException.ERROR_CODE_AUDIO_TRACK_INIT_FAILED,
            errorCodeName = "ERROR_CODE_AUDIO_TRACK_INIT_FAILED",
            retryable = true
        )

        val playbackReportContext = playbackReportContext(playMethod = PlayMethod.DIRECT_PLAY)

        assertTrue(PlaybackRetryPolicy.shouldRetryWithTranscoding(error, playbackReportContext))
    }

    private fun playbackReportContext(playMethod: PlayMethod): PlaybackReportContext {
        return PlaybackReportContext(
            playMethod = playMethod,
            mediaSourceId = "source-id",
            audioStreamIndex = 0,
            subtitleStreamIndex = null,
            liveStreamId = null,
            playSessionId = "session-id",
            canRetryWithTranscoding = true
        )
    }
}
