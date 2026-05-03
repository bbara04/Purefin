package hu.bbara.purefin.ui.screen.home.components.continuewatching

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.ui.common.bar.MediaProgressBar
import hu.bbara.purefin.ui.common.card.MediaImageCard
import hu.bbara.purefin.ui.common.media.homeMediaSharedBoundsSource
import hu.bbara.purefin.ui.common.media.rememberHomeMediaSharedBoundsClick
import hu.bbara.purefin.ui.model.MediaAction
import hu.bbara.purefin.ui.model.MediaUiModel

@Composable
internal fun ContinueWatchingCard(
    item: MediaUiModel,
    sharedBoundsKey: String,
    onMediaSelected: (MediaUiModel) -> Unit,
    onMarkAsWatched: (MediaUiModel, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val onClick = rememberHomeMediaSharedBoundsClick(sharedBoundsKey) {
        onMediaSelected(item)
    }

    MediaImageCard(
        imageUrl = item.primaryImageUrl,
        title = item.primaryText,
        subtitle = item.secondaryText,
        onClick = onClick,
        popupActions = listOf(
            MediaAction(
                name = "Mark as watched",
                onClick = { onMarkAsWatched(item, true) }
            ),
            MediaAction(
                name = "Mark as unwatched",
                onClick = { onMarkAsWatched(item, false) }
            )
        ),
        imageModifier = Modifier.homeMediaSharedBoundsSource(sharedBoundsKey),
        shapeSize = 26.dp,
        imageAspectRatio = 16f / 9f,
        titleStyle = MaterialTheme.typography.titleMedium,
        textPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        modifier = modifier.width(280.dp)
    ) {
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
        item.progress?.let { progress ->
            MediaProgressBar(
                progress = progress,
                foregroundColor = scheme.primary,
                backgroundColor = Color.White.copy(alpha = 0.24f),
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }
    }
}
