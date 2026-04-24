package hu.bbara.purefin.core.player.model

import hu.bbara.purefin.data.PlaybackReportContext

data class MetadataState(
    val mediaId: String? = null,
    val title: String? = null,
    val subtitle: String? = null,
    val playbackReportContext: PlaybackReportContext? = null,
)