package hu.bbara.purefin.common.ui

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
import hu.bbara.purefin.common.ui.components.PurefinAsyncImage
import hu.bbara.purefin.common.ui.components.UnwatchedEpisodeIndicator
import hu.bbara.purefin.common.ui.components.WatchStateIndicator
import hu.bbara.purefin.feature.shared.home.FocusableItem
import hu.bbara.purefin.feature.shared.home.PosterItem
import org.jellyfin.sdk.model.UUID
import org.jellyfin.sdk.model.api.BaseItemKind

@Composable
fun PosterCard(
    item: PosterItem,
    modifier: Modifier = Modifier,
    imageModifier: Modifier = Modifier,
    posterWidth: Dp = 144.dp,
    showSecondaryText: Boolean = false,
    indicatorSize: Int = 28,
    indicatorPadding: Dp = 8.dp,
    onFocusedItem: (FocusableItem) -> Unit = {},
    onMovieSelected: (UUID) -> Unit,
    onSeriesSelected: (UUID) -> Unit,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isFocused) 1.07f else 1.0f, label = "scale")

    fun openItem(posterItem: PosterItem) {
        when (posterItem.type) {
            BaseItemKind.MOVIE -> onMovieSelected(posterItem.id)
            BaseItemKind.SERIES -> onSeriesSelected(posterItem.id)
            BaseItemKind.EPISODE -> {
                val ep = posterItem.episode!!
                onEpisodeSelected(ep.seriesId, ep.seasonId, ep.id)
            }
            else -> {}
        }
    }

    Column(
        modifier = modifier
            .width(posterWidth)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0.5f, 0f)
            }
    ) {
        Box {
            PurefinAsyncImage(
                model = item.imageUrl,
                contentDescription = null,
                modifier = imageModifier
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(14.dp))
                    .border(
                        width = if (isFocused) 2.dp else 1.dp,
                        color = if (isFocused) scheme.primary else scheme.outlineVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .background(scheme.surfaceVariant)
                    .onFocusChanged {
                        isFocused = it.isFocused
                        if (it.isFocused) {
                            onFocusedItem(item)
                        }
                    }
                    .clickable(onClick = { openItem(item) }),
                contentScale = ContentScale.Crop
            )
            when (item.type) {
                BaseItemKind.MOVIE -> {
                    val m = item.movie!!
                    WatchStateIndicator(
                        size = indicatorSize,
                        modifier = Modifier.align(Alignment.TopEnd)
                            .padding(indicatorPadding),
                        watched = m.watched,
                        started = (m.progress ?: 0.0) > 0
                    )
                }
                BaseItemKind.EPISODE -> {
                    val ep = item.episode!!
                    WatchStateIndicator(
                        size = indicatorSize,
                        modifier = Modifier.align(Alignment.TopEnd)
                            .padding(indicatorPadding),
                        watched = ep.watched,
                        started = (ep.progress ?: 0.0) > 0
                    )
                }
                BaseItemKind.SERIES -> UnwatchedEpisodeIndicator(
                    size = indicatorSize,
                    modifier = Modifier.align(Alignment.TopEnd)
                        .padding(indicatorPadding),
                    unwatchedCount = item.series!!.unwatchedEpisodeCount
                )
                else -> {}
            }
        }
        Column(
            modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp, bottom = 8.dp)
        ) {
            Text(
                text = item.title,
                color = scheme.onBackground,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            item.secondaryText
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
