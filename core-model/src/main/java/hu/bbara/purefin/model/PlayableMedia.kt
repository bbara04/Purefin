package hu.bbara.purefin.model

import androidx.media3.common.MediaItem
import java.util.UUID

data class PlayableMedia (
    val id: UUID,
    val resumePositionMs: Float,
    val mediaItem: MediaItem,
    val preferences: MediaTrackPreferences,
    val mediaSegments: List<MediaSegment>
)