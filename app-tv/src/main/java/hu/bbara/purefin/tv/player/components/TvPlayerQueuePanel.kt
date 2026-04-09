package hu.bbara.purefin.tv.player.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
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
import hu.bbara.purefin.core.player.model.PlayerUiState
import hu.bbara.purefin.core.player.model.QueueItemUi
import coil3.compose.AsyncImage

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

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = scheme.surface.copy(alpha = 0.94f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Playlist",
                    color = scheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = queueCountLabel,
                    color = scheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

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
                    contentPadding = PaddingValues(end = 4.dp)
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
}

@Composable
private fun TvQueueRowCard(
    item: QueueItemUi,
    isCurrent: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    onReturnToControls: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    var isFocused by remember { mutableStateOf(false) }
    val borderColor = when {
        isFocused -> scheme.primary
        isCurrent -> scheme.primary.copy(alpha = 0.55f)
        else -> scheme.outlineVariant.copy(alpha = 0.35f)
    }
    val backgroundColor = when {
        isFocused -> scheme.primary.copy(alpha = 0.18f)
        isCurrent -> scheme.surfaceVariant.copy(alpha = 0.78f)
        else -> scheme.surfaceVariant.copy(alpha = 0.62f)
    }

    Column(
        modifier = modifier
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(18.dp)
            )
            .clip(RoundedCornerShape(18.dp))
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
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TvQueueArtwork(
            artworkUrl = item.artworkUrl,
            modifier = Modifier.fillMaxWidth()
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (isCurrent) {
                Text(
                    text = "Current",
                    color = scheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = item.title,
                color = scheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            item.subtitle?.takeIf { it.isNotBlank() }?.let { subtitle ->
                Text(
                    text = subtitle,
                    color = scheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun TvQueueArtwork(
    artworkUrl: String?,
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
    }
}
