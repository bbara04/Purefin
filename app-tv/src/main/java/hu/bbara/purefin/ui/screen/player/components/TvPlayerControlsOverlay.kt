package hu.bbara.purefin.ui.screen.player.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Forward30
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Replay10
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.player.model.PlayerUiState

internal const val TvPlayerPlaylistStateTag = "tv_player_playlist_state"
internal const val TvPlayerPlayPauseButtonTag = "tv_player_play_pause_button"
internal const val TvPlayerSeekBarTag = "tv_player_seek_bar"

@Composable
internal fun TvPlayerControlsOverlay(
    uiState: PlayerUiState,
    focusRequester: FocusRequester,
    isPlaylistExpanded: Boolean,
    qualityButtonFocusRequester: FocusRequester,
    audioButtonFocusRequester: FocusRequester,
    subtitlesButtonFocusRequester: FocusRequester,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSeekRelative: (Long) -> Unit,
    onSeekLiveEdge: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onOpenAudioPanel: () -> Unit,
    onOpenSubtitlesPanel: () -> Unit,
    onOpenQualityPanel: () -> Unit,
    onExpandPlaylist: () -> Unit,
    onCollapsePlaylist: () -> Unit,
    onSelectQueueItem: (String) -> Unit,
    qualityButtonEnabled: Boolean,
    audioButtonEnabled: Boolean,
    subtitlesButtonEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Black.copy(alpha = 0.5f),
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.7f)
                    )
                )
            )
    ) {
        TvPlayerTopBar(
            title = uiState.title ?: "Playing",
            subtitle = uiState.subtitle,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        )
        TvPlayerBottomSection(
            uiState = uiState,
            focusRequester = focusRequester,
            isPlaylistExpanded = isPlaylistExpanded,
            qualityButtonFocusRequester = qualityButtonFocusRequester,
            audioButtonFocusRequester = audioButtonFocusRequester,
            subtitlesButtonFocusRequester = subtitlesButtonFocusRequester,
            onPlayPause = onPlayPause,
            onSeek = onSeek,
            onSeekRelative = onSeekRelative,
            onSeekLiveEdge = onSeekLiveEdge,
            onNext = onNext,
            onPrevious = onPrevious,
            onOpenAudioPanel = onOpenAudioPanel,
            onOpenSubtitlesPanel = onOpenSubtitlesPanel,
            onOpenQualityPanel = onOpenQualityPanel,
            onExpandPlaylist = onExpandPlaylist,
            onCollapsePlaylist = onCollapsePlaylist,
            onSelectQueueItem = onSelectQueueItem,
            qualityButtonEnabled = qualityButtonEnabled,
            audioButtonEnabled = audioButtonEnabled,
            subtitlesButtonEnabled = subtitlesButtonEnabled,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        )
    }
}

