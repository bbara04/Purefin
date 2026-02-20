package hu.bbara.purefin.feature.shared.home

import hu.bbara.purefin.core.model.Episode
import hu.bbara.purefin.core.model.Movie
import hu.bbara.purefin.core.model.Series
import org.jellyfin.sdk.model.UUID
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.CollectionType

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
        BaseItemKind.MOVIE -> movie!!.heroImageUrl
        BaseItemKind.EPISODE -> episode!!.heroImageUrl
        BaseItemKind.SERIES -> series!!.heroImageUrl
        else -> throw IllegalArgumentException("Invalid type: $type")
    }
    fun watched() = when (type) {
        BaseItemKind.MOVIE -> movie!!.watched
        BaseItemKind.EPISODE -> episode!!.watched
        else -> throw IllegalArgumentException("Invalid type: $type")
    }
}
