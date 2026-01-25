package hu.bbara.purefin.player.ui.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun PlayerGesturesLayer(
    modifier: Modifier = Modifier,
    onTap: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekBackward: () -> Unit,
    onVerticalDragLeft: (delta: Float) -> Unit,
    onVerticalDragRight: (delta: Float) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() },
                    onDoubleTap = { offset ->
                        val half = size.width / 2
                        if (offset.x < half) {
                            onSeekBackward()
                        } else {
                            onSeekForward()
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    val horizontalThreshold = size.width / 2
                    if (change.position.x < horizontalThreshold) {
                        onVerticalDragLeft(dragAmount.y)
                    } else {
                        onVerticalDragRight(dragAmount.y)
                    }
                }
            }
    )
}
