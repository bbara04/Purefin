package hu.bbara.purefin.ui.screen.player

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
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
import hu.bbara.purefin.ui.common.visual.ValueChangeTimedVisibility
import hu.bbara.purefin.ui.screen.player.components.PlayerSeekBarTrack
import hu.bbara.purefin.ui.screen.player.components.TvIconButton
import hu.bbara.purefin.ui.screen.player.components.TvNextEpisodeOverlay
import hu.bbara.purefin.ui.screen.player.components.TvPlayerControlsOverlay
import hu.bbara.purefin.ui.screen.player.components.TvPlayerLoadingErrorEndCard
import hu.bbara.purefin.ui.screen.player.components.TvPlayerTimeRow
import hu.bbara.purefin.ui.screen.player.components.TvTrackPanelType
import hu.bbara.purefin.ui.screen.player.components.TvTrackSelectionPanel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val TV_CONTROLS_AUTO_HIDE_MS = 5_000L
private const val CONTROLS_VISIBLE_SUBTITLE_BOTTOM_PADDING_FRACTION = 0.22f
internal const val TV_HIDDEN_STOP_FEEDBACK_MS = 1_200L
internal const val TvPlayerHiddenStopFeedbackTag = "tv_player_hidden_stop_feedback"

@OptIn(UnstableApi::class)
@Composable
fun TvPlayerScreen(
    mediaId: String,
    viewModel: PlayerViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    LaunchedEffect(mediaId) {
        viewModel.loadMedia(mediaId)
    }

    val scope = rememberCoroutineScope()
    var hideControlsJob: Job? by remember { mutableStateOf(null) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var controlsVisible by remember { mutableStateOf(true) }
    var isPlaylistExpanded by remember { mutableStateOf(false) }
    var trackPanelType by remember { mutableStateOf<TvTrackPanelType?>(null) }
    var pendingTrackButtonFocus by remember { mutableStateOf<TvTrackPanelType?>(null) }

    // This is a hack for timed visibility.
    var resumeStopFeedbackCounter by remember { mutableIntStateOf(0) }
    var hiddenSeekCounter by remember { mutableIntStateOf(0) }


    val context = LocalContext.current


    val backgroundFocusRequester = remember { FocusRequester() }

    // Main section focus requesters
    var rootFocusRequester by remember { mutableStateOf(backgroundFocusRequester) }
    var controlsFocusRequester by remember { mutableStateOf(FocusRequester() ) }

    val qualityButtonFocusRequester = remember { FocusRequester() }
    val audioButtonFocusRequester = remember { FocusRequester() }
    val subtitlesButtonFocusRequester = remember { FocusRequester() }
    val skipButtonFocusRequester = remember { FocusRequester() }
    val nextEpisodeFocusRequester = remember { FocusRequester() }

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        viewModel.pausePlayback()
    }
    DisposableEffect(Unit) {
        onDispose {
            (context as? Activity)?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
    LaunchedEffect(uiState.isPlaying) {
        val activity = context as? Activity
        if (uiState.isPlaying) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    fun showControls() {
        controlsVisible = true
    }
    fun hideControls() {
        controlsVisible = false
    }
    fun hideControlsWithTimeout() {
        hideControlsJob?.cancel()
        if (controlsVisible && !isPlaylistExpanded && trackPanelType == null && !uiState.isEnded && uiState.error == null) {
            hideControlsJob = scope.launch {
                delay(TV_CONTROLS_AUTO_HIDE_MS)
                hideControls()
            }
        }
    }
    // Needed because of composition scheduling
    LaunchedEffect(controlsVisible) {
        if (controlsVisible) {
            controlsFocusRequester.requestFocus()
        } else {
            rootFocusRequester.requestFocus()
        }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        hideControlsWithTimeout()
    }

    fun expandPlaylist() {
        // TODO: focus management for playlist expansion
        isPlaylistExpanded = true
    }
    fun closePlaylist() {
        // TODO focus
        isPlaylistExpanded = false
    }
    val closeTrackPanel: () -> Unit = {
        trackPanelType?.let { panelType ->
            pendingTrackButtonFocus = panelType
            trackPanelType = null
        }
    }


    val showSkipIntroButton = !controlsVisible
        && uiState.activeSkippableSegmentEndMs != null
        && uiState.activeSkippableSegmentType == SegmentType.INTRO
        && !uiState.isEnded
    val showNextEpisodeOverlay = !controlsVisible
        && uiState.nextEpisode != null
        && uiState.durationMs > 0L
        && ((uiState.durationMs - uiState.positionMs) <= 30_000L || uiState.activeSkippableSegmentType == SegmentType.OUTRO)
        && !uiState.isEnded
    LaunchedEffect(showSkipIntroButton, showNextEpisodeOverlay) {
        rootFocusRequester = when {
            showSkipIntroButton -> {
                skipButtonFocusRequester
            }
            showNextEpisodeOverlay -> {
                nextEpisodeFocusRequester
            }
            else -> {
                backgroundFocusRequester
            }
        }
        if (!controlsVisible) {
            rootFocusRequester.requestFocus()
        }
    }


    LaunchedEffect(trackPanelType, pendingTrackButtonFocus) {
        val pendingFocus = pendingTrackButtonFocus ?: return@LaunchedEffect
        if (trackPanelType != null) return@LaunchedEffect
        when (pendingFocus) {
            TvTrackPanelType.AUDIO -> audioButtonFocusRequester.requestFocus()
            TvTrackPanelType.SUBTITLES -> subtitlesButtonFocusRequester.requestFocus()
            TvTrackPanelType.QUALITY -> qualityButtonFocusRequester.requestFocus()
        }
        pendingTrackButtonFocus = null
    }

    val subtitleBottomPaddingFraction =
        if (controlsVisible) {
            CONTROLS_VISIBLE_SUBTITLE_BOTTOM_PADDING_FRACTION
        } else {
            SubtitleView.DEFAULT_BOTTOM_PADDING_FRACTION
        }

    BackHandler(enabled = true) {
        when {
            trackPanelType != null -> closeTrackPanel()
            isPlaylistExpanded -> {
                closePlaylist()
            }
            controlsVisible -> {
                hideControls()
            }
            else -> onBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(rootFocusRequester)
            .onKeyEvent { event ->
                val handled = handleTvPlayerRootKeyEvent(
                    event = event,
                    controlsVisible = controlsVisible,
                    popupVisible = showSkipIntroButton || showNextEpisodeOverlay,
                    onShowControls = ::showControls,
                    onTogglePlayback = {
                        // This is a hack to trigger the ValueChangeTimedVisibility to show the hidden resume/stop feedback.
                        resumeStopFeedbackCounter++
                        viewModel.togglePlayPause()
                    },
                    onSeekRelative = {
                        // This is a hack to trigger the ValueChangeTimedVisibility to show the hidden seek timeline.
                        hiddenSeekCounter++
                        viewModel.seekBy(it)
                    },
                )
                if (event.type == KeyEventType.KeyDown) {
                    hideControlsWithTimeout()
                }
                handled
            }
            .focusable()
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
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.Center)
        )

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            TvPlayerControlsOverlay(
                modifier = Modifier.fillMaxSize(),
                uiState = uiState,
                focusRequester = controlsFocusRequester,
                isPlaylistExpanded = isPlaylistExpanded,
                qualityButtonFocusRequester = qualityButtonFocusRequester,
                audioButtonFocusRequester = audioButtonFocusRequester,
                subtitlesButtonFocusRequester = subtitlesButtonFocusRequester,
                onPlayPause = { viewModel.togglePlayPause() },
                onSeek = { positionMs ->
                    viewModel.seekTo(positionMs)
                },
                onSeekRelative = { deltaMs ->
                    viewModel.seekBy(deltaMs)
                },
                onSkipSegment = {
                    viewModel.skipActiveSegment()
                },
                onNext = { viewModel.next() },
                onPrevious = { viewModel.previous() },
                onOpenAudioPanel = { trackPanelType = TvTrackPanelType.AUDIO },
                onOpenSubtitlesPanel = { trackPanelType = TvTrackPanelType.SUBTITLES },
                onOpenQualityPanel = { trackPanelType = TvTrackPanelType.QUALITY },
                onExpandPlaylist = ::expandPlaylist,
                onCollapsePlaylist = ::closePlaylist,
                onSelectQueueItem = { id ->
                    viewModel.playQueueItem(id)
                },
                qualityButtonEnabled = uiState.qualityTracks.isNotEmpty(),
                audioButtonEnabled = uiState.audioTracks.isNotEmpty(),
                subtitlesButtonEnabled = uiState.textTracks.isNotEmpty()
            )
        }

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            TvPlayerClock()
        }

        AnimatedVisibility(
            visible = showSkipIntroButton,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 24.dp,
                    bottom = 24.dp
                )
        ) {
            TvIconButton(
                icon = Icons.Outlined.SkipNext,
                contentDescription = "Skip segment",
                onClick = { viewModel.skipActiveSegment() },
                size = 64,
                label = "Skip",
                modifier = Modifier.focusRequester(skipButtonFocusRequester)
            )
        }

        AnimatedVisibility(
            visible = showNextEpisodeOverlay,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 32.dp,
                    bottom = 140.dp
                )
        ) {
            uiState.nextEpisode?.let { nextEpisode ->
                TvNextEpisodeOverlay(
                    nextEpisode = nextEpisode,
                    onClick = { viewModel.next() },
                    modifier = Modifier.focusRequester(nextEpisodeFocusRequester)
                )
            }
        }

        if (!showSkipIntroButton && !controlsVisible) {
            ValueChangeTimedVisibility(
                value = hiddenSeekCounter,
                hideAfterMillis = 2500L,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 32.dp, vertical = 28.dp)
            ) {
                HiddenTvSeekTimeline(
                    positionMs = uiState.positionMs,
                    durationMs = uiState.durationMs,
                    bufferedMs = uiState.bufferedMs,
                    chapterMarkers = uiState.chapters,
                    adMarkers = uiState.ads
                )
            }
        }

        TvPlayerLoadingErrorEndCard(
            modifier = Modifier.align(Alignment.Center),
            uiState = uiState,
            onRetry = { viewModel.retry() },
            onNext = { viewModel.next() },
            onReplay = {
                viewModel.seekTo(0L)
                viewModel.resumePlayback()
            },
            onDismissError = { viewModel.clearError() }
        )

        ValueChangeTimedVisibility(
            value = resumeStopFeedbackCounter,
            hideAfterMillis = TV_HIDDEN_STOP_FEEDBACK_MS,
            modifier = Modifier.align(Alignment.Center)
        ) {
            TvPlayerStopFeedback(stopped = !uiState.isPlaying)
        }

        AnimatedVisibility(
            visible = trackPanelType != null,
            enter = slideInHorizontally { it },
            exit = slideOutHorizontally { it }
        ) {
            trackPanelType?.let { panelType ->
                TvTrackSelectionPanel(
                    panelType = panelType,
                    uiState = uiState,
                    onSelect = { track ->
                        viewModel.selectTrack(track)
                        closeTrackPanel()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

    }
}

@Composable
private fun HiddenTvSeekTimeline(
    positionMs: Long,
    durationMs: Long,
    bufferedMs: Long,
    chapterMarkers: List<TimedMarker>,
    adMarkers: List<TimedMarker>,
    modifier: Modifier = Modifier
) {
    val safeDuration = durationMs.takeIf { it > 0 } ?: 1L

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Transparent)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        TvPlayerTimeRow(
            positionMs = positionMs,
            durationMs = durationMs,
            modifier = Modifier.fillMaxWidth()
        )
        PlayerSeekBarTrack(
            positionMs = positionMs,
            durationMs = safeDuration,
            bufferedMs = bufferedMs,
            chapterMarkers = chapterMarkers,
            adMarkers = adMarkers,
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp),
            isFocused = false,
            thumbRadius = 7.dp,
            focusedThumbRadius = 9.dp,
            focusedThumbHaloRadiusDelta = 0.dp
        )
    }
}

