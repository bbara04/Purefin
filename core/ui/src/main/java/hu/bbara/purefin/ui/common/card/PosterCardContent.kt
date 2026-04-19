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
import hu.bbara.purefin.core.ui.model.EpisodeUiModel
import hu.bbara.purefin.core.ui.model.MediaUiModel
import hu.bbara.purefin.core.ui.model.MovieUiModel
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
    model: MediaUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageModifier: Modifier = Modifier,
    posterWidth: Dp = 144.dp,
    contentScale: Float = 1f,
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
    val shape = RoundedCornerShape((14f * contentScale).dp)
    val unfocusedBorderWidth = (1f * contentScale).dp
    val scaledFocusedBorderWidth = focusedBorderWidth * contentScale
    val contentTopPadding = (8f * contentScale).dp
    val contentHorizontalPadding = (4f * contentScale).dp
    val contentBottomPadding = (8f * contentScale).dp
    val titleFontSize = (13f * contentScale).sp
    val secondaryFontSize = (11f * contentScale).sp
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
                model = model.primaryImageUrl,
                contentDescription = null,
                modifier = imageModifier
                    .aspectRatio(2f / 3f)
                    .clip(shape)
                    .border(
                        width = if (isFocused) scaledFocusedBorderWidth else unfocusedBorderWidth,
                        color = if (isFocused) scheme.primary else scheme.outlineVariant.copy(alpha = 0.3f),
                        shape = shape
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
            when (model) {
                is MovieUiModel, is EpisodeUiModel -> {
                    WatchStateBadge(
                        size = indicatorSize,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(indicatorPadding),
                        watched = model.watched,
                        started = (model.progress ?: 0f) > 0f
                    )
                }
                else -> Unit
            }
        }
        Column(
            modifier = Modifier.padding(
                top = contentTopPadding,
                start = contentHorizontalPadding,
                end = contentHorizontalPadding,
                bottom = contentBottomPadding
            )
        ) {
            Text(
                text = model.primaryText,
                color = scheme.onBackground,
                fontSize = titleFontSize,
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
                        fontSize = secondaryFontSize,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
        }
    }
}
