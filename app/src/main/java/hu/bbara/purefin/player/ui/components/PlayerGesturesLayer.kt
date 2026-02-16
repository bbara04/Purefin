package hu.bbara.purefin.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.player.helper.HorizontalSeekGestureHelper

@Composable
fun PlayerGesturesLayer(
    modifier: Modifier = Modifier,
    onTap: () -> Unit,
    onDoubleTapCenter: () -> Unit,
    onDoubleTapRight: () -> Unit,
    onDoubleTapLeft: () -> Unit,
    onVerticalDragLeft: (delta: Float) -> Unit,
    onVerticalDragRight: (delta: Float) -> Unit,
    onHorizontalDragPreview: (deltaMs: Long?) -> Unit = {},
    onHorizontalDrag: (deltaMs: Long) -> Unit,
) {
    val density = LocalDensity.current
    val horizontalThresholdPx = with(density) { HorizontalSeekGestureHelper.START_THRESHOLD.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth(0.90f)
            .fillMaxHeight(0.70f)
//            .background(Color(2f, 2f, 2f, 0.3f))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() },
                    onDoubleTap = { offset ->
                        // TODO extract it into an enum
                        val screenWidth = size.width
                        val oneThird = screenWidth / 3
                        val secondThird = oneThird * 2
                        if (offset.x < oneThird) {
                            onDoubleTapLeft()
                        } else if (offset.x >= oneThird && offset.x <= secondThird) {
                            onDoubleTapCenter()
                        } else {
                            onDoubleTapRight()
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
            .pointerInput(Unit) {
                var accumulatedHorizontalDrag = 0f
                var isHorizontalDragActive = false
                var lastPreviewDelta: Long? = null
                detectHorizontalDragGestures(
                    onDragStart = {
                        accumulatedHorizontalDrag = 0f
                        isHorizontalDragActive = false
                        lastPreviewDelta = null
                        onHorizontalDragPreview(null)
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        accumulatedHorizontalDrag += dragAmount
                        if (!isHorizontalDragActive && kotlin.math.abs(accumulatedHorizontalDrag) >= horizontalThresholdPx) {
                            isHorizontalDragActive = true
                        }
                        if (isHorizontalDragActive) {
                            change.consume()
                            val deltaMs = HorizontalSeekGestureHelper.deltaMs(accumulatedHorizontalDrag)
                            if (deltaMs != 0L && deltaMs != lastPreviewDelta) {
                                lastPreviewDelta = deltaMs
                                onHorizontalDragPreview(deltaMs)
                            }
                        }
                    },
                    onDragEnd = {
                        if (isHorizontalDragActive) {
                            val deltaMs = HorizontalSeekGestureHelper.deltaMs(accumulatedHorizontalDrag)
                            if (deltaMs != 0L) {
                                onHorizontalDrag(deltaMs)
                                onHorizontalDragPreview(deltaMs)
                            }
                        }
                        accumulatedHorizontalDrag = 0f
                        isHorizontalDragActive = false
                        lastPreviewDelta = null
                        onHorizontalDragPreview(null)
                    },
                    onDragCancel = {
                        accumulatedHorizontalDrag = 0f
                        isHorizontalDragActive = false
                        lastPreviewDelta = null
                        onHorizontalDragPreview(null)
                    }
                )
            }
    )
}
