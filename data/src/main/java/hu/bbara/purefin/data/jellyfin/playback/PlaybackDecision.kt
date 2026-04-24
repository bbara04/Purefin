package hu.bbara.purefin.data.jellyfin.playback

import hu.bbara.purefin.data.PlaybackReportContext
import org.jellyfin.sdk.model.api.MediaSourceInfo

data class PlaybackDecision(
    val url: String,
    val mediaSource: MediaSourceInfo,
    val reportContext: PlaybackReportContext
)
