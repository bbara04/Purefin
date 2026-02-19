package hu.bbara.purefin.app.content.movie

import org.jellyfin.sdk.model.UUID

data class CastMember(
    val name: String,
    val role: String,
    val imageUrl: String?
)

data class MovieUiModel(
    val id: UUID,
    val title: String,
    val year: String,
    val rating: String,
    val runtime: String,
    val format: String,
    val synopsis: String,
    val heroImageUrl: String,
    val audioTrack: String,
    val subtitles: String,
    val progress: Double?,
    val cast: List<CastMember>
)
