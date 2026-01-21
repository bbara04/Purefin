package hu.bbara.purefin.app.content.series

data class SeriesEpisodeUiModel(
    val id: String,
    val title: String,
    val description: String,
    val duration: String,
    val imageUrl: String
)

data class SeriesSeasonUiModel(
    val name: String,
    val isSelected: Boolean,
    val episodes: List<SeriesEpisodeUiModel>
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
)
