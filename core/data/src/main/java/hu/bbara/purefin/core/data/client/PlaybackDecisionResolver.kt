package hu.bbara.purefin.core.data.client

import org.jellyfin.sdk.model.api.MediaProtocol
import org.jellyfin.sdk.model.api.MediaSourceInfo
import org.jellyfin.sdk.model.api.PlayMethod

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
            mediaSource.supportsDirectPlay -> PlayMethod.DIRECT_PLAY
            mediaSource.supportsDirectStream && !mediaSource.transcodingUrl.isNullOrBlank() -> PlayMethod.DIRECT_STREAM
            mediaSource.supportsTranscoding && !mediaSource.transcodingUrl.isNullOrBlank() -> PlayMethod.TRANSCODE
            else -> return null
        }

        val url = when (playMethod) {
            PlayMethod.DIRECT_PLAY -> directPlayUrl(mediaSource)
            PlayMethod.DIRECT_STREAM,
            PlayMethod.TRANSCODE,
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
