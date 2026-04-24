package hu.bbara.purefin.player.model

import hu.bbara.purefin.model.MediaSegment
import hu.bbara.purefin.player.preference.MediaTrackPreferences

data class MediaContext(
    val mediaId: String,
    val preferences: MediaTrackPreferences,
    val mediaSegments: List<MediaSegment>
)