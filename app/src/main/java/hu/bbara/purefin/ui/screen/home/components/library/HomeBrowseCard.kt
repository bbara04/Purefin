package hu.bbara.purefin.ui.screen.home.components.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.ui.common.image.PurefinAsyncImage
import hu.bbara.purefin.ui.common.badge.UnwatchedEpisodeIndicator
import hu.bbara.purefin.ui.common.badge.WatchStateIndicator
import hu.bbara.purefin.feature.browse.home.PosterItem
import java.util.UUID
import hu.bbara.purefin.core.model.MediaKind

@Composable
internal fun HomeBrowseCard(
    item: PosterItem,
    onMovieSelected: (UUID) -> Unit,
    onSeriesSelected: (UUID) -> Unit,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val supportingText = when (item.type) {
        MediaKind.MOVIE -> listOf(
            item.movie?.year,
            item.movie?.runtime
        ).filterNotNull().filter { it.isNotBlank() }.joinToString(" • ")

        MediaKind.SERIES -> item.series!!.let { series ->
            if (series.seasonCount == 1) "1 season" else "${series.seasonCount} seasons"
        }

        MediaKind.EPISODE -> listOf(
            "Episode ${item.episode?.index}",
            item.episode?.runtime
        ).filterNotNull().filter { it.isNotBlank() }.joinToString(" • ")

        else -> ""
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = scheme.surfaceContainer,
        modifier = modifier.width(188.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    when (item.type) {
                        MediaKind.MOVIE -> onMovieSelected(item.id)
                        MediaKind.SERIES -> onSeriesSelected(item.id)
                        MediaKind.EPISODE -> {
                            val episode = item.episode!!
                            onEpisodeSelected(episode.seriesId, episode.seasonId, episode.id)
                        }

                        else -> Unit
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(scheme.surface)
            ) {
                PurefinAsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                when (item.type) {
                    MediaKind.MOVIE -> {
                        val movie = item.movie!!
                        WatchStateIndicator(
                            size = 28,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp),
                            watched = movie.watched,
                            started = (movie.progress ?: 0.0) > 0
                        )
                    }

                    MediaKind.EPISODE -> {
                        val episode = item.episode!!
                        WatchStateIndicator(
                            size = 28,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp),
                            watched = episode.watched,
                            started = (episode.progress ?: 0.0) > 0
                        )
                    }

                    MediaKind.SERIES -> {
                        UnwatchedEpisodeIndicator(
                            size = 28,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp),
                            unwatchedCount = item.series!!.unwatchedEpisodeCount
                        )
                    }

                    else -> Unit
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Column(modifier = modifier.padding(12.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (supportingText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = supportingText,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
