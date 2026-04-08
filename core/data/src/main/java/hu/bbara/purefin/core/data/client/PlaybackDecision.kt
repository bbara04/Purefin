package hu.bbara.purefin.core.data.client

import org.jellyfin.sdk.model.api.MediaSourceInfo
import org.jellyfin.sdk.model.api.PlayMethod

data class PlaybackDecision(
    val url: String,
    val mediaSource: MediaSourceInfo,
    val reportContext: PlaybackReportContext
)

data class PlaybackReportContext(
    val playMethod: PlayMethod,
    val mediaSourceId: String?,
    val audioStreamIndex: Int?,
    val subtitleStreamIndex: Int?,
    val liveStreamId: String?,
    val playSessionId: String?,
)
