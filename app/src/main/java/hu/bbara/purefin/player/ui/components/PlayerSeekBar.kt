package hu.bbara.purefin.player.ui.components

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.player.model.MarkerType
import hu.bbara.purefin.player.model.TimedMarker

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
    val position = positionMs.coerceIn(0, safeDuration)
    val bufferRatio = (bufferedMs.toFloat() / safeDuration).coerceIn(0f, 1f)
    val combinedMarkers = chapterMarkers.map { it.copy(type = MarkerType.CHAPTER) } + adMarkers.map { it.copy(type = MarkerType.AD) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .height(32.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.dp, vertical = 10.dp)
        ) {
            // Buffered bar
            val bufferWidth = bufferRatio * size.width
            drawRect(
                color = scheme.onSurface.copy(alpha = 0.2f),
                size = Size(width = size.width, height = 4f),
                topLeft = Offset(0f, size.height / 2 - 2f)
            )
            drawRect(
                color = scheme.onSurface.copy(alpha = 0.4f),
                size = Size(width = bufferWidth, height = 4f),
                topLeft = Offset(0f, size.height / 2 - 2f)
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
            value = position.toFloat(),
            onValueChange = { newValue ->
                onScrubStarted()
                onSeek(newValue.toLong())
            },
            onValueChangeFinished = onScrubFinished,
            valueRange = 0f..safeDuration.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = scheme.primary,
                activeTrackColor = scheme.primary,
                inactiveTrackColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxSize()
        )
    }
}
