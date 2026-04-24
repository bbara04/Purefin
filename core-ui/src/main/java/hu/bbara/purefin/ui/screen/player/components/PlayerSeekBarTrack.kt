package hu.bbara.purefin.ui.screen.player.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.player.model.MarkerType
import hu.bbara.purefin.player.model.TimedMarker

@Composable
fun PlayerSeekBarTrack(
    positionMs: Long,
    durationMs: Long,
    bufferedMs: Long,
    chapterMarkers: List<TimedMarker>,
    adMarkers: List<TimedMarker>,
    modifier: Modifier = Modifier,
    isFocused: Boolean = false,
    trackHeight: Dp = 4.dp,
    focusedTrackHeight: Dp = 6.dp,
    thumbRadius: Dp = 6.dp,
    focusedThumbRadius: Dp = 9.dp,
    focusedThumbHaloRadiusDelta: Dp = 4.dp
) {
    val scheme = MaterialTheme.colorScheme
    val safeDuration = durationMs.takeIf { it > 0 } ?: 1L
    val position = positionMs.coerceIn(0, safeDuration)
    val bufferRatio = (bufferedMs.toFloat() / safeDuration).coerceIn(0f, 1f)
    val progressRatio = (position.toFloat() / safeDuration).coerceIn(0f, 1f)
    val combinedMarkers = chapterMarkers.map { it.copy(type = MarkerType.CHAPTER) } +
        adMarkers.map { it.copy(type = MarkerType.AD) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 2.dp, vertical = 10.dp)
    ) {
        val activeTrackHeight = if (isFocused) focusedTrackHeight.toPx() else trackHeight.toPx()
        val trackTop = size.height / 2 - activeTrackHeight / 2
        drawRect(
            color = scheme.onSurface.copy(alpha = 0.2f),
            size = Size(width = size.width, height = activeTrackHeight),
            topLeft = Offset(0f, trackTop)
        )
        drawRect(
            color = scheme.onSurface.copy(alpha = 0.4f),
            size = Size(width = bufferRatio * size.width, height = activeTrackHeight),
            topLeft = Offset(0f, trackTop)
        )
        val progressWidth = progressRatio * size.width
        drawRect(
            color = scheme.primary,
            size = Size(width = progressWidth, height = activeTrackHeight),
            topLeft = Offset(0f, trackTop)
        )

        val activeThumbRadius = if (isFocused) focusedThumbRadius.toPx() else thumbRadius.toPx()
        val thumbCenter = Offset(progressWidth.coerceIn(0f, size.width), size.height / 2)
        drawCircle(
            color = scheme.primary,
            radius = activeThumbRadius,
            center = thumbCenter
        )
        if (isFocused) {
            drawCircle(
                color = scheme.primary.copy(alpha = 0.3f),
                radius = activeThumbRadius + focusedThumbHaloRadiusDelta.toPx(),
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
