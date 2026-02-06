package hu.bbara.purefin.data.model

import java.util.UUID

data class Series(
    val id: UUID,
    val libraryId: UUID,
    val name: String,
    val synopsis: String,
    val year: String,
    val heroImageUrl: String,
    val unwatchedEpisodeCount: Int,
    val seasonCount: Int,
    val seasons: List<Season>,
    val cast: List<CastMember>
) {
}
