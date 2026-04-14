package hu.bbara.purefin.ui.screen.player.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.core.player.model.MarkerType
import hu.bbara.purefin.core.player.model.TimedMarker

@Composable
fun PlayerSeekBar(
    positionMs: Long,
    durationMs: Long,
    bufferedMs: Long,
    chapterMarkers: List<TimedMarker>,
    adMarkers: List<TimedMarker>,
    onSeek: (Long) -> Unit,
    onScrubStarted: () -> Unit,
    onScrubFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val safeDuration = durationMs.takeIf { it > 0 } ?: 1L
    val currentPosition = positionMs.coerceIn(0, safeDuration)
    var sliderPosition by remember { mutableFloatStateOf(currentPosition.toFloat()) }
    var isScrubbing by remember { mutableStateOf(false) }
    val sliderValue = if (isScrubbing) sliderPosition else currentPosition.toFloat()
    val bufferRatio = (bufferedMs.toFloat() / safeDuration).coerceIn(0f, 1f)
    val combinedMarkers = chapterMarkers.map { it.copy(type = MarkerType.CHAPTER) } + adMarkers.map { it.copy(type = MarkerType.AD) }
    val progressRatio = (sliderValue / safeDuration).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
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
            // Buffered bar
            val bufferWidth = bufferRatio * size.width
            drawRect(
                color = scheme.onSurface.copy(alpha = 0.2f),
                size = Size(width = size.width, height = trackHeight),
                topLeft = Offset(0f, trackTop)
            )
            drawRect(
                color = scheme.onSurface.copy(alpha = 0.4f),
                size = Size(width = bufferWidth, height = trackHeight),
                topLeft = Offset(0f, trackTop)
            )
            // Primary progress sits on top of the buffer line to match its width
            val progressWidth = progressRatio * size.width
            drawRect(
                color = scheme.primary,
                size = Size(width = progressWidth, height = trackHeight),
                topLeft = Offset(0f, trackTop)
            )
            // Draw a compact dot indicator instead of the default thumb
            val thumbRadius = 6.dp.toPx()
            drawCircle(
                color = scheme.primary,
                radius = thumbRadius,
                center = Offset(progressWidth.coerceIn(0f, size.width), size.height / 2)
            )
            // Markers
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
            value = sliderValue,
            onValueChange = { newValue ->
                if (!isScrubbing) {
                    isScrubbing = true
                    onScrubStarted()
                }
                sliderPosition = newValue
            },
            onValueChangeFinished = {
                val targetPosition = sliderPosition.toLong().coerceIn(0L, safeDuration)
                isScrubbing = false
                onSeek(targetPosition)
                onScrubFinished()
            },
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
