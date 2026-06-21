package hu.bbara.purefin.ui.screen.player.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.core.player.model.TimedMarker

@Composable
fun PlayerSeekBar(
    positionMs: Long,
    durationMs: Long,
    bufferedMs: Long,
    chapterMarkers: List<TimedMarker>,
    adMarkers: List<TimedMarker>,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val safeDuration = durationMs.takeIf { it > 0 } ?: 1L
    val currentPosition = positionMs.coerceIn(0, safeDuration)
    var sliderPosition by remember { mutableFloatStateOf(currentPosition.toFloat()) }
    var isScrubbing by remember { mutableStateOf(false) }
    val sliderValue = sliderPosition

    // Keep sliderPosition in sync with the player's position when the user is not
    // actively scrubbing. While scrubbing, the local drag value is preserved so the
    // thumb does not snap back to a stale positionMs prop before the seek is committed.
    LaunchedEffect(positionMs) {
        if (!isScrubbing) {
            sliderPosition = positionMs.toFloat()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .height(32.dp),
        contentAlignment = Alignment.Center
    ) {
        PlayerSeekBarTrack(
            positionMs = sliderValue.toLong(),
            durationMs = safeDuration,
            bufferedMs = bufferedMs,
            chapterMarkers = chapterMarkers,
            adMarkers = adMarkers,
            modifier = Modifier.fillMaxSize(),
            isFocused = false,
            focusedTrackHeight = 4.dp,
            focusedThumbRadius = 6.dp,
            focusedThumbHaloRadiusDelta = 0.dp
        )
        Slider(
            value = sliderValue,
            onValueChange = { newValue ->
                if (!isScrubbing) {
                    isScrubbing = true
                }
                sliderPosition = newValue
            },
            onValueChangeFinished = {
                val targetPosition = sliderPosition.toLong().coerceIn(0L, safeDuration)
                sliderPosition = targetPosition.toFloat()
                isScrubbing = false
                onSeek(targetPosition)
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
