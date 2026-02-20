package hu.bbara.purefin.core.model

import java.util.UUID

data class Movie(
    val id: UUID,
    val libraryId: UUID,
    val title: String,
    val progress: Double?,
    val watched: Boolean,
    val year: String,
    val rating: String,
    val runtime: String,
    val format: String,
    val synopsis: String,
    val heroImageUrl: String,
    val audioTrack: String,
    val subtitles: String,
    val cast: List<CastMember>
)
