package hu.bbara.purefin.ui.common.card

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.core.model.MediaKind
import hu.bbara.purefin.feature.browse.home.FocusableItem
import hu.bbara.purefin.feature.browse.home.PosterItem
import java.util.UUID

@Composable
fun PosterCard(
    item: PosterItem,
    modifier: Modifier = Modifier,
    imageModifier: Modifier = Modifier,
    posterWidth: Dp = 144.dp,
    showSecondaryText: Boolean = false,
    indicatorSize: Int = 28,
    indicatorPadding: Dp = 8.dp,
    onFocusedItem: (FocusableItem) -> Unit = {},
    onMovieSelected: (UUID) -> Unit,
    onSeriesSelected: (UUID) -> Unit,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
) {
    PosterCardContent(
        model = item.toPosterCardModel(),
        onClick = { item.open(onMovieSelected, onSeriesSelected, onEpisodeSelected) },
        modifier = modifier,
        imageModifier = imageModifier,
        posterWidth = posterWidth,
        showSecondaryText = showSecondaryText,
        indicatorSize = indicatorSize,
        indicatorPadding = indicatorPadding,
        onFocused = { onFocusedItem(item) },
        focusedScale = 1.07f,
        focusedBorderWidth = 2.dp
    )
}

private fun PosterItem.toPosterCardModel(): PosterCardModel {
    return PosterCardModel(
        title = title,
        secondaryText = secondaryText,
        imageUrl = imageUrl,
        mediaKind = type,
        badge = when (type) {
            MediaKind.MOVIE -> {
                val movie = requireNotNull(movie)
                PosterCardBadge.WatchState(
                    watched = movie.watched,
                    started = (movie.progress ?: 0.0) > 0.0
                )
            }

            MediaKind.EPISODE -> {
                val episode = requireNotNull(episode)
                PosterCardBadge.WatchState(
                    watched = episode.watched,
                    started = (episode.progress ?: 0.0) > 0.0
                )
            }

            MediaKind.SERIES -> PosterCardBadge.UnwatchedEpisodes(
                count = requireNotNull(series).unwatchedEpisodeCount
            )

            else -> PosterCardBadge.None
        }
    )
}

private fun PosterItem.open(
    onMovieSelected: (UUID) -> Unit,
    onSeriesSelected: (UUID) -> Unit,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
) {
    when (type) {
        MediaKind.MOVIE -> onMovieSelected(id)
        MediaKind.SERIES -> onSeriesSelected(id)
        MediaKind.EPISODE -> {
            val ep = requireNotNull(episode)
            onEpisodeSelected(ep.seriesId, ep.seasonId, ep.id)
        }

        else -> Unit
    }
}
