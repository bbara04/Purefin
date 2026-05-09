package hu.bbara.purefin.ui.screen.player.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import hu.bbara.purefin.core.player.model.PlayerUiState
import hu.bbara.purefin.core.player.model.PlaylistElementUiModel

internal const val TvPlayerPlaylistRowTag = "tv_player_playlist_row"
internal const val TvPlayerPlaylistCurrentItemTag = "tv_player_playlist_current_item"
internal const val TvPlayerPlaylistFirstItemTag = "tv_player_playlist_first_item"
internal const val TvPlayerPlaylistLastItemTag = "tv_player_playlist_last_item"

@Composable
internal fun TvPlayerQueuePanel(
    uiState: PlayerUiState,
    firstItemFocusRequester: FocusRequester,
    onSelect: (String) -> Unit,
    onReturnToControls: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val currentIndex = uiState.queue.indexOfFirst { it.isCurrent }
    val entryIndex = if (currentIndex >= 0) currentIndex else 0
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (entryIndex - 1).coerceAtLeast(0)
    )
    val queueCountLabel = when (uiState.queue.size) {
        0 -> "No items in queue"
        1 -> "1 item in queue"
        else -> "${uiState.queue.size} items in queue"
    }

    LaunchedEffect(uiState.queue, entryIndex) {
        if (uiState.queue.isNotEmpty()) {
            withFrameNanos { }
            firstItemFocusRequester.requestFocus()
        }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = scheme.surface.copy(alpha = 0.94f)
    ) {
        if (uiState.queue.isEmpty()) {
            Text(
                text = "Add something to the queue to browse it here.",
                color = scheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TvPlayerPlaylistRowTag),
                state = listState,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                itemsIndexed(uiState.queue, key = { _, item -> item.id }) { index, item ->
                    val isEntryItem = index == entryIndex
                    TvQueueRowCard(
                        item = item,
                        isCurrent = item.isCurrent,
                        isFirst = index == 0,
                        isLast = index == uiState.queue.lastIndex,
                        onClick = { onSelect(item.id) },
                        onReturnToControls = onReturnToControls,
                        modifier = Modifier
                            .width(228.dp)
                            .then(
                                if (isEntryItem) {
                                    Modifier
                                        .focusRequester(firstItemFocusRequester)
                                        .testTag(TvPlayerPlaylistCurrentItemTag)
                                } else {
                                    Modifier
                                }
                            )
                            .then(
                                if (index == 0 && !isEntryItem) {
                                    Modifier.testTag(TvPlayerPlaylistFirstItemTag)
                                } else {
                                    Modifier
                                }
                            )
                            .then(
                                if (index == uiState.queue.lastIndex && !isEntryItem) {
                                    Modifier.testTag(TvPlayerPlaylistLastItemTag)
                                } else {
                                    Modifier
                                }
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun TvQueueRowCard(
    item: PlaylistElementUiModel,
    isCurrent: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    onReturnToControls: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.06f else 1f,
        label = "queueCardScale"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            isFocused -> scheme.primary
            isCurrent -> scheme.tertiary
            else -> scheme.outlineVariant.copy(alpha = 0.5f)
        },
        label = "queueCardBorder"
    )
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isFocused -> scheme.primaryContainer.copy(alpha = 0.96f)
            isCurrent -> scheme.tertiaryContainer.copy(alpha = 0.86f)
            else -> scheme.surfaceContainerHigh.copy(alpha = 0.72f)
        },
        label = "queueCardBackground"
    )
    val contentColor = when {
        isFocused -> scheme.onPrimaryContainer
        isCurrent -> scheme.onTertiaryContainer
        else -> scheme.onSurface
    }
    val shape = RoundedCornerShape(18.dp)

    Column(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .border(
                width = when {
                    isFocused -> 3.dp
                    isCurrent -> 2.dp
                    else -> 1.dp
                },
                color = borderColor,
                shape = shape
            )
            .clip(shape)
            .background(backgroundColor)
            .onFocusChanged { isFocused = it.isFocused }
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) {
                    false
                } else {
                    when (event.key) {
                        Key.DirectionUp -> {
                            onReturnToControls()
                            true
                        }

                        Key.DirectionLeft -> isFirst
                        Key.DirectionRight -> isLast
                        else -> false
                    }
                }
            }
            .clickable { onClick() }
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TvQueueArtwork(
            artworkUrl = item.artworkUrl,
            isCurrent = isCurrent,
            badgeColor = if (isFocused) scheme.primary else scheme.tertiary,
            badgeContentColor = if (isFocused) scheme.onPrimary else scheme.onTertiary,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = item.title,
            color = contentColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isCurrent || isFocused) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
private fun TvQueueArtwork(
    artworkUrl: String?,
    isCurrent: Boolean,
    badgeColor: Color,
    badgeContentColor: Color,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val placeholderPainter = ColorPainter(scheme.surfaceVariant)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(scheme.surfaceContainerHigh)
    ) {
        if (artworkUrl != null) {
            AsyncImage(
                model = artworkUrl.takeIf { it.isNotEmpty() },
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                contentScale = ContentScale.Crop,
                placeholder = placeholderPainter,
                error = placeholderPainter,
                fallback = placeholderPainter
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(scheme.surfaceContainerHigh)
            )
        }
        if (isCurrent) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(badgeColor)
                    .padding(horizontal = 9.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.PlayArrow,
                    contentDescription = null,
                    tint = badgeContentColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Now playing",
                    color = badgeContentColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
