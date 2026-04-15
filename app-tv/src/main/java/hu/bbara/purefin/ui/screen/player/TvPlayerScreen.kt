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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import hu.bbara.purefin.core.player.viewmodel.ControlsAutoHideBlocker
import hu.bbara.purefin.core.player.viewmodel.PlayerViewModel
import hu.bbara.purefin.ui.screen.player.components.TvPlayerControlsOverlay
import hu.bbara.purefin.ui.screen.player.components.TvPlayerLoadingErrorEndCard
import hu.bbara.purefin.ui.screen.player.components.TvTrackPanelType
import hu.bbara.purefin.ui.screen.player.components.TvTrackSelectionPanel
import kotlinx.coroutines.delay

private const val TV_CONTROLS_AUTO_HIDE_MS = 5_000L
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

    val uiState by viewModel.uiState.collectAsState()
    val controlsVisible by viewModel.controlsVisible.collectAsState()
    var isPlaylistExpanded by remember { mutableStateOf(false) }
    var trackPanelType by remember { mutableStateOf<TvTrackPanelType?>(null) }
    var pendingTrackButtonFocus by remember { mutableStateOf<TvTrackPanelType?>(null) }
    var stopFeedbackVisible by remember { mutableStateOf(false) }
    var stopFeedbackRequestId by remember { mutableStateOf(0) }
    val controlsAutoHideBlocked = isPlaylistExpanded || trackPanelType != null

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.setControlsAutoHideDelay(TV_CONTROLS_AUTO_HIDE_MS)
    }
    LaunchedEffect(uiState.isPlaying) {
        val activity = context as? Activity
        if (uiState.isPlaying) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            (context as? Activity)?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            viewModel.setControlsAutoHideBlocked(ControlsAutoHideBlocker.PLAYLIST, false)
            viewModel.setControlsAutoHideBlocked(ControlsAutoHideBlocker.TRACK_PANEL, false)
        }
    }

    LaunchedEffect(isPlaylistExpanded) {
        viewModel.setControlsAutoHideBlocked(ControlsAutoHideBlocker.PLAYLIST, isPlaylistExpanded)
    }

    LaunchedEffect(trackPanelType) {
        viewModel.setControlsAutoHideBlocked(
            ControlsAutoHideBlocker.TRACK_PANEL,
            trackPanelType != null
        )
    }

    val hiddenControlFocusRequester = remember { FocusRequester() }
    val controlsFocusRequester = remember { FocusRequester() }
    val qualityButtonFocusRequester = remember { FocusRequester() }
    val audioButtonFocusRequester = remember { FocusRequester() }
    val subtitlesButtonFocusRequester = remember { FocusRequester() }
    val expandPlaylist: () -> Unit = {
        if (!isPlaylistExpanded) {
            isPlaylistExpanded = true
        }
    }
    val collapsePlaylistToControls: () -> Unit = {
        if (isPlaylistExpanded) {
            isPlaylistExpanded = false
            viewModel.showControls(TV_CONTROLS_AUTO_HIDE_MS)
        }
    }
    val showTvControls: () -> Unit = {
        viewModel.showControls(TV_CONTROLS_AUTO_HIDE_MS)
    }
    val togglePlayPauseAndShowControls: () -> Unit = {
        viewModel.togglePlayPause(TV_CONTROLS_AUTO_HIDE_MS)
    }
    val pausePlaybackWithoutShowingControls: () -> Unit = {
        viewModel.pausePlayback()
        stopFeedbackVisible = true
        stopFeedbackRequestId += 1
    }
    val resumePlaybackWithoutShowingControls: () -> Unit = {
        viewModel.resumePlayback()
        stopFeedbackVisible = false
    }
    val seekAndShowControls: (Long) -> Unit = { positionMs ->
        viewModel.seekTo(positionMs)
        showTvControls()
    }
    val seekByAndShowControls: (Long) -> Unit = { deltaMs ->
        viewModel.seekBy(deltaMs)
        showTvControls()
    }
    val seekByWithoutShowingControls: (Long) -> Unit = { deltaMs ->
        viewModel.seekBy(deltaMs)
        stopFeedbackVisible = false
    }
    val seekToLiveEdgeAndShowControls: () -> Unit = {
        viewModel.seekToLiveEdge()
        showTvControls()
    }
    val nextAndShowControls: () -> Unit = {
        viewModel.next(TV_CONTROLS_AUTO_HIDE_MS)
    }
    val previousAndShowControls: () -> Unit = {
        viewModel.previous(TV_CONTROLS_AUTO_HIDE_MS)
    }
    val closeTrackPanel: () -> Unit = {
        trackPanelType?.let { panelType ->
            pendingTrackButtonFocus = panelType
            trackPanelType = null
            viewModel.showControls(TV_CONTROLS_AUTO_HIDE_MS)
        }
    }

    LaunchedEffect(controlsVisible, controlsAutoHideBlocked) {
        if (controlsAutoHideBlocked) return@LaunchedEffect
        if (controlsVisible) {
            controlsFocusRequester.requestFocus()
        } else {
            hiddenControlFocusRequester.requestFocus()
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

    LaunchedEffect(stopFeedbackRequestId) {
        if (stopFeedbackRequestId == 0) return@LaunchedEffect
        delay(TV_HIDDEN_STOP_FEEDBACK_MS)
        stopFeedbackVisible = false
    }

    LaunchedEffect(controlsVisible, isPlaylistExpanded, trackPanelType, uiState.isEnded, uiState.error) {
        if (controlsVisible || isPlaylistExpanded || trackPanelType != null || uiState.isEnded || uiState.error != null) {
            stopFeedbackVisible = false
        }
    }

    BackHandler(enabled = true) {
        when {
            trackPanelType != null -> closeTrackPanel()
            isPlaylistExpanded -> collapsePlaylistToControls()
            controlsVisible -> viewModel.toggleControlsVisibility()
            else -> onBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(hiddenControlFocusRequester)
            .onPreviewKeyEvent { event ->
                handleTvPlayerRootKeyEvent(
                    event = event,
                    isPlaying = uiState.isPlaying,
                    controlsVisible = controlsVisible,
                    isPlaylistExpanded = isPlaylistExpanded,
                    trackPanelType = trackPanelType,
                    onCloseTrackPanel = closeTrackPanel,
                    onCollapsePlaylist = collapsePlaylistToControls,
                    onHideControls = { viewModel.toggleControlsVisibility() },
                    onPausePlaybackWithoutShowingControls = pausePlaybackWithoutShowingControls,
                    onResumePlaybackWithoutShowingControls = resumePlaybackWithoutShowingControls,
                    onSeekRelative = seekByWithoutShowingControls,
                    onShowControls = showTvControls,
                    onTogglePlayPause = togglePlayPauseAndShowControls
                )
            }
            .focusable()
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    player = viewModel.player
                }
            },
            update = { it.player = viewModel.player },
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.Center)
        )

        AnimatedVisibility(
            visible = controlsVisible || isPlaylistExpanded || trackPanelType != null || uiState.isEnded || uiState.error != null,
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
                onPlayPause = togglePlayPauseAndShowControls,
                onSeek = seekAndShowControls,
                onSeekRelative = seekByAndShowControls,
                onSeekLiveEdge = seekToLiveEdgeAndShowControls,
                onNext = nextAndShowControls,
                onPrevious = previousAndShowControls,
                onOpenAudioPanel = { trackPanelType = TvTrackPanelType.AUDIO },
                onOpenSubtitlesPanel = { trackPanelType = TvTrackPanelType.SUBTITLES },
                onOpenQualityPanel = { trackPanelType = TvTrackPanelType.QUALITY },
                onExpandPlaylist = expandPlaylist,
                onCollapsePlaylist = collapsePlaylistToControls,
                onSelectQueueItem = { id ->
                    viewModel.playQueueItem(id, TV_CONTROLS_AUTO_HIDE_MS)
                    collapsePlaylistToControls()
                },
                qualityButtonEnabled = uiState.qualityTracks.isNotEmpty(),
                audioButtonEnabled = uiState.audioTracks.isNotEmpty(),
                subtitlesButtonEnabled = uiState.textTracks.isNotEmpty()
            )
        }

        TvPlayerLoadingErrorEndCard(
            modifier = Modifier.align(Alignment.Center),
            uiState = uiState,
            onRetry = { viewModel.retry() },
            onNext = nextAndShowControls,
            onReplay = {
                viewModel.seekTo(0L)
                togglePlayPauseAndShowControls()
            },
            onDismissError = { viewModel.clearError() }
        )

        AnimatedVisibility(
            visible = stopFeedbackVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            TvPlayerHiddenStopFeedback()
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

internal fun handleTvPlayerRootKeyEvent(
    event: androidx.compose.ui.input.key.KeyEvent,
    isPlaying: Boolean,
    controlsVisible: Boolean,
    isPlaylistExpanded: Boolean,
    trackPanelType: TvTrackPanelType?,
    onCloseTrackPanel: () -> Unit,
    onCollapsePlaylist: () -> Unit,
    onHideControls: () -> Unit,
    onPausePlaybackWithoutShowingControls: () -> Unit,
    onResumePlaybackWithoutShowingControls: () -> Unit,
    onSeekRelative: (Long) -> Unit,
    onShowControls: () -> Unit,
    onTogglePlayPause: () -> Unit
): Boolean {
    if (event.type != KeyEventType.KeyDown) return false

    if (event.key == Key.Back || event.key == Key.Escape) {
        return when {
            trackPanelType != null -> {
                onCloseTrackPanel()
                true
            }

            isPlaylistExpanded -> {
                onCollapsePlaylist()
                true
            }

            controlsVisible -> {
                onHideControls()
                true
            }

            else -> false
        }
    }

    if (!controlsVisible) {
        return when (event.key) {
            Key.DirectionLeft -> {
                onSeekRelative(-10_000)
                true
            }

            Key.DirectionRight -> {
                onSeekRelative(10_000)
                true
            }

            Key.DirectionUp, Key.DirectionDown -> {
                onShowControls()
                true
            }

            Key.DirectionCenter, Key.Enter -> {
                if (isPlaying) {
                    onPausePlaybackWithoutShowingControls()
                } else {
                    onResumePlaybackWithoutShowingControls()
                }
                true
            }

            else -> false
        }
    }

    return false
}

@Composable
internal fun TvPlayerHiddenStopFeedback(
    modifier: Modifier = Modifier
) {
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
            contentDescription = "Pause playback",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(72.dp)
        )
    }
}
