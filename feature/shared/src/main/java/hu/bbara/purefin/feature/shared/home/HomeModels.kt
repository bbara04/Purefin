package hu.bbara.purefin.feature.shared.home

import hu.bbara.purefin.core.data.image.JellyfinImageHelper
import hu.bbara.purefin.core.model.Episode
import hu.bbara.purefin.core.model.Movie
import hu.bbara.purefin.core.model.Series
import org.jellyfin.sdk.model.UUID
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.CollectionType
import org.jellyfin.sdk.model.api.ImageType


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
    val type: BaseItemKind
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
    override val type: BaseItemKind = BaseItemKind.EPISODE,
    override val id: UUID = episode.id,
    override val title: String = episode.title,
    override val description: String = episode.synopsis,
    override val imageUrl: String = episode.heroImageUrl
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
    override val type: BaseItemKind = BaseItemKind.SERIES,
    override val id: UUID = series.id,
    override val title: String = series.name,
    override val description: String = series.synopsis,
    override val imageUrl: String = JellyfinImageHelper.finishImageUrl(
        prefixImageUrl = series.imageUrlPrefix,
        imageType = ImageType.PRIMARY
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
    override val type: BaseItemKind = BaseItemKind.MOVIE,
    override val id: UUID = movie.id,
    override val title: String = movie.title,
    override val description: String = movie.synopsis,
    override val imageUrl: String = JellyfinImageHelper.finishImageUrl(
        prefixImageUrl = movie.imageUrlPrefix,
        imageType = ImageType.PRIMARY
        )
) : SuggestedItem

data class ContinueWatchingItem(
    val type: BaseItemKind,
    val movie: Movie? = null,
    val episode: Episode? = null
) {
    val id: UUID = when (type) {
        BaseItemKind.MOVIE -> movie!!.id
        BaseItemKind.EPISODE -> episode!!.id
        else -> throw UnsupportedOperationException("Unsupported item type: $type")
    }
    val primaryText: String = when (type) {
        BaseItemKind.MOVIE -> movie!!.title
        BaseItemKind.EPISODE -> episode!!.title
        else -> throw UnsupportedOperationException("Unsupported item type: $type")
    }
    val secondaryText: String = when (type) {
        BaseItemKind.MOVIE -> movie!!.year
        BaseItemKind.EPISODE -> episode!!.releaseDate
        else -> throw UnsupportedOperationException("Unsupported item type: $type")
    }
    val progress: Double = when (type) {
        BaseItemKind.MOVIE -> movie!!.progress ?: 0.0
        BaseItemKind.EPISODE -> episode!!.progress ?: 0.0
        else -> throw UnsupportedOperationException("Unsupported item type: $type")
    }
}

data class NextUpItem(
    val episode: Episode
) {
    val id: UUID = episode.id
    val primaryText: String = episode.title
    val secondaryText: String = episode.releaseDate
}

data class LibraryItem(
    val id: UUID,
    val name: String,
    val type: CollectionType,
    val posterUrl: String,
    val isEmpty: Boolean
)

data class PosterItem(
    val type: BaseItemKind,
    val movie: Movie? = null,
    val series: Series? = null,
    val episode: Episode? = null
) {
    val id: UUID = when (type) {
        BaseItemKind.MOVIE -> movie!!.id
        BaseItemKind.EPISODE -> episode!!.id
        BaseItemKind.SERIES -> series!!.id
        else -> throw IllegalArgumentException("Invalid type: $type")
    }
    val title: String = when (type) {
        BaseItemKind.MOVIE -> movie!!.title
        BaseItemKind.EPISODE -> episode!!.title
        BaseItemKind.SERIES -> series!!.name
        else -> throw IllegalArgumentException("Invalid type: $type")
    }
    val imageUrl: String = when (type) {
        BaseItemKind.MOVIE -> movie!!.imageUrlPrefix
        BaseItemKind.EPISODE -> episode!!.heroImageUrl
        BaseItemKind.SERIES -> series!!.imageUrlPrefix
        else -> throw IllegalArgumentException("Invalid type: $type")
    }
    fun watched() = when (type) {
        BaseItemKind.MOVIE -> movie!!.watched
        BaseItemKind.EPISODE -> episode!!.watched
        else -> throw IllegalArgumentException("Invalid type: $type")
    }
}