@Composable
private fun TvPlayerClock(modifier: Modifier = Modifier) {
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(1_000L)
        }
    }
    val formatter = remember { DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()) }
    Text(
        text = formatter.format(currentTime),
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

internal fun handleTvPlayerRootKeyEvent(
    event: KeyEvent,
    controlsVisible: Boolean,
    popupVisible: Boolean,
    onTogglePlayback: () -> Unit,
    onSeekRelative: (Long) -> Unit,
    onShowControls: () -> Unit,
): Boolean {
    if (event.type != KeyEventType.KeyDown) return false

    if (!controlsVisible) {
        return when (event.key) {
            Key.DirectionLeft -> {
                onSeekRelative(-10_000)
                true
            }

            Key.DirectionRight -> {
                onSeekRelative(30_000)
                true
            }

            Key.DirectionUp, Key.DirectionDown -> {
                onShowControls()
                true
            }

            Key.DirectionCenter, Key.Enter -> {
                if (popupVisible) {
                    // Do nothing because the focused component is not he root, but an overlay
                    false
                } else {
                    onTogglePlayback()
                    true
                }
            }

            else -> false
        }
    }

    return false
}

@Composable
internal fun TvPlayerStopFeedback(
    stopped: Boolean,
    modifier: Modifier = Modifier
) {
    if (!stopped) {
        return
    }
    Box(
        modifier = modifier
            .testTag(TvPlayerHiddenStopFeedbackTag)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.72f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Pause,
            contentDescription = "Play/Pause playback",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(72.dp)
        )
    }
}
