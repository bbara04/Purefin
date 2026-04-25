package hu.bbara.purefin.model

import androidx.media3.common.MediaItem
import java.util.UUID

sealed class PlayableMedia {
    abstract val id: UUID
    abstract val resumePositionMs: Long
    abstract val mediaItem: MediaItem
    abstract val preferences: MediaTrackPreferences
    abstract val mediaSegments: List<MediaSegment>

    data class Movie(
        override val id: UUID,
        override val resumePositionMs: Long,
        override val mediaItem: MediaItem,
        override val preferences: MediaTrackPreferences,
        override val mediaSegments: List<MediaSegment>
    ) : PlayableMedia()

    data class Series(
        override val id: UUID,
        override val resumePositionMs: Long,
        override val mediaItem: MediaItem,
        override val preferences: MediaTrackPreferences,
        override val mediaSegments: List<MediaSegment>
    ) : PlayableMedia()

    data class Episode(
        override val id: UUID,
        override val resumePositionMs: Long,
        override val mediaItem: MediaItem,
        override val preferences: MediaTrackPreferences,
        override val mediaSegments: List<MediaSegment>
    ) : PlayableMedia()
}
