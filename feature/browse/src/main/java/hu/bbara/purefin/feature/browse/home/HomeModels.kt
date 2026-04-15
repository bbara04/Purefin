package hu.bbara.purefin.feature.browse.home

import hu.bbara.purefin.core.image.ImageUrlBuilder
import hu.bbara.purefin.core.model.Episode
import hu.bbara.purefin.core.model.Movie
import hu.bbara.purefin.core.model.Series
import java.util.UUID
import hu.bbara.purefin.core.model.MediaKind
import hu.bbara.purefin.core.model.LibraryKind
import hu.bbara.purefin.core.image.ArtworkKind


sealed interface SuggestedItem {
    val id: UUID
    val badge: String
    val title: String
    val supportingText: String
    val description: String
    val metadata: List<String>
    val imageUrl: String
    val ctaLabel: String
    val progress: Float?
    val type: MediaKind
}

data class SuggestedEpisode (
    val episode: Episode,
    override val badge: String = "",
    override val supportingText: String = listOf("Episode ${episode.index}", episode.runtime)
        .filter { it.isNotBlank() }
        .joinToString(" • "),
    override val metadata: List<String> =
        listOf(episode.releaseDate, episode.runtime, episode.rating, episode.format)
            .filter { it.isNotBlank() },
    override val ctaLabel: String = "Open",
    override val progress: Float? = episode.progress?.toFloat(),
    override val type: MediaKind = MediaKind.EPISODE,
    override val id: UUID = episode.id,
    override val title: String = episode.title,
    override val description: String = episode.synopsis,
    override val imageUrl: String = ImageUrlBuilder.finishImageUrl(
        prefixImageUrl = episode.imageUrlPrefix,
        artworkKind = ArtworkKind.PRIMARY
    )
) : SuggestedItem

data class SuggestedSeries (
    val series: Series,
    override val badge: String = "",
    override val supportingText: String =
        if (series.unwatchedEpisodeCount > 0) {
            "${series.unwatchedEpisodeCount} unwatched episodes"
        } else {
            "${series.seasonCount} seasons"
        },
    override val metadata: List<String> =
        listOf(series.year, "${series.seasonCount} seasons").filter { it.isNotBlank() },
    override val ctaLabel: String = "Open",
    override val progress: Float? = null,
    override val type: MediaKind = MediaKind.SERIES,
    override val id: UUID = series.id,
    override val title: String = series.name,
    override val description: String = series.synopsis,
    override val imageUrl: String = ImageUrlBuilder.finishImageUrl(
        prefixImageUrl = series.imageUrlPrefix,
        artworkKind = ArtworkKind.PRIMARY
    )
) : SuggestedItem

data class SuggestedMovie (
    val movie: Movie,
    override val badge: String = "",
    override val supportingText: String = listOf(movie.year, movie.runtime)
        .filter { it.isNotBlank() }
        .joinToString(" • "),
    override val metadata: List<String> =
        listOf(movie.year, movie.runtime, movie.rating, movie.format)
            .filter { it.isNotBlank() },
    override val ctaLabel: String = "Open",
    override val progress: Float? = movie.progress?.toFloat(),
    override val type: MediaKind = MediaKind.MOVIE,
    override val id: UUID = movie.id,
    override val title: String = movie.title,
    override val description: String = movie.synopsis,
    override val imageUrl: String = ImageUrlBuilder.finishImageUrl(
        prefixImageUrl = movie.imageUrlPrefix,
        artworkKind = ArtworkKind.PRIMARY
        )
) : SuggestedItem

sealed interface FocusableItem {
    val imageUrl: String
    val primaryText: String
    val secondaryText: String
    val description: String
    val id: UUID
    val type: MediaKind
}

