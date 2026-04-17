package hu.bbara.purefin.ui.common.card

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import hu.bbara.purefin.core.model.MediaKind
import hu.bbara.purefin.core.ui.model.MediaUiModel
import hu.bbara.purefin.core.ui.model.MovieUiModel
import hu.bbara.purefin.core.ui.model.SeriesUiModel
import hu.bbara.purefin.feature.browse.home.PosterItem
import java.util.UUID

@Composable
fun PosterCard(
    item: MediaUiModel,
    modifier: Modifier = Modifier,
    onMovieSelected: (UUID) -> Unit,
    onSeriesSelected: (UUID) -> Unit,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
) {
    PosterCardContent(
        model = item,
        onClick = {
            //TODO same throw this shit out when to composite onSelect is finished
            when (item) {
                is MovieUiModel -> onMovieSelected(item.id)
                is SeriesUiModel -> onSeriesSelected(item.id)
                else -> Unit
            }
        },
        modifier = modifier
    )
}

internal fun PosterItem.toPosterCardModel(): PosterCardModel {
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
