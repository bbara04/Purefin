package hu.bbara.purefin.data.model

import java.util.UUID

data class Season(
    val id: UUID,
    val seriesId: UUID,
    val name: String,
    val index: Int,
    val unwatchedEpisodeCount: Int,
    val episodeCount: Int,
    val episodes: List<Episode>
) {
}
