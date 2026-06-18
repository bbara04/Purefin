package hu.bbara.purefin.ui.screen.player.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.core.player.model.PlaylistElementUiModel
import hu.bbara.purefin.ui.common.image.PurefinAsyncImage

/**
 * Overlay button that appears near the end of a media item to let the user
 * quickly jump to the next episode. Shows the next episode's artwork and title.
 */
@Composable
fun NextEpisodeOverlay(
    nextEpisode: PlaylistElementUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .width(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable { onClick() },
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // "Up Next" label
        Text(
            text = "Up Next",
            color = scheme.secondary,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.padding(start = 12.dp, top = 8.dp, end = 12.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Thumbnail with play button overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp))
        ) {
            PurefinAsyncImage(
                model = nextEpisode.artworkUrl,
                contentDescription = nextEpisode.title,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )

            // Dark gradient overlay for readability
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.1f),
                                Color.Black.copy(alpha = 0.4f)
                            )
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Episode title
        Text(
            text = nextEpisode.title,
            color = scheme.onSurface,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 10.dp)
        )
    }
}

internal const val PlayerNextEpisodeOverlayTag = "player_next_episode_overlay"
