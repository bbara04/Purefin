package hu.bbara.purefin.app.content.series

data class SeriesEpisodeUiModel(
    val id: String,
    val title: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val description: String,
    val duration: String,
    val imageUrl: String,
    val watched: Boolean,
    val progress: Double?
)

data class SeriesSeasonUiModel(
    val name: String,
    val episodes: List<SeriesEpisodeUiModel>,
    val unplayedCount: Int?
)

data class SeriesCastMemberUiModel(
    val name: String,
    val role: String,
    val imageUrl: String?
)

data class SeriesUiModel(
    val title: String,
    val year: String,
    val rating: String,
    val seasons: String,
    val format: String,
    val synopsis: String,
    val heroImageUrl: String,
    val seasonTabs: List<SeriesSeasonUiModel>,
    val cast: List<SeriesCastMemberUiModel>
) {
    fun getNextEpisode(): SeriesEpisodeUiModel {
        for (season in seasonTabs) {
            for (episode in season.episodes) {
                if (!episode.watched) {
                    return episode
                }
            }
        }
        return seasonTabs.first().episodes.first()
    }
}
