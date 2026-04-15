package hu.bbara.purefin.ui.common.card

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.bbara.purefin.core.model.MediaKind
import hu.bbara.purefin.ui.common.badge.UnwatchedEpisodeBadge
import hu.bbara.purefin.ui.common.badge.WatchStateBadge
import hu.bbara.purefin.ui.common.image.PurefinAsyncImage

data class PosterCardModel(
    val title: String,
    val secondaryText: String = "",
    val imageUrl: String?,
    val mediaKind: MediaKind,
    val badge: PosterCardBadge = PosterCardBadge.None
)

sealed interface PosterCardBadge {
    data object None : PosterCardBadge

    data class WatchState(
        val watched: Boolean,
        val started: Boolean
    ) : PosterCardBadge

    data class UnwatchedEpisodes(
        val count: Int
    ) : PosterCardBadge
}

@Composable
fun PosterCardContent(
    model: PosterCardModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageModifier: Modifier = Modifier,
    posterWidth: Dp = 144.dp,
    showSecondaryText: Boolean = false,
    indicatorSize: Int = 28,
    indicatorPadding: Dp = 8.dp,
    onFocused: () -> Unit = {},
    focusedScale: Float = 1f,
    focusedBorderWidth: Dp = 1.dp,
    focusedTransformOrigin: TransformOrigin = TransformOrigin(0.5f, 0f)
) {
    val scheme = MaterialTheme.colorScheme
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) focusedScale else 1f,
        label = "scale"
    )

    Column(
        modifier = modifier
            .width(posterWidth)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                transformOrigin = focusedTransformOrigin
            }
    ) {
        Box {
            PurefinAsyncImage(
                model = model.imageUrl,
                contentDescription = null,
                modifier = imageModifier
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(14.dp))
                    .border(
                        width = if (isFocused) focusedBorderWidth else 1.dp,
                        color = if (isFocused) scheme.primary else scheme.outlineVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .background(scheme.surfaceVariant)
                    .onFocusChanged {
                        isFocused = it.isFocused
                        if (it.isFocused) {
                            onFocused()
                        }
                    }
                    .clickable(onClick = onClick),
                contentScale = ContentScale.Crop
            )
            when (val badge = model.badge) {
                is PosterCardBadge.WatchState -> {
                    WatchStateBadge(
                        size = indicatorSize,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(indicatorPadding),
                        watched = badge.watched,
                        started = badge.started
                    )
                }

                is PosterCardBadge.UnwatchedEpisodes -> {
                    UnwatchedEpisodeBadge(
                        size = indicatorSize,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(indicatorPadding),
                        unwatchedCount = badge.count
                    )
                }

                PosterCardBadge.None -> Unit
            }
        }
        Column(
            modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp, bottom = 8.dp)
        ) {
            Text(
                text = model.title,
                color = scheme.onBackground,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            model.secondaryText
                .takeIf { showSecondaryText }
                ?.takeIf { it.isNotBlank() }
                ?.let { text ->
                    Text(
                        text = text,
                        color = scheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
        }
    }
}
