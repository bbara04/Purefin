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
import hu.bbara.purefin.tv.player.components.TvPlayerQueuePanel
import hu.bbara.purefin.tv.player.components.TvTrackPanelType
import hu.bbara.purefin.tv.player.components.TvTrackSelectionPanel

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
    var showQueuePanel by remember { mutableStateOf(false) }
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

    BackHandler(enabled = controlsVisible) {
        viewModel.toggleControlsVisibility()
    }

    LaunchedEffect(uiState.isPlaying) {
        if (uiState.isPlaying && controlsVisible) {
            viewModel.toggleControlsVisibility()
        }
    }

    val hiddenControlFocusRequester = remember { FocusRequester() }
    val controlsFocusRequester = remember { FocusRequester() }

    LaunchedEffect(controlsVisible) {
        if (controlsVisible) {
            controlsFocusRequester.requestFocus()
        } else {
            hiddenControlFocusRequester.requestFocus()
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
                            viewModel.seekBy(-10_000)
                            true
                        }

                        Key.DirectionRight -> {
                            viewModel.seekBy(10_000)
                            true
                        }

                        Key.DirectionUp, Key.DirectionDown -> {
                            viewModel.showControls()
                            true
                        }

                        Key.DirectionCenter, Key.Enter -> {
                            viewModel.togglePlayPause()
                            viewModel.showControls()
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
            visible = controlsVisible || uiState.isEnded || uiState.error != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            TvPlayerControlsOverlay(
                modifier = Modifier.fillMaxSize(),
                uiState = uiState,
                focusRequester = controlsFocusRequester,
                onPlayPause = { viewModel.togglePlayPause() },
                onSeek = { viewModel.seekTo(it) },
                onSeekRelative = { viewModel.seekBy(it) },
                onSeekLiveEdge = { viewModel.seekToLiveEdge() },
                onNext = { viewModel.next() },
                onPrevious = { viewModel.previous() },
                onOpenAudioPanel = { trackPanelType = TvTrackPanelType.AUDIO },
                onOpenSubtitlesPanel = { trackPanelType = TvTrackPanelType.SUBTITLES },
                onOpenQualityPanel = { trackPanelType = TvTrackPanelType.QUALITY },
                onOpenQueue = { showQueuePanel = true }
            )
        }

        TvPlayerLoadingErrorEndCard(
            modifier = Modifier.align(Alignment.Center),
            uiState = uiState,
            onRetry = { viewModel.retry() },
            onNext = { viewModel.next() },
            onReplay = {
                viewModel.seekTo(0L)
                viewModel.togglePlayPause()
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

        AnimatedVisibility(
            visible = showQueuePanel,
            enter = slideInHorizontally { it },
            exit = slideOutHorizontally { it }
        ) {
            TvPlayerQueuePanel(
                uiState = uiState,
                onSelect = { id ->
                    viewModel.playQueueItem(id)
                    showQueuePanel = false
                },
                onClose = { showQueuePanel = false },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
