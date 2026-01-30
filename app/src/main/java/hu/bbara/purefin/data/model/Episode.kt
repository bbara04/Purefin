package hu.bbara.purefin.data.model

import java.util.UUID

data class Episode(
    val id: UUID,
    val seriesId: UUID,
    val seasonId: UUID,
    val index: Int,
    val title: String,
    val synopsis: String,
    val releaseDate: String,
    val rating: String,
    val runtime: String,
    val progress: Double?,
    val watched: Boolean,
    val format: String,
    val heroImageUrl: String,
    val cast: List<CastMember>
)
