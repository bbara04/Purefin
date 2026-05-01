package hu.bbara.purefin.model

import java.util.UUID

data class Episode(
    val id: UUID,
    val seriesId: UUID,
    val seriesName: String,
    val seasonId: UUID,
    val seasonIndex: Int,
    val index: Int,
    val title: String,
    val synopsis: String,
    val releaseDate: String,
    val rating: String,
    val runtime: String,
    val progress: Double?,
    val watched: Boolean,
    val format: String,
    val imageUrlPrefix: String,
    val cast: List<CastMember>
)
