package hu.bbara.purefin.core.player.model

import hu.bbara.purefin.core.model.MediaSegment
import hu.bbara.purefin.core.player.preference.MediaTrackPreferences

data class MediaContext(
    val mediaId: String,
    val preferences: MediaTrackPreferences,
    val mediaSegments: List<MediaSegment>
)