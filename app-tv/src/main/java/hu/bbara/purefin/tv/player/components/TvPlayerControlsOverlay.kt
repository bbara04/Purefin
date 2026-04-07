package hu.bbara.purefin.tv.player.components

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
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.outlined.Forward30
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Replay10
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.core.player.model.PlayerUiState

@Composable
internal fun TvPlayerControlsOverlay(
    uiState: PlayerUiState,
    focusRequester: FocusRequester,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSeekRelative: (Long) -> Unit,
    onSeekLiveEdge: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onOpenAudioPanel: () -> Unit,
    onOpenSubtitlesPanel: () -> Unit,
    onOpenQualityPanel: () -> Unit,
    onOpenQueue: () -> Unit,
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
            onOpenQueue = onOpenQueue,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        )
        TvPlayerBottomSection(
            uiState = uiState,
            focusRequester = focusRequester,
            onPlayPause = onPlayPause,
            onSeek = onSeek,
            onSeekRelative = onSeekRelative,
            onSeekLiveEdge = onSeekLiveEdge,
            onNext = onNext,
            onPrevious = onPrevious,
            onOpenAudioPanel = onOpenAudioPanel,
            onOpenSubtitlesPanel = onOpenSubtitlesPanel,
            onOpenQualityPanel = onOpenQualityPanel,
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
    onOpenQueue: () -> Unit,
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
        TvIconButton(
            icon = Icons.AutoMirrored.Outlined.PlaylistPlay,
            contentDescription = "Queue",
            onClick = onOpenQueue
        )
    }
}

@Composable
private fun TvPlayerBottomSection(
    uiState: PlayerUiState,
    focusRequester: FocusRequester,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSeekRelative: (Long) -> Unit,
    onSeekLiveEdge: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onOpenAudioPanel: () -> Unit,
    onOpenSubtitlesPanel: () -> Unit,
    onOpenQualityPanel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Column(modifier = modifier) {
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
            focusRequester = focusRequester
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
                    size = 64
                )
                TvIconButton(
                    icon = Icons.Outlined.Replay10,
                    contentDescription = "Seek backward 10 seconds",
                    onClick = { onSeekRelative(-10_000) },
                    size = 64
                )
                TvIconButton(
                    icon = if (uiState.isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                    contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                    onClick = onPlayPause,
                    size = 72
                )
                TvIconButton(
                    icon = Icons.Outlined.Forward30,
                    contentDescription = "Seek forward 30 seconds",
                    onClick = { onSeekRelative(30_000) },
                    size = 64
                )
                TvIconButton(
                    icon = Icons.Outlined.SkipNext,
                    contentDescription = "Next",
                    onClick = onNext,
                    size = 64
                )
            }
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TvQualitySelectionButton(onClick = onOpenQualityPanel)
                TvAudioSelectionButton(onClick = onOpenAudioPanel)
                TvSubtitlesSelectionButton(onClick = onOpenSubtitlesPanel)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
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
