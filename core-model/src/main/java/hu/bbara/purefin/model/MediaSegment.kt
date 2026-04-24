package hu.bbara.purefin.model

import java.util.UUID

enum class SegmentType(val value: Int) {
    INTRO(0),
    OUTRO(1),
    MAIN_CONTENT(2),
    PREVIEW(3),
    RECAP(4);

    companion object {
        fun fromValue(value: Int): SegmentType? =
            entries.firstOrNull { it.value == value }
    }
}

data class MediaSegment(
    val id: UUID,
    val type: SegmentType,
    val startMs: Long,
    val endMs: Long
)
