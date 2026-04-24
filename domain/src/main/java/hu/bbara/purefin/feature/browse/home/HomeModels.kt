package hu.bbara.purefin.feature.browse.home

import hu.bbara.purefin.image.ArtworkKind
import hu.bbara.purefin.image.ImageUrlBuilder
import hu.bbara.purefin.model.Episode
import hu.bbara.purefin.model.LibraryKind
import hu.bbara.purefin.model.MediaKind
import hu.bbara.purefin.model.Movie
import hu.bbara.purefin.model.Series
import java.util.UUID

sealed interface FocusableItem {
    val imageUrl: String
    val primaryText: String
    val secondaryText: String
    val description: String
    val id: UUID
    val type: MediaKind
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
