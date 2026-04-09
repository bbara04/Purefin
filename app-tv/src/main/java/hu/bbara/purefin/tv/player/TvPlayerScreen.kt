package hu.bbara.purefin.tv.player

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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import hu.bbara.purefin.core.player.viewmodel.PlayerViewModel
import hu.bbara.purefin.tv.player.components.TvPlayerControlsOverlay
import hu.bbara.purefin.tv.player.components.TvPlayerLoadingErrorEndCard
import hu.bbara.purefin.tv.player.components.TvTrackPanelType
import hu.bbara.purefin.tv.player.components.TvTrackSelectionPanel

private const val TV_CONTROLS_AUTO_HIDE_MS = 5_000L

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

    val context = LocalContext.current
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
        }
    }

    LaunchedEffect(uiState.isPlaying, controlsVisible, isPlaylistExpanded) {
        if (uiState.isPlaying && controlsVisible && !isPlaylistExpanded) {
            viewModel.showControls(TV_CONTROLS_AUTO_HIDE_MS)
        }
    }

    val hiddenControlFocusRequester = remember { FocusRequester() }
    val controlsFocusRequester = remember { FocusRequester() }
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
    val seekAndShowControls: (Long) -> Unit = { positionMs ->
        viewModel.seekTo(positionMs)
        showTvControls()
    }
    val seekByAndShowControls: (Long) -> Unit = { deltaMs ->
        viewModel.seekBy(deltaMs)
        showTvControls()
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

    LaunchedEffect(controlsVisible) {
        if (controlsVisible) {
            controlsFocusRequester.requestFocus()
        } else {
            hiddenControlFocusRequester.requestFocus()
        }
    }

    BackHandler(enabled = true) {
        when {
            trackPanelType != null -> trackPanelType = null
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
                if (!controlsVisible && event.type == KeyEventType.KeyDown) {
                    when (event.key) {
                        Key.DirectionLeft -> {
                            seekByAndShowControls(-10_000)
                            true
                        }

                        Key.DirectionRight -> {
                            seekByAndShowControls(10_000)
                            true
                        }

                        Key.DirectionUp, Key.DirectionDown -> {
                            showTvControls()
                            true
                        }

                        Key.DirectionCenter, Key.Enter -> {
                            togglePlayPauseAndShowControls()
                            true
                        }

                        else -> false
                    }
                } else {
                    false
                }
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
            visible = controlsVisible || isPlaylistExpanded || uiState.isEnded || uiState.error != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            TvPlayerControlsOverlay(
                modifier = Modifier.fillMaxSize(),
                uiState = uiState,
                focusRequester = controlsFocusRequester,
                isPlaylistExpanded = isPlaylistExpanded,
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
                }
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
                        trackPanelType = null
                    },
                    onClose = { trackPanelType = null },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

    }
}
