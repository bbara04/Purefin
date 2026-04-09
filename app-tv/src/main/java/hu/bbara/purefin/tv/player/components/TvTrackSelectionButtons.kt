package hu.bbara.purefin.tv.player.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ClosedCaption
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.Language
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.core.player.model.PlayerUiState
import hu.bbara.purefin.core.player.model.TrackOption

internal const val TvPlayerQualityButtonTag = "tv_player_quality_button"
internal const val TvPlayerAudioButtonTag = "tv_player_audio_button"
internal const val TvPlayerSubtitlesButtonTag = "tv_player_subtitles_button"
internal const val TvPlayerTrackPanelTag = "tv_player_track_panel"
internal const val TvPlayerTrackSelectedItemTag = "tv_player_track_selected_item"
internal const val TvPlayerTrackFirstItemTag = "tv_player_track_first_item"
internal const val TvPlayerTrackLastItemTag = "tv_player_track_last_item"

internal enum class TvTrackPanelType {
    AUDIO,
    SUBTITLES,
    QUALITY
}

@Composable
internal fun TvQualitySelectionButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    TvIconButton(
        icon = Icons.Outlined.HighQuality,
        contentDescription = "Quality",
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
    )
}

@Composable
internal fun TvAudioSelectionButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    TvIconButton(
        icon = Icons.Outlined.Language,
        contentDescription = "Audio",
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
    )
}

@Composable
internal fun TvSubtitlesSelectionButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    TvIconButton(
        icon = Icons.Outlined.ClosedCaption,
        contentDescription = "Subtitles",
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
    )
}

@Composable
internal fun TvTrackSelectionPanel(
    panelType: TvTrackPanelType,
    uiState: PlayerUiState,
    onSelect: (TrackOption) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val (title, options, selectedId) = when (panelType) {
        TvTrackPanelType.AUDIO -> Triple("Audio", uiState.audioTracks, uiState.selectedAudioTrackId)
        TvTrackPanelType.SUBTITLES -> Triple("Subtitles", uiState.textTracks, uiState.selectedTextTrackId)
        TvTrackPanelType.QUALITY -> Triple("Quality", uiState.qualityTracks, uiState.selectedQualityTrackId)
    }
    val entryIndex = options.indexOfFirst { it.id == selectedId }.takeIf { it >= 0 } ?: 0
    val focusRequesters = remember(options.map(TrackOption::id)) {
        options.map { FocusRequester() }
    }
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (entryIndex - 1).coerceAtLeast(0)
    )

    LaunchedEffect(entryIndex, focusRequesters) {
        withFrameNanos { }
        focusRequesters.getOrNull(entryIndex)?.requestFocus()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag(TvPlayerTrackPanelTag),
        contentAlignment = Alignment.CenterEnd
    ) {
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .width(320.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)),
            color = scheme.surface.copy(alpha = 0.97f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    color = scheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                LazyColumn(
                    modifier = Modifier
                        .heightIn(max = 500.dp)
                        .fillMaxWidth(),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(options, key = { _, option -> option.id }) { index, option ->
                        val entryItemTag = when {
                            selectedId == null && index == entryIndex -> TvPlayerTrackFirstItemTag
                            index == entryIndex -> TvPlayerTrackSelectedItemTag
                            index == 0 -> TvPlayerTrackFirstItemTag
                            index == options.lastIndex -> TvPlayerTrackLastItemTag
                            else -> null
                        }
                        TvTrackOptionRow(
                            modifier = Modifier
                                .then(
                                    if (index == entryIndex) {
                                        Modifier
                                            .focusRequester(focusRequesters[index])
                                    } else {
                                        Modifier
                                    }
                                )
                                .then(
                                    if (entryItemTag != null) {
                                        Modifier.testTag(entryItemTag)
                                    } else {
                                        Modifier
                                    }
                                ),
                            label = option.label,
                            selected = option.id == selectedId,
                            isFirst = index == 0,
                            isLast = index == options.lastIndex,
                            onClose = onClose,
                            onClick = { onSelect(option) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TvTrackOptionRow(
    modifier: Modifier = Modifier,
    label: String,
    selected: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    onClose: () -> Unit,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isFocused) {
                    Modifier.border(2.dp, scheme.primary, RoundedCornerShape(12.dp))
                } else {
                    Modifier
                }
            )
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isFocused) scheme.primary.copy(alpha = 0.3f)
                else if (selected) scheme.primary.copy(alpha = 0.15f)
                else scheme.surfaceVariant.copy(alpha = 0.6f)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) {
                    false
                } else {
                    when (event.key) {
                        Key.Back -> {
                            onClose()
                            true
                        }

                        Key.DirectionLeft, Key.DirectionRight -> true
                        Key.DirectionUp -> isFirst
                        Key.DirectionDown -> isLast
                        else -> false
                    }
                }
            }
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Text(
            text = label,
            color = scheme.onSurface,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
