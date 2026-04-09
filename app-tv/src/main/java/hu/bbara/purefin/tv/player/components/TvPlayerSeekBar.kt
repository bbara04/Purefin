package hu.bbara.purefin.tv.player.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.core.player.model.MarkerType
import hu.bbara.purefin.core.player.model.TimedMarker

@Composable
internal fun TvPlayerSeekBar(
    positionMs: Long,
    durationMs: Long,
    bufferedMs: Long,
    chapterMarkers: List<TimedMarker>,
    adMarkers: List<TimedMarker>,
    onSeek: (Long) -> Unit,
    onSeekRelative: (Long) -> Unit,
    togglePlayState: () -> Unit,
    focusRequester: FocusRequester = remember { FocusRequester() },
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val safeDuration = durationMs.takeIf { it > 0 } ?: 1L
    val position = positionMs.coerceIn(0, safeDuration)
    val bufferRatio = (bufferedMs.toFloat() / safeDuration).coerceIn(0f, 1f)
    val progressRatio = (position.toFloat() / safeDuration).coerceIn(0f, 1f)
    val combinedMarkers = chapterMarkers.map { it.copy(type = MarkerType.CHAPTER) } +
        adMarkers.map { it.copy(type = MarkerType.AD) }
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .height(32.dp)
            .focusRequester(focusRequester)
            .onFocusChanged { isFocused = it.isFocused }
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when (event.key) {
                        Key.DirectionLeft -> {
                            onSeekRelative(-10_000)
                            true
                        }

                        Key.DirectionRight -> {
                            onSeekRelative(10_000)
                            true
                        }

                        Key.DirectionCenter,
                        Key.Enter,
                        Key.NumPadEnter -> {
                            togglePlayState()
                            true
                        }

                        else -> false
                    }
                } else {
                    false
                }
            }
            .focusable(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.dp, vertical = 10.dp)
        ) {
            val trackHeight = if (isFocused) 6f else 4f
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
            val thumbRadius = if (isFocused) 9.dp.toPx() else 7.dp.toPx()
            val thumbCenter = Offset(progressWidth.coerceIn(0f, size.width), size.height / 2)
            drawCircle(
                color = scheme.primary,
                radius = thumbRadius,
                center = thumbCenter
            )
            if (isFocused) {
                drawCircle(
                    color = scheme.primary.copy(alpha = 0.3f),
                    radius = thumbRadius + 4.dp.toPx(),
                    center = thumbCenter
                )
            }
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
    }
}
