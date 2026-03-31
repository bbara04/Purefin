package hu.bbara.purefin.app.home.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import hu.bbara.purefin.common.ui.components.PurefinAsyncImage
import hu.bbara.purefin.common.ui.components.UnwatchedEpisodeIndicator
import hu.bbara.purefin.common.ui.components.WatchStateIndicator
import hu.bbara.purefin.feature.shared.home.PosterItem
import org.jellyfin.sdk.model.UUID
import org.jellyfin.sdk.model.api.BaseItemKind

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
        BaseItemKind.MOVIE -> listOf(
            item.movie?.year,
            item.movie?.runtime
        ).filterNotNull().filter { it.isNotBlank() }.joinToString(" • ")

        BaseItemKind.SERIES -> item.series!!.let { series ->
            if (series.seasonCount == 1) "1 season" else "${series.seasonCount} seasons"
        }

        BaseItemKind.EPISODE -> listOf(
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
                        BaseItemKind.MOVIE -> onMovieSelected(item.id)
                        BaseItemKind.SERIES -> onSeriesSelected(item.id)
                        BaseItemKind.EPISODE -> {
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
                    .border(
                        1.dp, scheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(18.dp)
                    )
                    .background(scheme.surfaceVariant)
            ) {
                PurefinAsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                when (item.type) {
                    BaseItemKind.MOVIE -> {
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

                    BaseItemKind.EPISODE -> {
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

                    BaseItemKind.SERIES -> {
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
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
