package hu.bbara.purefin.tv.home.ui

import hu.bbara.purefin.core.image.ImageUrlBuilder
import hu.bbara.purefin.core.model.Episode
import hu.bbara.purefin.core.model.Movie
import hu.bbara.purefin.core.model.Series
import hu.bbara.purefin.feature.browse.home.ContinueWatchingItem
import hu.bbara.purefin.feature.browse.home.FocusableItem
import hu.bbara.purefin.feature.browse.home.NextUpItem
import hu.bbara.purefin.feature.browse.home.PosterItem
import java.util.UUID
import hu.bbara.purefin.core.model.MediaKind
import hu.bbara.purefin.core.image.ArtworkKind
import kotlin.math.roundToInt

internal data class TvFocusedHeroModel(
    val id: UUID,
    val backdropImageUrl: String,
    val title: String,
    val metadata: List<String>,
) {
    val metadataText: String?
        get() = metadata.takeIf { it.isNotEmpty() }?.joinToString(" • ")
}

internal fun FocusableItem.toTvFocusedHeroModel(): TvFocusedHeroModel {
    return when (this) {
        is ContinueWatchingItem -> when (type) {
            MediaKind.MOVIE -> movie!!.toTvFocusedHeroModel()
            MediaKind.EPISODE -> episode!!.toTvFocusedHeroModel()
            else -> unsupportedType(type)
        }

        is NextUpItem -> episode.toTvFocusedHeroModel()
        is PosterItem -> when (type) {
            MediaKind.MOVIE -> movie!!.toTvFocusedHeroModel()
            MediaKind.EPISODE -> episode!!.toTvFocusedHeroModel()
            MediaKind.SERIES -> series!!.toTvFocusedHeroModel()
            else -> unsupportedType(type)
        }
    }
}

internal fun backdropImageUrl(imageUrlPrefix: String?, fallbackImageUrl: String): String {
    val backdropImageUrl = ImageUrlBuilder.finishImageUrl(imageUrlPrefix, ArtworkKind.BACKDROP)
    return backdropImageUrl.ifBlank { fallbackImageUrl }
}

private fun Movie.toTvFocusedHeroModel(): TvFocusedHeroModel {
    return TvFocusedHeroModel(
        id = id,
        backdropImageUrl = backdropImageUrl(
            imageUrlPrefix = imageUrlPrefix,
            fallbackImageUrl = ImageUrlBuilder.finishImageUrl(imageUrlPrefix, ArtworkKind.PRIMARY)
        ),
        title = title,
        metadata = listOf(year, rating, runtime, format).compactMetadata(),
    )
}

private fun Episode.toTvFocusedHeroModel(): TvFocusedHeroModel {
    return TvFocusedHeroModel(
        id = id,
        backdropImageUrl = backdropImageUrl(
            imageUrlPrefix = imageUrlPrefix,
            fallbackImageUrl = ImageUrlBuilder.finishImageUrl(imageUrlPrefix, ArtworkKind.PRIMARY)
        ),
        title = title,
        metadata = listOf(
            "Episode $index",
            releaseDate,
            runtime,
            rating,
            format
        ).compactMetadata(),
    )
}

private fun Series.toTvFocusedHeroModel(): TvFocusedHeroModel {
    val unwatchedText = if (unwatchedEpisodeCount > 0) {
        "$unwatchedEpisodeCount unwatched"
    } else {
        null
    }

    return TvFocusedHeroModel(
        id = id,
        backdropImageUrl = backdropImageUrl(
            imageUrlPrefix = imageUrlPrefix,
            fallbackImageUrl = ImageUrlBuilder.finishImageUrl(imageUrlPrefix, ArtworkKind.PRIMARY)
        ),
        title = name,
        metadata = listOf(year, seasonLabel(seasonCount), unwatchedText).compactMetadata(),
    )
}

private fun progressFraction(progress: Double?): Float? {
    val fraction = progress
        ?.div(100.0)
        ?.toFloat()
        ?.coerceIn(0f, 1f)

    return fraction?.takeIf { it > 0f }
}

private fun progressLabel(progressFraction: Float?): String? {
    return progressFraction?.let { "${(it * 100).roundToInt()}%" }
}

private fun List<String?>.compactMetadata(): List<String> {
    return map(String?::orEmpty)
        .map(String::trim)
        .filter(String::isNotBlank)
}

private fun seasonLabel(seasonCount: Int): String {
    return if (seasonCount == 1) "1 season" else "$seasonCount seasons"
}

private fun unsupportedType(type: MediaKind): Nothing {
    throw UnsupportedOperationException("Unsupported item type: $type")
}
