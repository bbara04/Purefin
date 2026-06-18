package hu.bbara.purefin.ui.screen.player.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import hu.bbara.purefin.core.player.model.PlaylistElementUiModel

/**
 * TV-optimized overlay button that appears near the end of a media item to let the user
 * quickly jump to the next episode. Shows the next episode's artwork and title.
 * Supports D-pad navigation and focus animations.
 */
@Composable
fun TvNextEpisodeOverlay(
    nextEpisode: PlaylistElementUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1f,
        label = "nextEpisodeScale"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) scheme.primary else Color.Transparent,
        label = "nextEpisodeBorder"
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (isFocused) {
            scheme.surfaceContainerHigh.copy(alpha = 0.96f)
        } else {
            Color.Black.copy(alpha = 0.85f)
        },
        label = "nextEpisodeBackground"
    )

    Column(
        modifier = modifier
            .width(280.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .border(
                width = if (isFocused) 3.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .clickable { onClick() },
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // "Up Next" label
        Text(
            text = "Up Next",
            color = scheme.secondary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Thumbnail with play button overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(12.dp))
        ) {
            AsyncImage(
                model = nextEpisode.artworkUrl?.takeIf { it.isNotEmpty() },
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

        Spacer(modifier = Modifier.height(12.dp))

        // Episode title
        Text(
            text = nextEpisode.title,
            color = scheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
        )
    }
}

internal const val TvPlayerNextEpisodeOverlayTag = "tv_player_next_episode_overlay"
