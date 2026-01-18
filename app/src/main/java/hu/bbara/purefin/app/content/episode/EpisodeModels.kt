package hu.bbara.purefin.app.content.episode

import org.jellyfin.sdk.model.UUID

data class CastMember(
    val name: String,
    val role: String,
    val imageUrl: String?
)

data class EpisodeUiModel(
    val id: UUID,
    val title: String,
    val releaseDate: String,
    val rating: String,
    val runtime: String,
    val format: String,
    val synopsis: String,
    val heroImageUrl: String,
    val audioTrack: String,
    val subtitles: String,
    val cast: List<CastMember>
)