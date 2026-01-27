package hu.bbara.purefin.player.ui

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import hu.bbara.purefin.player.ui.components.PlayerControlsOverlay
import hu.bbara.purefin.player.ui.components.PlayerGesturesLayer
import hu.bbara.purefin.player.ui.components.PlayerLoadingErrorEndCard
import hu.bbara.purefin.player.ui.components.PlayerQueuePanel
import hu.bbara.purefin.player.ui.components.PlayerSettingsSheet
import hu.bbara.purefin.player.ui.components.PlayerSideSliders
import hu.bbara.purefin.player.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    onBack: () -> Unit

) {
    val uiState by viewModel.uiState.collectAsState()
    val controlsVisible by viewModel.controlsVisible.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1) }
    var volume by remember { mutableStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) / maxVolume.toFloat()) }
    var brightness by remember { mutableStateOf(readCurrentBrightness(activity)) }
    var showSettings by remember { mutableStateOf(false) }
    var brightnessOverlayVisible by remember { mutableStateOf(false) }
    var volumeOverlayVisible by remember { mutableStateOf(false) }
    var showQueuePanel by remember { mutableStateOf(false) }
    var horizontalSeekFeedback by remember { mutableStateOf<Long?>(null) }
    var showFeedbackPreview by remember { mutableStateOf(false) }

    LaunchedEffect(showFeedbackPreview) {
        if (!showFeedbackPreview) {
            delay(1000)
            horizontalSeekFeedback = null
        }
    }

    LaunchedEffect(uiState.isPlaying) {
        if (uiState.isPlaying) {
            showSettings = false
            showQueuePanel = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    player = viewModel.player
                }
            },
            update = {
                it.player = viewModel.player
            },
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.Center)
        )

        PlayerGesturesLayer(
            modifier = Modifier.fillMaxSize(),
            onTap = { viewModel.toggleControlsVisibility() },
            onDoubleTapRight = { viewModel.seekBy(30_000) },
            onDoubleTapLeft = { viewModel.seekBy(-10_000) },
            onDoubleTapCenter = {viewModel.togglePlayPause()},
            onVerticalDragLeft = { delta ->
                val diff = (-delta / 800f)
                brightness = (brightness + diff).coerceIn(0f, 1f)
                brightnessOverlayVisible = true
                applyBrightness(activity, brightness)
            },
            onVerticalDragRight = { delta ->
                val diff = (-delta / 800f)
                volume = (volume + diff).coerceIn(0f, 1f)
                volumeOverlayVisible = true
                audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    (volume * maxVolume).roundToInt(),
                    0
                )
            },
            setFeedBackPreview = {
                showFeedbackPreview = it
            },
            onHorizontalDragPreview = {
                horizontalSeekFeedback = it
            },
            onHorizontalDrag = {
                viewModel.seekBy(it)
                horizontalSeekFeedback = it
            }
        )

        horizontalSeekFeedback?.let { delta ->
            SeekAmountIndicator(
                deltaMs = delta,
                modifier = Modifier
                    .align(Alignment.Center)
            )
        }

        AnimatedVisibility(
            visible = volumeOverlayVisible || brightnessOverlayVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            PlayerSideSliders(
                modifier = Modifier
                    .fillMaxSize(),
                brightness = brightness,
                volume = volume,
                showBrightness = brightnessOverlayVisible,
                showVolume = volumeOverlayVisible,
                onHide = {
                    brightnessOverlayVisible = false
                    volumeOverlayVisible = false
                }
            )
        }

        AnimatedVisibility(
            visible = controlsVisible || uiState.isBuffering || uiState.isEnded || uiState.error != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            PlayerControlsOverlay(
                modifier = Modifier.fillMaxSize(),
                uiState = uiState,
                showControls = controlsVisible,
                onBack = onBack,
                onPlayPause = { viewModel.togglePlayPause() },
                onSeek = { viewModel.seekTo(it) },
                onSeekRelative = { delta -> viewModel.seekBy(delta) },
                onSeekLiveEdge = { viewModel.seekToLiveEdge() },
                onNext = { viewModel.next() },
                onPrevious = { viewModel.previous() },
                onToggleCaptions = {
                    val off = uiState.textTracks.firstOrNull { it.isOff }
                    val currentId = uiState.selectedTextTrackId
                    val next = if (currentId == off?.id) {
                        uiState.textTracks.firstOrNull { !it.isOff }
                    } else off
                    next?.let { viewModel.selectTrack(it) }
                },
                onShowSettings = { showSettings = true },
                onQueueSelected = { viewModel.playQueueItem(it) },
                onOpenQueue = { showQueuePanel = true }
            )
        }

        PlayerLoadingErrorEndCard(
            modifier = Modifier.align(Alignment.Center),
            uiState = uiState,
            onRetry = {
                viewModel.retry()
            },
            onNext = { viewModel.next() },
            onReplay = { viewModel.seekTo(0L); viewModel.togglePlayPause() },
            onDismissError = { viewModel.clearError() }
        )

        PlayerSettingsSheet(
            visible = showSettings,
            uiState = uiState,
            onDismiss = { showSettings = false },
            onSelectTrack = { viewModel.selectTrack(it) },
            onSpeedSelected = { viewModel.setPlaybackSpeed(it) }
        )

        AnimatedVisibility(
            visible = showQueuePanel,
            enter = slideInHorizontally { it },
            exit = slideOutHorizontally { it }
        ) {
            PlayerQueuePanel(
                uiState = uiState,
                onSelect = { id ->
                    viewModel.playQueueItem(id)
                    showQueuePanel = false
                },
                onClose = { showQueuePanel = false },
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}

@Composable
private fun SeekAmountIndicator(deltaMs: Long, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val prefix = if (deltaMs >= 0) "+" else "-"
    val formatted = formatSeekDelta(abs(deltaMs))
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(scheme.surface.copy(alpha = 0.9f))
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = "$prefix$formatted",
            color = scheme.onSurface,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

private fun formatSeekDelta(deltaMs: Long): String {
    val totalSeconds = (deltaMs / 1000).toInt()
    val seconds = totalSeconds % 60
    val minutes = totalSeconds / 60
    return if (minutes > 0) {
        "%d:%02d".format(minutes, seconds)
    } else {
        "%02d s".format(seconds)
    }
}

private fun readCurrentBrightness(activity: Activity?): Float {
    val current = activity?.window?.attributes?.screenBrightness
    return if (current != null && current >= 0) current else 0.5f
}

private fun applyBrightness(activity: Activity?, value: Float) {
    activity ?: return
    val params = activity.window.attributes
    params.screenBrightness = value
    activity.window.attributes = params
}