data class ContinueWatchingItem(
    override val type: MediaKind,
    val movie: Movie? = null,
    val episode: Episode? = null,
) : FocusableItem {
    override val id: UUID = when (type) {
        MediaKind.MOVIE -> movie!!.id
        MediaKind.EPISODE -> episode!!.id
        else -> throw UnsupportedOperationException("Unsupported item type: $type")
    }
    override val primaryText: String = when (type) {
        MediaKind.MOVIE -> movie!!.title
        MediaKind.EPISODE -> episode!!.title
        else -> throw UnsupportedOperationException("Unsupported item type: $type")
    }
    override val secondaryText: String = when (type) {
        MediaKind.MOVIE -> movie!!.year
        MediaKind.EPISODE -> episode!!.releaseDate
        else -> throw UnsupportedOperationException("Unsupported item type: $type")
    }
    override val imageUrl: String = when (type) {
        MediaKind.MOVIE -> ImageUrlBuilder.finishImageUrl(
            prefixImageUrl = movie!!.imageUrlPrefix,
            artworkKind = ArtworkKind.PRIMARY
        )
        MediaKind.EPISODE -> ImageUrlBuilder.finishImageUrl(
            prefixImageUrl = episode!!.imageUrlPrefix,
            artworkKind = ArtworkKind.PRIMARY
        )
        else -> throw IllegalArgumentException("Invalid type: $type")
    }
    override val description: String = when (type) {
        MediaKind.MOVIE -> movie!!.synopsis
        MediaKind.EPISODE -> episode!!.synopsis
        else -> throw UnsupportedOperationException("Unsupported item type: $type")
    }
    val progress: Double = when (type) {
        MediaKind.MOVIE -> movie!!.progress ?: 0.0
        MediaKind.EPISODE -> episode!!.progress ?: 0.0
        else -> throw UnsupportedOperationException("Unsupported item type: $type")
    }
}

data class NextUpItem(
    val episode: Episode
) : FocusableItem {
    override val id: UUID = episode.id
    override val type: MediaKind
        get() = MediaKind.EPISODE
    override val imageUrl: String
        get() = ImageUrlBuilder.finishImageUrl(
            prefixImageUrl = episode.imageUrlPrefix,
            artworkKind = ArtworkKind.PRIMARY
        )
    override val primaryText: String = episode.title
    override val secondaryText: String = episode.releaseDate
    override val description: String
        get() = episode.synopsis
}

data class LibraryItem(
    val id: UUID,
    val name: String,
    val type: LibraryKind,
    val posterUrl: String,
    val isEmpty: Boolean
)

data class PosterItem(
    override val type: MediaKind,
    val movie: Movie? = null,
    val series: Series? = null,
    val episode: Episode? = null
) : FocusableItem {
    override val id: UUID = when (type) {
        MediaKind.MOVIE -> movie!!.id
        MediaKind.EPISODE -> episode!!.id
        MediaKind.SERIES -> series!!.id
        else -> throw IllegalArgumentException("Invalid type: $type")
    }
    val title: String = when (type) {
        MediaKind.MOVIE -> movie!!.title
        MediaKind.EPISODE -> episode!!.title
        MediaKind.SERIES -> series!!.name
        else -> throw IllegalArgumentException("Invalid type: $type")
    }
    override val imageUrl: String = when (type) {
        MediaKind.MOVIE -> ImageUrlBuilder.finishImageUrl(
            prefixImageUrl = movie!!.imageUrlPrefix,
            artworkKind = ArtworkKind.PRIMARY
        )
        MediaKind.EPISODE -> ImageUrlBuilder.finishImageUrl(
            prefixImageUrl = episode!!.imageUrlPrefix,
            artworkKind = ArtworkKind.PRIMARY
        )
        MediaKind.SERIES -> ImageUrlBuilder.finishImageUrl(
            prefixImageUrl = series!!.imageUrlPrefix,
            artworkKind = ArtworkKind.PRIMARY
        )
        else -> throw IllegalArgumentException("Invalid type: $type")
    }
    override val primaryText: String
        get() = when (type) {
            MediaKind.MOVIE -> movie!!.title
            MediaKind.EPISODE -> episode!!.title
            MediaKind.SERIES -> series!!.name
            else -> throw IllegalArgumentException("Invalid type: $type")
        }
    override val secondaryText: String
        get() = when (type) {
            MediaKind.MOVIE -> movie!!.year
            MediaKind.EPISODE -> episode!!.releaseDate
            MediaKind.SERIES -> series!!.year
            else -> throw IllegalArgumentException("Invalid type: $type")
        }
    override val description: String
        get() = when (type) {
            MediaKind.MOVIE -> movie!!.synopsis
            MediaKind.EPISODE -> episode!!.synopsis
            MediaKind.SERIES -> series!!.synopsis
            else -> throw IllegalArgumentException("Invalid type: $type")
        }

    fun watched() = when (type) {
        MediaKind.MOVIE -> movie!!.watched
        MediaKind.EPISODE -> episode!!.watched
        else -> throw IllegalArgumentException("Invalid type: $type")
    }
}
