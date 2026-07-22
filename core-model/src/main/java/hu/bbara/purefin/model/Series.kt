package hu.bbara.purefin.model

import java.util.UUID

val UNCATEGORIZED_SEASON_ID: UUID = UUID.fromString("c0ffee00-0000-0000-0000-000000000000")
val UNCATEGORIZED_SERIES_ID: UUID = UUID.fromString("c0ffee00-0000-0000-0000-000000000001")
const val UNCATEGORIZED_LABEL: String = "Uncategorized"

data class Series(
    val id: UUID,
    val libraryId: UUID,
    val name: String,
    val synopsis: String,
    val year: String,
    val imageUrlPrefix: String,
    val unwatchedEpisodeCount: Int,
    val seasonCount: Int,
    val seasons: List<Season>,
    val uncategorizedEpisodes: List<Episode> = emptyList(),
    val cast: List<CastMember>
) {

    /**
     * Real seasons plus a synthetic "Uncategorized" season appended at the end
     * when [uncategorizedEpisodes] is non-empty. Used by the UI for the season
     * tab list so uncategorized episodes are selectable like any other season.
     */
    val allSeasons: List<Season>
        get() = if (uncategorizedEpisodes.isEmpty()) {
            seasons
        } else {
            seasons + Season(
                id = UNCATEGORIZED_SEASON_ID,
                seriesId = id,
                name = UNCATEGORIZED_LABEL,
                index = 0,
                unwatchedEpisodeCount = uncategorizedEpisodes.count { !it.watched },
                episodeCount = uncategorizedEpisodes.size,
                episodes = uncategorizedEpisodes,
            )
        }
}
