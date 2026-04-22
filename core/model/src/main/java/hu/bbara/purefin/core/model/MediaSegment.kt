package hu.bbara.purefin.core.model

import java.util.UUID

enum class SegmentType {
    INTRO,
    OUTRO,
    MAIN_CONTENT,
    PREVIEW,
    RECAP;
}

data class MediaSegment(
    val id: UUID,
    val type: SegmentType,
    val startMs: Long,
    val endMs: Long
)
