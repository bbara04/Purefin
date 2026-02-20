package hu.bbara.purefin.player.ui.components

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
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Cast
import androidx.compose.material.icons.outlined.Forward30
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.PlaylistPlay
import androidx.compose.material.icons.outlined.Replay10
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.common.ui.components.GhostIconButton
import hu.bbara.purefin.common.ui.components.PurefinIconButton
import hu.bbara.purefin.core.player.model.PlayerUiState
import hu.bbara.purefin.core.player.model.TrackOption

@Composable
fun PlayerControlsOverlay(
    uiState: PlayerUiState,
    showControls: Boolean,
    overlayController: PersistentOverlayController,
    onBack: () -> Unit,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSeekRelative: (Long) -> Unit,
    onSeekLiveEdge: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSelectTrack: (TrackOption) -> Unit,
    onQueueSelected: (String) -> Unit,
    onOpenQueue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    var scrubbing by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Black.copy(alpha = 0.45f),
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.6f)
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            TopBar(
                title = uiState.title ?: "Playing",
                subtitle = uiState.subtitle,
                onBack = onBack,
                onCast = { },
                onMore = { },
                onOpenQueue = onOpenQueue,
                modifier = Modifier.align(Alignment.TopCenter)
            )
            BottomSection(
                uiState = uiState,
                scrubbing = scrubbing,
                overlayController = overlayController,
                onScrubStart = { scrubbing = true },
                onScrub = onSeek,
                onScrubFinished = { scrubbing = false },
                onNext = onNext,
                onPrevious = onPrevious,
                onPlayPause = onPlayPause,
                onSeekForward = { onSeekRelative(30_000) },
                onSeekBackward = { onSeekRelative(-10_000) },
                onSeekLiveEdge = onSeekLiveEdge,
                onSelectTrack = onSelectTrack,
                onQueueSelected = onQueueSelected,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun TopBar(
    title: String,
    subtitle: String?,
    onBack: () -> Unit,
    onCast: () -> Unit,
    onMore: () -> Unit,
    onOpenQueue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GhostIconButton(
                icon = Icons.Outlined.ArrowBack,
                contentDescription = "Back",
                onClick = onBack
            )
            Column {
                Text(text = title, color = scheme.onBackground, fontWeight = FontWeight.Bold)
                if (subtitle != null) {
                    Text(text = subtitle, color = scheme.onBackground.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GhostIconButton(
                icon = Icons.Outlined.PlaylistPlay,
                contentDescription = "Queue",
                onClick = onOpenQueue
            )
            GhostIconButton(icon = Icons.Outlined.Cast, contentDescription = "Cast", onClick = onCast)
            GhostIconButton(icon = Icons.Outlined.MoreVert, contentDescription = "More", onClick = onMore)
        }
    }
}

@Composable
private fun BottomSection(
    uiState: PlayerUiState,
    scrubbing: Boolean,
    overlayController: PersistentOverlayController,
    onScrubStart: () -> Unit,
    onScrub: (Long) -> Unit,
    onScrubFinished: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekBackward: () -> Unit,
    onSeekLiveEdge: () -> Unit,
    onSelectTrack: (TrackOption) -> Unit,
    onQueueSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatTime(uiState.positionMs),
                color = scheme.onSurface,
                style = MaterialTheme.typography.bodySmall
            )
            if (uiState.isLive) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "LIVE", color = scheme.primary, fontWeight = FontWeight.Bold)
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
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        PlayerSeekBar(
            positionMs = uiState.positionMs,
            durationMs = uiState.durationMs,
            bufferedMs = uiState.bufferedMs,
            chapterMarkers = uiState.chapters,
            adMarkers = uiState.ads,
            onSeek = onScrub,
            onScrubStarted = onScrubStart,
            onScrubFinished = onScrubFinished
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
        ) {
            Row(
                modifier = Modifier.align(Alignment.Center),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PurefinIconButton(
                    icon = Icons.Outlined.SkipPrevious,
                    contentDescription = "Previous",
                    onClick = onPrevious,
                    size = 64
                )
                PurefinIconButton(
                    icon = Icons.Outlined.Replay10,
                    contentDescription = "Seek backward",
                    onClick = onSeekBackward,
                    size = 64
                )
                PurefinIconButton(
                    icon = if (uiState.isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                    contentDescription = "Play/Pause",
                    onClick = onPlayPause,
                    size = 64
                )
                PurefinIconButton(
                    icon = Icons.Outlined.Forward30,
                    contentDescription = "Seek forward",
                    onClick = onSeekForward,
                    size = 64
                )
                PurefinIconButton(
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
                QualitySelectionButton(
                    options = uiState.qualityTracks,
                    selectedId = uiState.selectedQualityTrackId,
                    onSelect = onSelectTrack,
                    overlayController = overlayController
                )
                AudioSelectionButton(
                    options = uiState.audioTracks,
                    selectedId = uiState.selectedAudioTrackId,
                    onSelect = onSelectTrack,
                    overlayController = overlayController
                )
                SubtitlesSelectionButton(
                    options = uiState.textTracks,
                    selectedId = uiState.selectedTextTrackId,
                    onSelect = onSelectTrack,
                    overlayController = overlayController
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
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
