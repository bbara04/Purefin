package hu.bbara.purefin.data

data class PlaybackReportContext(
    val playMethod: PlaybackMethod,
    val mediaSourceId: String?,
    val audioStreamIndex: Int?,
    val subtitleStreamIndex: Int?,
    val liveStreamId: String?,
    val playSessionId: String?,
)
