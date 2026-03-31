package hu.bbara.purefin.app.home.ui.continuewatching

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
import hu.bbara.purefin.common.ui.components.MediaProgressBar
import hu.bbara.purefin.common.ui.components.PurefinAsyncImage
import hu.bbara.purefin.feature.shared.home.ContinueWatchingItem
import org.jellyfin.sdk.model.UUID
import org.jellyfin.sdk.model.api.BaseItemKind

@Composable
internal fun ContinueWatchingCard(
    item: ContinueWatchingItem,
    onMovieSelected: (UUID) -> Unit,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val supportingText = when (item.type) {
        BaseItemKind.MOVIE -> listOf(
            item.movie?.year,
            item.movie?.runtime
        ).filterNotNull().filter { it.isNotBlank() }.joinToString(" • ")

        BaseItemKind.EPISODE -> listOf(
            "Episode ${item.episode?.index}",
            item.episode?.runtime
        ).filterNotNull().filter { it.isNotBlank() }.joinToString(" • ")

        else -> ""
    }
    val imageUrl = when (item.type) {
        BaseItemKind.MOVIE -> item.movie?.heroImageUrl
        BaseItemKind.EPISODE -> item.episode?.heroImageUrl
        else -> null
    }

    Surface(
        shape = RoundedCornerShape(26.dp),
        color = scheme.surfaceContainer,
        modifier = modifier.width(320.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    when (item.type) {
                        BaseItemKind.MOVIE -> onMovieSelected(item.movie!!.id)
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
