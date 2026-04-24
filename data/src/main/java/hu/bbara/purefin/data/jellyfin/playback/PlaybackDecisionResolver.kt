package hu.bbara.purefin.data.jellyfin.playback

import hu.bbara.purefin.data.PlaybackMethod
import hu.bbara.purefin.data.PlaybackReportContext
import org.jellyfin.sdk.model.api.MediaProtocol
import org.jellyfin.sdk.model.api.MediaSourceInfo

internal object PlaybackDecisionResolver {
    fun resolve(
        mediaSources: List<MediaSourceInfo>,
        playSessionId: String?,
        serverUrl: String,
        directPlayUrl: (MediaSourceInfo) -> String,
    ): PlaybackDecision? {
        val mediaSource = mediaSources.firstOrNull { it.protocol == MediaProtocol.FILE && !it.isRemote }
            ?: return null

        val playMethod = when {
            mediaSource.supportsDirectPlay -> PlaybackMethod.DIRECT_PLAY
            mediaSource.supportsDirectStream && !mediaSource.transcodingUrl.isNullOrBlank() -> PlaybackMethod.DIRECT_STREAM
            mediaSource.supportsTranscoding && !mediaSource.transcodingUrl.isNullOrBlank() -> PlaybackMethod.TRANSCODE
            else -> return null
        }

        val url = when (playMethod) {
            PlaybackMethod.DIRECT_PLAY -> directPlayUrl(mediaSource)
            PlaybackMethod.DIRECT_STREAM,
            PlaybackMethod.TRANSCODE,
            -> absolutePlaybackUrl(serverUrl, requireNotNull(mediaSource.transcodingUrl))
        }

        return PlaybackDecision(
            url = url,
            mediaSource = mediaSource,
            reportContext = PlaybackReportContext(
                playMethod = playMethod,
                mediaSourceId = mediaSource.id,
                audioStreamIndex = mediaSource.defaultAudioStreamIndex,
                subtitleStreamIndex = mediaSource.defaultSubtitleStreamIndex,
                liveStreamId = mediaSource.liveStreamId,
                playSessionId = playSessionId,
            ),
        )
    }

    fun absolutePlaybackUrl(serverUrl: String, playbackUrl: String): String = when {
        playbackUrl.startsWith("http://", ignoreCase = true) -> playbackUrl
        playbackUrl.startsWith("https://", ignoreCase = true) -> playbackUrl
        playbackUrl.startsWith("/") -> "${serverUrl.trimEnd('/')}$playbackUrl"
        else -> "${serverUrl.trimEnd('/')}/${playbackUrl.trimStart('/')}"
    }
}
