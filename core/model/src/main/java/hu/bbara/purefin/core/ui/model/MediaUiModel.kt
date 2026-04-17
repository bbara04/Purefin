package hu.bbara.purefin.core.ui.model

import hu.bbara.purefin.core.image.ArtworkKind
import hu.bbara.purefin.core.image.ImageUrlBuilder
import hu.bbara.purefin.core.model.Episode
import hu.bbara.purefin.core.model.Movie
import hu.bbara.purefin.core.model.Series
import java.util.UUID

sealed interface MediaUiModel {
    val id: UUID
    val primaryText: String
    val secondaryText: String
    val description: String
    val imageUrl: String
    val progress: Float?
        get() = null
    val watched: Boolean
        get() = false
}

class MovieUiModel: MediaUiModel {
    override val id: UUID
    override val primaryText: String
    override val secondaryText: String
    override val description: String
    override val imageUrl: String
    override val progress: Float?

    constructor(movie: Movie) {
        id = movie.id
        primaryText = movie.title
        secondaryText = movie.year
        description = movie.synopsis
        imageUrl = ImageUrlBuilder.finishImageUrl(
            prefixImageUrl = movie.imageUrlPrefix,
            artworkKind = ArtworkKind.PRIMARY
        )
        progress = (movie.progress?.toFloat() ?: 0f) / 100f
    }
}

class SeriesUiModel : MediaUiModel {
    override val id: UUID
    override val primaryText: String
    override val secondaryText: String
    override val description: String
    override val imageUrl: String

    constructor(series: Series) {
        id = series.id
        primaryText = series.name
        secondaryText = "${series.seasonCount} seasons"
        description = series.synopsis
        imageUrl = ImageUrlBuilder.finishImageUrl(
            prefixImageUrl = series.imageUrlPrefix,
            artworkKind = ArtworkKind.PRIMARY
        )
    }
}

class EpisodeUiModel : MediaUiModel {
    override val id: UUID
    override val primaryText: String
    override val secondaryText: String
    override val description: String
    override val imageUrl: String
    override val progress: Float?
    val seriesId: UUID
    val seasonId: UUID

    constructor(episode: Episode) {
        id = episode.id
        primaryText = episode.title
        secondaryText = episode.releaseDate
        description = episode.synopsis
        imageUrl = ImageUrlBuilder.finishImageUrl(
            prefixImageUrl = episode.imageUrlPrefix,
            artworkKind = ArtworkKind.PRIMARY
        )
        progress = (episode.progress?.toFloat() ?: 0f) / 100f
        seriesId = episode.seriesId
        seasonId = episode.seasonId
    }
}