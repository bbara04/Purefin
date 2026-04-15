package hu.bbara.purefin.ui.screen.player.components

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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
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
    onMoveDown: (() -> Boolean)? = null,
    focusRequester: FocusRequester = remember { FocusRequester() },
    modifier: Modifier = Modifier
) {
    val safeDuration = durationMs.takeIf { it > 0 } ?: 1L
    val position = positionMs.coerceIn(0, safeDuration)
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
                        Key.DirectionDown -> onMoveDown?.invoke() ?: false

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
        PlayerSeekBarTrack(
            positionMs = position,
            durationMs = safeDuration,
            bufferedMs = bufferedMs,
            chapterMarkers = chapterMarkers,
            adMarkers = adMarkers,
            modifier = Modifier.fillMaxSize(),
            isFocused = isFocused,
            thumbRadius = 7.dp,
            focusedThumbRadius = 9.dp,
            focusedThumbHaloRadiusDelta = 4.dp
        )
    }
}
