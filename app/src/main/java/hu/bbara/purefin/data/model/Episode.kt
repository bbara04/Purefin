package hu.bbara.purefin.data.model

import hu.bbara.purefin.app.content.episode.CastMember
import java.util.UUID

data class Episode(
    val id: UUID,
    val seriesId: UUID,
    val seasonId: UUID,
    val title: String,
    val index: Int,
    val releaseDate: String,
    val rating: String,
    val runtime: String,
    val format: String,
    val synopsis: String,
    val heroImageUrl: String,
    val cast: List<CastMember>
)
