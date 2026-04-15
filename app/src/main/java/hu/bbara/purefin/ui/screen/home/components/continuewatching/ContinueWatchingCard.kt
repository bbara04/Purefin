package hu.bbara.purefin.ui.screen.home.components.continuewatching

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.ui.common.bar.MediaProgressBar
import hu.bbara.purefin.ui.common.image.PurefinAsyncImage
import hu.bbara.purefin.core.image.ImageUrlBuilder
import hu.bbara.purefin.feature.browse.home.ContinueWatchingItem
import java.util.UUID
import hu.bbara.purefin.core.model.MediaKind
import hu.bbara.purefin.core.image.ArtworkKind

@Composable
internal fun ContinueWatchingCard(
    item: ContinueWatchingItem,
    onMovieSelected: (UUID) -> Unit,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val supportingText = when (item.type) {
        MediaKind.MOVIE -> listOf(
            item.movie?.year,
            item.movie?.runtime
        ).filterNotNull().filter { it.isNotBlank() }.joinToString(" • ")

        MediaKind.EPISODE -> listOf(
            "Episode ${item.episode?.index}",
            item.episode?.runtime
        ).filterNotNull().filter { it.isNotBlank() }.joinToString(" • ")

        else -> ""
    }
    val imageUrl = when (item.type) {
        MediaKind.MOVIE -> ImageUrlBuilder.finishImageUrl(
            prefixImageUrl = item.movie?.imageUrlPrefix,
            artworkKind = ArtworkKind.PRIMARY
        )
        MediaKind.EPISODE -> ImageUrlBuilder.finishImageUrl(
            prefixImageUrl = item.episode?.imageUrlPrefix,
            artworkKind = ArtworkKind.PRIMARY
        )
        else -> null
    }

    Surface(
        shape = RoundedCornerShape(26.dp),
        color = scheme.surfaceContainer,
        modifier = modifier.width(280.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    when (item.type) {
                        MediaKind.MOVIE -> onMovieSelected(item.movie!!.id)
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
                    .aspectRatio(16f / 9f)
                    .background(scheme.surfaceContainer)
            ) {
                if (imageUrl != null) {
                    PurefinAsyncImage(
                        model = imageUrl,
                        contentDescription = item.primaryText,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.08f),
                                    Color.Black.copy(alpha = 0.38f)
                                )
                            )
                        )
                )
                MediaProgressBar(
                    progress = (item.progress.toFloat() / 100f).coerceIn(0f, 1f),
                    foregroundColor = scheme.primary,
                    backgroundColor = Color.White.copy(alpha = 0.24f),
                    modifier = Modifier.align(Alignment.BottomStart)
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = item.primaryText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (supportingText.isNotBlank()) {
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
