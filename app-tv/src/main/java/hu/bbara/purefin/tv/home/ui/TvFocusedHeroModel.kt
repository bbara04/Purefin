package hu.bbara.purefin.tv.home.ui

import hu.bbara.purefin.core.data.image.JellyfinImageHelper
import hu.bbara.purefin.core.model.Episode
import hu.bbara.purefin.core.model.Movie
import hu.bbara.purefin.core.model.Series
import hu.bbara.purefin.feature.shared.home.ContinueWatchingItem
import hu.bbara.purefin.feature.shared.home.FocusableItem
import hu.bbara.purefin.feature.shared.home.NextUpItem
import hu.bbara.purefin.feature.shared.home.PosterItem
import org.jellyfin.sdk.model.UUID
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.ImageType
import kotlin.math.roundToInt

internal data class TvFocusedHeroModel(
    val id: UUID,
    val backdropImageUrl: String,
    val eyebrowText: String,
    val title: String,
    val metadata: List<String>,
    val watchedText: String?,
    val progressFraction: Float?,
    val progressLabel: String?,
) {
    val metadataText: String?
        get() = metadata.takeIf { it.isNotEmpty() }?.joinToString(" • ")
}

internal fun FocusableItem.toTvFocusedHeroModel(): TvFocusedHeroModel {
    return when (this) {
        is ContinueWatchingItem -> when (type) {
            BaseItemKind.MOVIE -> movie!!.toTvFocusedHeroModel(sourceLabel = "Continue watching")
            BaseItemKind.EPISODE -> episode!!.toTvFocusedHeroModel(sourceLabel = "Continue watching")
            else -> unsupportedType(type)
        }

        is NextUpItem -> episode.toTvFocusedHeroModel(sourceLabel = "Next up")
        is PosterItem -> when (type) {
            BaseItemKind.MOVIE -> movie!!.toTvFocusedHeroModel(sourceLabel = "Movie")
            BaseItemKind.EPISODE -> episode!!.toTvFocusedHeroModel(sourceLabel = "Episode")
            BaseItemKind.SERIES -> series!!.toTvFocusedHeroModel(sourceLabel = "Series")
            else -> unsupportedType(type)
        }
    }
}

internal fun backdropImageUrl(imageUrlPrefix: String?, fallbackImageUrl: String): String {
    val backdropImageUrl = JellyfinImageHelper.finishImageUrl(imageUrlPrefix, ImageType.BACKDROP)
    return backdropImageUrl.ifBlank { fallbackImageUrl }
}

private fun Movie.toTvFocusedHeroModel(sourceLabel: String): TvFocusedHeroModel {
    val progressFraction = progressFraction(progress)
    return TvFocusedHeroModel(
        id = id,
        backdropImageUrl = backdropImageUrl(
            imageUrlPrefix = imageUrlPrefix,
            fallbackImageUrl = JellyfinImageHelper.finishImageUrl(imageUrlPrefix, ImageType.PRIMARY)
        ),
        eyebrowText = sourceLabel,
        title = title,
        metadata = listOf(year, rating, runtime, format).compactMetadata(),
        watchedText = "Watched".takeIf { watched },
        progressFraction = progressFraction.takeIf { !watched && it != null },
        progressLabel = progressLabel(progressFraction).takeIf { !watched && progressFraction != null },
    )
}

private fun Episode.toTvFocusedHeroModel(sourceLabel: String): TvFocusedHeroModel {
    val progressFraction = progressFraction(progress)
    return TvFocusedHeroModel(
        id = id,
        backdropImageUrl = backdropImageUrl(
            imageUrlPrefix = imageUrlPrefix,
            fallbackImageUrl = JellyfinImageHelper.finishImageUrl(imageUrlPrefix, ImageType.PRIMARY)
        ),
        eyebrowText = sourceLabel,
        title = title,
        metadata = listOf(
            "Episode $index",
            releaseDate,
            runtime,
            rating,
            format
        ).compactMetadata(),
        watchedText = "Watched".takeIf { watched },
        progressFraction = progressFraction.takeIf { !watched && it != null },
        progressLabel = progressLabel(progressFraction).takeIf { !watched && progressFraction != null },
    )
}

private fun Series.toTvFocusedHeroModel(sourceLabel: String): TvFocusedHeroModel {
    val unwatchedText = if (unwatchedEpisodeCount > 0) {
        "$unwatchedEpisodeCount unwatched"
    } else {
        null
    }

    return TvFocusedHeroModel(
        id = id,
        backdropImageUrl = backdropImageUrl(
            imageUrlPrefix = imageUrlPrefix,
            fallbackImageUrl = JellyfinImageHelper.finishImageUrl(imageUrlPrefix, ImageType.PRIMARY)
        ),
        eyebrowText = sourceLabel,
        title = name,
        metadata = listOf(year, seasonLabel(seasonCount), unwatchedText).compactMetadata(),
        watchedText = null,
        progressFraction = null,
        progressLabel = null,
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

private fun unsupportedType(type: BaseItemKind): Nothing {
    throw UnsupportedOperationException("Unsupported item type: $type")
}
