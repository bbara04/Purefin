package hu.bbara.purefin.core.player.viewmodel

import androidx.media3.common.PlaybackException
import hu.bbara.purefin.core.data.client.PlaybackReportContext
import hu.bbara.purefin.core.player.model.PlayerError
import hu.bbara.purefin.core.player.model.PlayerErrorSource
import org.jellyfin.sdk.model.api.PlayMethod

internal object PlaybackRetryPolicy {
    private val retryableErrorCodes = setOf(
        PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
        PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED,
        PlaybackException.ERROR_CODE_AUDIO_TRACK_INIT_FAILED,
        PlaybackException.ERROR_CODE_AUDIO_TRACK_OFFLOAD_INIT_FAILED,
        PlaybackException.ERROR_CODE_AUDIO_TRACK_WRITE_FAILED,
        PlaybackException.ERROR_CODE_AUDIO_TRACK_OFFLOAD_WRITE_FAILED,
        PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED,
        PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED
    )

    fun shouldRetryWithTranscoding(
        error: PlayerError,
        playbackReportContext: PlaybackReportContext
    ): Boolean {
        if (error.source != PlayerErrorSource.PLAYBACK) {
            return false
        }
        if (!playbackReportContext.canRetryWithTranscoding) {
            return false
        }
        if (playbackReportContext.playMethod == PlayMethod.TRANSCODE) {
            return false
        }

        val errorCode = error.errorCode
        if (errorCode != null && errorCode in retryableErrorCodes) {
            return true
        }

        val detail = buildString {
            append(error.summary)
            error.errorCodeName?.let {
                append(' ')
                append(it)
            }
            error.detailText?.let {
                append(' ')
                append(it)
            }
        }.lowercase()

        return "decoder" in detail ||
            "codec" in detail ||
            "unsupported" in detail ||
            "audiotrack" in detail ||
            "audio sink" in detail
    }
}
