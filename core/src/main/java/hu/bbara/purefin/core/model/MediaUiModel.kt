package hu.bbara.purefin.core.model

import hu.bbara.purefin.core.image.ArtworkKind
import hu.bbara.purefin.core.image.ImageUrlBuilder
import hu.bbara.purefin.model.Episode
import hu.bbara.purefin.model.Movie
import hu.bbara.purefin.model.Series
import java.util.UUID

sealed interface MediaUiModel {
    val id: UUID
    val primaryText: String
    val secondaryText: String
    val description: String
    val primaryImageUrl: String
    val backdropImageUrl: String
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
    private val prefixImageUrl: String
    override val watched: Boolean
    override val primaryImageUrl: String
        get() {
            return ImageUrlBuilder.finishImageUrl(
                prefixImageUrl = prefixImageUrl,
                artworkKind = ArtworkKind.PRIMARY
            )
        }
    override val backdropImageUrl: String
        get() {
            return ImageUrlBuilder.finishImageUrl(
                prefixImageUrl = prefixImageUrl,
                artworkKind = ArtworkKind.BACKDROP
            )
        }
    override val progress: Float?

    constructor(movie: Movie) {
        id = movie.id
        primaryText = movie.title
        secondaryText = movie.year
        description = movie.synopsis
        prefixImageUrl = movie.imageUrlPrefix
        progress = (movie.progress?.toFloat() ?: 0f) / 100f
        watched = movie.watched
    }

    companion object {
        fun createPlaceholder() : MovieUiModel {
            return MovieUiModel(
                Movie(
                    id = UUID.randomUUID(),
                    libraryId = UUID.randomUUID(),
                    title = "Loading...",
                    progress = 0.0,
                    watched = false,
                    year = "",
                    rating = "",
                    runtime = "",
                    format = "",
                    synopsis = "",
                    imageUrlPrefix = "",
                    cast = emptyList()
                )
            )
        }
    }
}

class SeriesUiModel : MediaUiModel {
    override val id: UUID
    override val primaryText: String
    override val secondaryText: String
    override val description: String
    override val watched: Boolean
    private val prefixImageUrl: String
    val unwatchedEpisodeCount: Int
    override val primaryImageUrl: String
        get() {
            return ImageUrlBuilder.finishImageUrl(
                prefixImageUrl = prefixImageUrl,
                artworkKind = ArtworkKind.PRIMARY
            )
        }
    override val backdropImageUrl: String
        get() {
            return ImageUrlBuilder.finishImageUrl(
                prefixImageUrl = prefixImageUrl,
                artworkKind = ArtworkKind.BACKDROP
            )
        }

    constructor(series: Series) {
        id = series.id
        primaryText = series.name
        secondaryText = "${series.seasonCount} seasons"
        description = series.synopsis
        prefixImageUrl = series.imageUrlPrefix
        watched = series.unwatchedEpisodeCount == 0
        unwatchedEpisodeCount = series.unwatchedEpisodeCount
    }
}

class EpisodeUiModel : MediaUiModel {
    override val id: UUID
    override val primaryText: String
    override val secondaryText: String
    override val description: String
    private val prefixImageUrl: String
    override val watched: Boolean
    override val primaryImageUrl: String
        get() {
            return ImageUrlBuilder.finishImageUrl(
                prefixImageUrl = prefixImageUrl,
                artworkKind = ArtworkKind.PRIMARY
            )
        }
    override val backdropImageUrl: String
        get() {
            return ImageUrlBuilder.finishImageUrl(
                prefixImageUrl = prefixImageUrl,
                artworkKind = ArtworkKind.PRIMARY
            )
        }
    override val progress: Float?
    val seriesId: UUID
    val seasonId: UUID

    constructor(episode: Episode) {
        id = episode.id
        primaryText = episode.seriesName
        secondaryText = "S${episode.seasonIndex.toString().padStart(2, '0')}xE${episode.index.toString().padStart(2, '0')} : ${episode.title}"
        description = episode.synopsis
        prefixImageUrl = episode.imageUrlPrefix
        progress = (episode.progress?.toFloat() ?: 0f) / 100f
        seriesId = episode.seriesId
        seasonId = episode.seasonId
        watched = episode.watched
    }
}