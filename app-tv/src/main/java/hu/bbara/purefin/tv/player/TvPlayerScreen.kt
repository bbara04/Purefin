package hu.bbara.purefin.tv.player

import android.app.Activity
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ClosedCaption
import androidx.compose.material.icons.outlined.Forward30
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.PlaylistPlay
import androidx.compose.material.icons.outlined.Replay10
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import hu.bbara.purefin.common.ui.components.PurefinAsyncImage
import hu.bbara.purefin.core.player.model.MarkerType
import hu.bbara.purefin.core.player.model.PlayerUiState
import hu.bbara.purefin.core.player.model.TimedMarker
import hu.bbara.purefin.core.player.model.TrackOption
import hu.bbara.purefin.core.player.viewmodel.PlayerViewModel

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
    var trackPanelType by remember { mutableStateOf<TrackPanelType?>(null) }

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

    LaunchedEffect(uiState.isPlaying) {
        if (uiState.isPlaying) showQueuePanel = false
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
                onBack = onBack,
                onPlayPause = { viewModel.togglePlayPause() },
                onSeek = { viewModel.seekTo(it) },
                onSeekRelative = { viewModel.seekBy(it) },
                onSeekLiveEdge = { viewModel.seekToLiveEdge() },
                onNext = { viewModel.next() },
                onPrevious = { viewModel.previous() },
                onOpenAudioPanel = { trackPanelType = TrackPanelType.AUDIO },
                onOpenSubtitlesPanel = { trackPanelType = TrackPanelType.SUBTITLES },
                onOpenQualityPanel = { trackPanelType = TrackPanelType.QUALITY },
                onOpenQueue = { showQueuePanel = true }
            )
        }

        TvPlayerStateCard(
            modifier = Modifier.align(Alignment.Center),
            uiState = uiState,
            onRetry = { viewModel.retry() },
            onNext = { viewModel.next() },
            onReplay = { viewModel.seekTo(0L); viewModel.togglePlayPause() },
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
            TvQueuePanel(
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

private enum class TrackPanelType { AUDIO, SUBTITLES, QUALITY }

@Composable
private fun TvPlayerControlsOverlay(
    uiState: PlayerUiState,
    onBack: () -> Unit,
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
            onBack = onBack,
            onOpenQueue = onOpenQueue,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        )
        TvPlayerBottomSection(
            uiState = uiState,
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
    onBack: () -> Unit,
    onOpenQueue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TvIconButton(
                icon = Icons.Outlined.ArrowBack,
                contentDescription = "Back",
                onClick = onBack
            )
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
        TvIconButton(
            icon = Icons.Outlined.PlaylistPlay,
            contentDescription = "Queue",
            onClick = onOpenQueue
        )
    }
}

@Composable
private fun TvPlayerBottomSection(
    uiState: PlayerUiState,
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
            onSeek = onSeek
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
                TvIconButton(
                    icon = Icons.Outlined.HighQuality,
                    contentDescription = "Quality",
                    onClick = onOpenQualityPanel
                )
                TvIconButton(
                    icon = Icons.Outlined.Language,
                    contentDescription = "Audio",
                    onClick = onOpenAudioPanel
                )
                TvIconButton(
                    icon = Icons.Outlined.ClosedCaption,
                    contentDescription = "Subtitles",
                    onClick = onOpenSubtitlesPanel
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun TvPlayerSeekBar(
    positionMs: Long,
    durationMs: Long,
    bufferedMs: Long,
    chapterMarkers: List<TimedMarker>,
    adMarkers: List<TimedMarker>,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val safeDuration = durationMs.takeIf { it > 0 } ?: 1L
    val position = positionMs.coerceIn(0, safeDuration)
    val bufferRatio = (bufferedMs.toFloat() / safeDuration).coerceIn(0f, 1f)
    val progressRatio = (position.toFloat() / safeDuration).coerceIn(0f, 1f)
    val combinedMarkers = chapterMarkers.map { it.copy(type = MarkerType.CHAPTER) } +
            adMarkers.map { it.copy(type = MarkerType.AD) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .height(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.dp, vertical = 10.dp)
        ) {
            val trackHeight = 4f
            val trackTop = size.height / 2 - trackHeight / 2
            drawRect(
                color = scheme.onSurface.copy(alpha = 0.2f),
                size = Size(width = size.width, height = trackHeight),
                topLeft = Offset(0f, trackTop)
            )
            drawRect(
                color = scheme.onSurface.copy(alpha = 0.4f),
                size = Size(width = bufferRatio * size.width, height = trackHeight),
                topLeft = Offset(0f, trackTop)
            )
            val progressWidth = progressRatio * size.width
            drawRect(
                color = scheme.primary,
                size = Size(width = progressWidth, height = trackHeight),
                topLeft = Offset(0f, trackTop)
            )
            val thumbRadius = 7.dp.toPx()
            drawCircle(
                color = scheme.primary,
                radius = thumbRadius,
                center = Offset(progressWidth.coerceIn(0f, size.width), size.height / 2)
            )
            combinedMarkers.forEach { marker ->
                val x = (marker.positionMs.toFloat() / safeDuration) * size.width
                val color = if (marker.type == MarkerType.AD) scheme.secondary else scheme.primary
                drawRect(
                    color = color,
                    topLeft = Offset(x - 1f, size.height / 2 - 6f),
                    size = Size(width = 2f, height = 12f)
                )
            }
        }
        Slider(
            value = position.toFloat(),
            onValueChange = { onSeek(it.toLong()) },
            valueRange = 0f..safeDuration.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = Color.Transparent,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun TvPlayerStateCard(
    uiState: PlayerUiState,
    onRetry: () -> Unit,
    onNext: () -> Unit,
    onReplay: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Box(modifier = modifier) {
        AnimatedVisibility(visible = uiState.isBuffering && uiState.error == null) {
            CircularProgressIndicator(color = scheme.primary)
        }

        AnimatedVisibility(visible = uiState.error != null) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(scheme.background.copy(alpha = 0.92f))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = uiState.error ?: "Playback error",
                    color = scheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onRetry) { Text("Retry") }
                    Button(
                        onClick = onDismissError,
                        colors = ButtonDefaults.buttonColors(containerColor = scheme.surface)
                    ) {
                        Text("Dismiss", color = scheme.onSurface)
                    }
                }
            }
        }

        AnimatedVisibility(visible = uiState.isEnded && uiState.error == null && !uiState.isBuffering) {
            val nextUp = uiState.queue.getOrNull(
                uiState.queue.indexOfFirst { it.isCurrent }.takeIf { it >= 0 }?.plus(1) ?: -1
            )
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(scheme.background.copy(alpha = 0.92f))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (nextUp != null) {
                    Text(
                        text = "Up next",
                        color = scheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = nextUp.title,
                        color = scheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Button(onClick = onNext) { Text("Play next") }
                } else {
                    Text(
                        text = "Playback finished",
                        color = scheme.onBackground,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Button(onClick = onReplay) { Text("Replay") }
                }
            }
        }
    }
}

@Composable
private fun TvTrackSelectionPanel(
    panelType: TrackPanelType,
    uiState: PlayerUiState,
    onSelect: (TrackOption) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val (title, options, selectedId) = when (panelType) {
        TrackPanelType.AUDIO -> Triple("Audio", uiState.audioTracks, uiState.selectedAudioTrackId)
        TrackPanelType.SUBTITLES -> Triple("Subtitles", uiState.textTracks, uiState.selectedTextTrackId)
        TrackPanelType.QUALITY -> Triple("Quality", uiState.qualityTracks, uiState.selectedQualityTrackId)
    }

    Box(
        modifier = modifier.fillMaxSize(),
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        color = scheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TvIconButton(
                        icon = Icons.Outlined.ArrowBack,
                        contentDescription = "Close",
                        onClick = onClose
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Column(
                    modifier = Modifier
                        .heightIn(max = 500.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    options.forEach { option ->
                        val selected = option.id == selectedId
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selected) scheme.primary.copy(alpha = 0.15f)
                                    else scheme.surfaceVariant.copy(alpha = 0.6f)
                                )
                                .clickable { onSelect(option) }
                                .padding(horizontal = 20.dp, vertical = 14.dp)
                        ) {
                            Text(
                                text = option.label,
                                color = scheme.onSurface,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TvQueuePanel(
    uiState: PlayerUiState,
    onSelect: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier.fillMaxSize(),
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Up next",
                        color = scheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TvIconButton(
                        icon = Icons.Outlined.ArrowBack,
                        contentDescription = "Close",
                        onClick = onClose
                    )
                }
                if (uiState.queue.isEmpty()) {
                    Text(
                        text = "No items in queue",
                        color = scheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(uiState.queue) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (item.isCurrent) scheme.primary.copy(alpha = 0.15f)
                                        else scheme.surfaceVariant.copy(alpha = 0.8f)
                                    )
                                    .clickable { onSelect(item.id) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(72.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(scheme.surfaceVariant)
                                ) {
                                    if (item.artworkUrl != null) {
                                        PurefinAsyncImage(
                                            model = item.artworkUrl,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(4f / 3f)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        color = scheme.onSurface,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (item.isCurrent) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    item.subtitle?.let { subtitle ->
                                        Text(
                                            text = subtitle,
                                            color = scheme.onSurface.copy(alpha = 0.7f),
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TvIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    size: Int = 52,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .widthIn(min = size.dp)
            .height(size.dp)
            .clip(RoundedCornerShape(50))
            .background(scheme.background.copy(alpha = 0.65f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = scheme.onBackground,
            modifier = Modifier.padding(8.dp)
        )
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
