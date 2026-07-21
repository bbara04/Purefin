package hu.bbara.purefin.ui.screen.player

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.provider.Settings
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrightnessMedium
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.media3.ui.SubtitleView
import hu.bbara.purefin.core.player.model.TimedMarker
import hu.bbara.purefin.core.player.viewmodel.PlayerViewModel
import hu.bbara.purefin.model.SegmentType
import hu.bbara.purefin.ui.common.visual.EmptyValueTimedVisibility
import hu.bbara.purefin.ui.common.visual.ValueChangeTimedVisibility
import hu.bbara.purefin.ui.screen.player.components.NextEpisodeOverlay
import hu.bbara.purefin.ui.screen.player.components.PersistentOverlayContainer
import hu.bbara.purefin.ui.screen.player.components.PlayerAdjustmentIndicator
import hu.bbara.purefin.ui.screen.player.components.PlayerControlsOverlay
import hu.bbara.purefin.ui.screen.player.components.PlayerGesturesLayer
import hu.bbara.purefin.ui.screen.player.components.PlayerLoadingErrorEndCard
import hu.bbara.purefin.ui.screen.player.components.PlayerQueuePanel
import hu.bbara.purefin.ui.screen.player.components.PlayerSeekBarTrack
import hu.bbara.purefin.ui.screen.player.components.PlayerTimeRow
import hu.bbara.purefin.ui.screen.player.components.SkipSegmentButton
import hu.bbara.purefin.ui.screen.player.components.rememberPersistentOverlayController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private const val CONTROLS_VISIBLE_SUBTITLE_BOTTOM_PADDING_FRACTION = 0.32f
private const val BRIGHTNESS_DRAG_SENSITIVITY = 800f
private const val BRIGHTNESS_AUTO_ENTER_THRESHOLD = 0.15f
private const val BRIGHTNESS_AUTO_EXIT_THRESHOLD = 0.15f
private const val CONTROLS_AUTO_HIDE_MS = 3_000L

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var hideControlsJob: Job? by remember { mutableStateOf(null) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var controlsVisible by remember { mutableStateOf(true) }
    var controlsHasPopupOpen by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as? Activity

    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1) }
    var volume by remember { mutableFloatStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) / maxVolume.toFloat()) }
    var brightness by remember { mutableFloatStateOf(readCurrentBrightness(activity, context)) }
    var isAutoBrightness by remember { mutableStateOf(false) }
    var dragOvershoot by remember { mutableFloatStateOf(0f) }
    var autoDragTick by remember { mutableStateOf(0) }
    val originalScreenBrightness by remember { mutableFloatStateOf(activity?.window?.attributes?.screenBrightness ?: -1f) }
    var showQueuePanel by remember { mutableStateOf(false) }
    var horizontalSeekFeedback by remember { mutableStateOf<Long?>(null) }
    var horizontalSeekPreviewPositionMs by remember { mutableStateOf<Long?>(null) }
    val overlayController = rememberPersistentOverlayController()

    val subtitleBottomPaddingFraction =
        if (controlsVisible) {
            CONTROLS_VISIBLE_SUBTITLE_BOTTOM_PADDING_FRACTION
        } else {
            SubtitleView.DEFAULT_BOTTOM_PADDING_FRACTION
        }


    val showSkipIntroButton = !controlsVisible && uiState.activeSkippableSegmentEndMs != null && uiState.activeSkippableSegmentType == SegmentType.INTRO
    val showNextEpisodeOverlay = !controlsVisible
            && uiState.nextEpisode != null
            && uiState.durationMs > 0L
            && ((uiState.durationMs - uiState.positionMs) <= 60_000L || uiState.activeSkippableSegmentType == SegmentType.OUTRO)
            && !uiState.isEnded


    fun toggleControlsVisibility() {
        controlsVisible = !controlsVisible
    }
    fun hideControls() {
        controlsVisible = false
    }
    fun hideControlsWithTimeout() {
        hideControlsJob?.cancel()
        if (controlsVisible && !controlsHasPopupOpen && !uiState.isEnded && uiState.error == null) {
            hideControlsJob = scope.launch {
                delay(CONTROLS_AUTO_HIDE_MS)
                hideControls()
            }
        }
    }
    fun onScreenTap() {
        toggleControlsVisibility()
        hideControlsWithTimeout()
    }
    fun onBrightnessDragStart(isLeftSide: Boolean) {
        if (isLeftSide) {
            dragOvershoot = 0f
        }
    }
    fun onBrightnessDragEnd() {
        dragOvershoot = 0f
    }
    fun onBrightnessDrag(delta: Float) {
        val diff = -delta / BRIGHTNESS_DRAG_SENSITIVITY
        if (isAutoBrightness) {
            if (diff > 0f) {
                dragOvershoot += diff
                if (dragOvershoot >= BRIGHTNESS_AUTO_EXIT_THRESHOLD) {
                    isAutoBrightness = false
                    brightness = (dragOvershoot - BRIGHTNESS_AUTO_EXIT_THRESHOLD).coerceIn(0f, 1f)
                    dragOvershoot = 0f
                    applyBrightness(activity, brightness, isAuto = false)
                }
            } else {
                autoDragTick++
            }
        } else {
            val newBrightness = brightness + diff
            if (newBrightness <= 0f) {
                brightness = 0f
                applyBrightness(activity, brightness, isAuto = false)
                dragOvershoot += newBrightness
                if (dragOvershoot <= -BRIGHTNESS_AUTO_ENTER_THRESHOLD) {
                    isAutoBrightness = true
                    dragOvershoot = 0f
                    applyBrightness(activity, brightness, isAuto = true)
                }
            } else {
                brightness = newBrightness.coerceIn(0f, 1f)
                dragOvershoot = 0f
                applyBrightness(activity, brightness, isAuto = false)
            }
        }
    }
    fun onVolumeDrag(delta: Float) {
        val diff = -delta / 800f
        volume = (volume + diff).coerceIn(0f, 1f)
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            (volume * maxVolume).roundToInt(),
            0
        )
    }
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        hideControlsWithTimeout()
    }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        restoreBrightness(activity, originalScreenBrightness)
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    player = viewModel.player
                    subtitleView?.setBottomPaddingFraction(subtitleBottomPaddingFraction)
                }
            },
            update = {
                it.player = viewModel.player
                it.subtitleView?.setBottomPaddingFraction(subtitleBottomPaddingFraction)
            },
            onRelease = { playerView ->
                playerView.player = null
            },
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.Center)
        )

        PlayerGesturesLayer(
            modifier = Modifier.align(Alignment.BottomCenter),
            controlsVisible = controlsVisible,
            onTap = ::onScreenTap,
            onDoubleTapRight = { viewModel.seekBy(30_000) },
            onDoubleTapLeft = { viewModel.seekBy(-10_000) },
            onDoubleTapCenter = { viewModel.togglePlayPause() },
            onVerticalDragStart = ::onBrightnessDragStart,
            onVerticalDragEnd = ::onBrightnessDragEnd,
            onVerticalDragLeft = ::onBrightnessDrag,
            onVerticalDragRight = ::onVolumeDrag,
            onHorizontalDragPreview = { deltaMs, previewPositionMs ->
                horizontalSeekFeedback = deltaMs
                horizontalSeekPreviewPositionMs = previewPositionMs?.let {
                    if (uiState.durationMs > 0L) {
                        it.coerceIn(0L, uiState.durationMs)
                    } else {
                        it.coerceAtLeast(0L)
                    }
                }
            },
            onHorizontalDragSeekTo = {
                viewModel.seekTo(it)
            },
            currentPositionProvider = { uiState.positionMs },
        )

        EmptyValueTimedVisibility(
            value = horizontalSeekFeedback,
            hideAfterMillis = 1_000,
            modifier = Modifier
                .align(Alignment.Center)
        ) {
            SeekAmountIndicator(
                deltaMs = it,
            )
        }

        EmptyValueTimedVisibility(
            value = horizontalSeekPreviewPositionMs,
            hideAfterMillis = 1_000,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) { previewPositionMs ->
            if (!controlsVisible) {
                HiddenSeekTimeline(
                    positionMs = previewPositionMs,
                    durationMs = uiState.durationMs,
                    bufferedMs = uiState.bufferedMs,
                    chapterMarkers = uiState.chapters,
                    adMarkers = uiState.ads,
                    isLive = uiState.isLive
                )
            }
        }

        ValueChangeTimedVisibility(
            value = BrightnessUiState(brightness, isAutoBrightness, autoDragTick),
            hideAfterMillis = 800,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 20.dp)
        ) { state ->
            PlayerAdjustmentIndicator(
                modifier = Modifier.align(Alignment.Center),
                icon = Icons.Outlined.BrightnessMedium,
                contentDescription = "Brightness",
                value = state.brightness,
                bottomText = if (state.isAuto) {
                    "Auto"
                } else {
                    "${(state.brightness * 100).roundToInt()}%"
                }
            )
        }

        ValueChangeTimedVisibility(
            value = volume,
            hideAfterMillis = 800,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 20.dp)
        ) { currentVolume ->
            PlayerAdjustmentIndicator(
                modifier = Modifier
                    .align(Alignment.Center),
                icon = Icons.Outlined.VolumeUp,
                contentDescription = "Volume",
                value = currentVolume,
                bottomText = "${(currentVolume.coerceIn(0f, 1f) * 100).roundToInt()}%"
            )
        }

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            PlayerControlsOverlay(
                modifier = Modifier.fillMaxSize(),
                uiState = uiState,
                overlayController = overlayController,
                onBack = onBack,
                onPlayPause = { viewModel.togglePlayPause() },
                onSeek = { viewModel.seekTo(it) },
                onSeekRelative = { delta -> viewModel.seekBy(delta) },
                onSeekLiveEdge = { viewModel.seekToLiveEdge() },
                onSkipSegment = { viewModel.skipActiveSegment() },
                onNext = { viewModel.next() },
                onPrevious = { viewModel.previous() },
                onSelectTrack = { viewModel.selectTrack(it) },
                onOpenQueue = { showQueuePanel = true }
            )
        }

        AnimatedVisibility(
            visible = showSkipIntroButton,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(
                    end = 24.dp,
                )
        ) {
            SkipSegmentButton(
                size = 72.dp,
                fontSize = 22,
                onClick = { viewModel.skipActiveSegment() }
            )
        }


        AnimatedVisibility(
            visible = showNextEpisodeOverlay,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 24.dp,
                    bottom = 24.dp
                )
        ) {
            uiState.nextEpisode?.let { nextEpisode ->
                NextEpisodeOverlay(
                    nextEpisode = nextEpisode,
                    onClick = { viewModel.next() }
                )
            }
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

        PersistentOverlayContainer(
            controller = overlayController,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun HiddenSeekTimeline(
    positionMs: Long,
    durationMs: Long,
    bufferedMs: Long,
    chapterMarkers: List<TimedMarker>,
    adMarkers: List<TimedMarker>,
    isLive: Boolean,
    modifier: Modifier = Modifier
) {
    val safeDuration = durationMs.takeIf { it > 0 } ?: 1L

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        PlayerTimeRow(
            positionMs = positionMs,
            durationMs = durationMs,
            isLive = isLive,
            onSeekLiveEdge = null
        )
        PlayerSeekBarTrack(
            positionMs = positionMs,
            durationMs = safeDuration,
            bufferedMs = bufferedMs,
            chapterMarkers = chapterMarkers,
            adMarkers = adMarkers,
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp),
            isFocused = false,
            focusedTrackHeight = 4.dp,
            focusedThumbRadius = 6.dp,
            focusedThumbHaloRadiusDelta = 0.dp
        )
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
            .background(Color.Black.copy(alpha = 0.9f))
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

private data class BrightnessUiState(val brightness: Float, val isAuto: Boolean, val dragTick: Int)

private fun readCurrentBrightness(activity: Activity?, context: Context): Float {
    val windowBrightness = activity?.window?.attributes?.screenBrightness
    if (windowBrightness != null && windowBrightness >= 0f) {
        return windowBrightness
    }
    return try {
        val systemBrightness = Settings.System.getInt(
            context.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS
        )
        (systemBrightness / 255f).coerceIn(0f, 1f)
    } catch (e: Settings.SettingNotFoundException) {
        0.5f
    }
}

private fun applyBrightness(activity: Activity?, brightness: Float, isAuto: Boolean) {
    activity ?: return
    val params = activity.window.attributes
    params.screenBrightness = if (isAuto) -1f else brightness
    activity.window.attributes = params
}

private fun restoreBrightness(activity: Activity?, originalValue: Float) {
    activity ?: return
    val params = activity.window.attributes
    params.screenBrightness = originalValue
    activity.window.attributes = params
}