@Composable
private fun TvPlayerTopBar(
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = title,
                color = scheme.onBackground,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = scheme.onBackground.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun TvPlayerBottomSection(
    uiState: PlayerUiState,
    focusRequester: FocusRequester,
    isPlaylistExpanded: Boolean,
    qualityButtonFocusRequester: FocusRequester,
    audioButtonFocusRequester: FocusRequester,
    subtitlesButtonFocusRequester: FocusRequester,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSeekRelative: (Long) -> Unit,
    onSeekLiveEdge: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onOpenAudioPanel: () -> Unit,
    onOpenSubtitlesPanel: () -> Unit,
    onOpenQualityPanel: () -> Unit,
    onExpandPlaylist: () -> Unit,
    onCollapsePlaylist: () -> Unit,
    onSelectQueueItem: (String) -> Unit,
    qualityButtonEnabled: Boolean,
    audioButtonEnabled: Boolean,
    subtitlesButtonEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val playlistFocusRequester = remember { FocusRequester() }
    var wasPlaylistExpanded by remember { mutableStateOf(isPlaylistExpanded) }

    LaunchedEffect(isPlaylistExpanded) {
        if (isPlaylistExpanded && uiState.queue.isNotEmpty()) {
            playlistFocusRequester.requestFocus()
        } else if (wasPlaylistExpanded) {
            focusRequester.requestFocus()
        }
        wasPlaylistExpanded = isPlaylistExpanded
    }

    val playlistExpandState = if (isPlaylistExpanded) "expanded" else "collapsed"
    val expandPlaylistModifier = Modifier.onPreviewKeyEvent { event ->
        handleExpandPlaylistKey(event, onExpandPlaylist)
    }

    Column(
        modifier = modifier
            .testTag(TvPlayerPlaylistStateTag)
            .semantics { stateDescription = playlistExpandState }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatTime(uiState.positionMs),
                color = scheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )
            if (uiState.isLive) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIVE",
                        color = scheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Catch up",
                        color = scheme.onSurface,
                        modifier = Modifier.clickable { onSeekLiveEdge() }
                    )
                }
            } else {
                Text(
                    text = formatTime(uiState.durationMs),
                    color = scheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        TvPlayerSeekBar(
            positionMs = uiState.positionMs,
            durationMs = uiState.durationMs,
            bufferedMs = uiState.bufferedMs,
            chapterMarkers = uiState.chapters,
            adMarkers = uiState.ads,
            onSeek = onSeek,
            onSeekRelative = onSeekRelative,
            togglePlayState = onPlayPause,
            onMoveDown = {
                focusRequester.requestFocus()
                true
            },
            modifier = Modifier.testTag(TvPlayerSeekBarTag)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Row(
                modifier = Modifier.align(Alignment.Center),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TvIconButton(
                    icon = Icons.Outlined.SkipPrevious,
                    contentDescription = "Previous",
                    onClick = onPrevious,
                    size = 64,
                    modifier = expandPlaylistModifier
                )
                TvIconButton(
                    icon = Icons.Outlined.Replay10,
                    contentDescription = "Seek backward 10 seconds",
                    onClick = { onSeekRelative(-10_000) },
                    size = 64,
                    modifier = expandPlaylistModifier
                )
                TvIconButton(
                    icon = if (uiState.isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                    contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                    onClick = onPlayPause,
                    size = 72,
                    modifier = expandPlaylistModifier
                        .focusRequester(focusRequester)
                        .testTag(TvPlayerPlayPauseButtonTag)
                )
                TvIconButton(
                    icon = Icons.Outlined.Forward30,
                    contentDescription = "Seek forward 30 seconds",
                    onClick = { onSeekRelative(30_000) },
                    size = 64,
                    modifier = expandPlaylistModifier
                )
                TvIconButton(
                    icon = Icons.Outlined.SkipNext,
                    contentDescription = "Next",
                    onClick = onNext,
                    size = 64,
                    modifier = expandPlaylistModifier
                )
            }
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TvQualitySelectionButton(
                    onClick = onOpenQualityPanel,
                    enabled = qualityButtonEnabled,
                    modifier = expandPlaylistModifier
                        .focusRequester(qualityButtonFocusRequester)
                        .testTag(TvPlayerQualityButtonTag)
                )
                TvAudioSelectionButton(
                    onClick = onOpenAudioPanel,
                    enabled = audioButtonEnabled,
                    modifier = expandPlaylistModifier
                        .focusRequester(audioButtonFocusRequester)
                        .testTag(TvPlayerAudioButtonTag)
                )
                TvSubtitlesSelectionButton(
                    onClick = onOpenSubtitlesPanel,
                    enabled = subtitlesButtonEnabled,
                    modifier = expandPlaylistModifier
                        .focusRequester(subtitlesButtonFocusRequester)
                        .testTag(TvPlayerSubtitlesButtonTag)
                )
            }
        }
        AnimatedVisibility(
            visible = isPlaylistExpanded,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
            TvPlayerQueuePanel(
                uiState = uiState,
                firstItemFocusRequester = playlistFocusRequester,
                onSelect = onSelectQueueItem,
                onReturnToControls = onCollapsePlaylist,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp)
            )
        }
        Spacer(modifier = Modifier.height(if (isPlaylistExpanded) 12.dp else 8.dp))
    }
}

private fun handleExpandPlaylistKey(
    event: androidx.compose.ui.input.key.KeyEvent,
    onExpand: () -> Unit
): Boolean {
    if (event.type == KeyEventType.KeyDown && event.key == Key.DirectionDown) {
        onExpand()
        return true
    }
    return false
}

private fun formatTime(positionMs: Long): String {
    val totalSeconds = positionMs / 1000
    val seconds = (totalSeconds % 60).toInt()
    val minutes = ((totalSeconds / 60) % 60).toInt()
    val hours = (totalSeconds / 3600).toInt()
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
