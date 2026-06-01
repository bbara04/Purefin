package hu.bbara.purefin.data.jellyfin.playback

import hu.bbara.purefin.core.data.PlaybackReportContext
import org.jellyfin.sdk.model.api.MediaSourceInfo

data class PlaybackSource(
    val mediaSource: MediaSourceInfo,
    val directPlayUrl: String,
    val transcodingUrl: String?,
    val playbackReportContext: PlaybackReportContext,
)