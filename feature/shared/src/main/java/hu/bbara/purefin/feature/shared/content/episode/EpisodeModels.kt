package hu.bbara.purefin.feature.shared.content.episode

import hu.bbara.purefin.core.model.CastMember
import org.jellyfin.sdk.model.UUID

data class EpisodeUiModel(
    val id: UUID,
    val title: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
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