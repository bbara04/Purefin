package hu.bbara.purefin.ui.screen.player.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.player.helper.HorizontalSeekGestureHelper
import kotlin.math.abs

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
    onHorizontalDragSeekTo: (positionMs: Long) -> Unit,
    currentPositionProvider: () -> Long,
) {
    val density = LocalDensity.current
    val horizontalThresholdPx = with(density) { HorizontalSeekGestureHelper.START_THRESHOLD.toPx() }
    val directionThresholdPx = with(density) { 20.dp.toPx() }
    val dragActive = remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth(0.90f)
            .fillMaxHeight(0.90f)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { if (!dragActive.value) onTap() },
                    onDoubleTap = { offset ->
                        if (dragActive.value) return@detectTapGestures
                        val screenWidth = size.width
                        val oneThird = screenWidth / 3
                        val secondThird = oneThird * 2
                        when {
                            offset.x < oneThird -> onDoubleTapLeft()
                            offset.x <= secondThird -> onDoubleTapCenter()
                            else -> onDoubleTapRight()
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val startX = down.position.x
                    dragActive.value = false

                    var accumulatedDrag = Offset.Zero
                    var dragDirection: DragDirection? = null
                    var accumulatedHorizontalDrag = 0f
                    var isHorizontalDragActive = false
                    var lastPreviewDelta: Long? = null
                    var startPositionMs = 0L

                    drag(down.id) { change ->
                        val delta = change.positionChange()
                        accumulatedDrag += delta

                        if (dragDirection == null && (abs(accumulatedDrag.x) > directionThresholdPx || abs(accumulatedDrag.y) > directionThresholdPx)) {
                            dragActive.value = true
                            dragDirection = if (abs(accumulatedDrag.x) > abs(accumulatedDrag.y)) {
                                DragDirection.HORIZONTAL
                            } else {
                                DragDirection.VERTICAL
                            }
                        }

                        when (dragDirection) {
                            DragDirection.HORIZONTAL -> {
                                accumulatedHorizontalDrag += delta.x
                                if (!isHorizontalDragActive && abs(accumulatedHorizontalDrag) >= horizontalThresholdPx) {
                                    isHorizontalDragActive = true
                                    startPositionMs = currentPositionProvider()
                                }
                                if (isHorizontalDragActive) {
                                    change.consume()
                                    val deltaMs = HorizontalSeekGestureHelper.deltaMs(accumulatedHorizontalDrag)
                                    if (deltaMs != 0L && deltaMs != lastPreviewDelta) {
                                        lastPreviewDelta = deltaMs
                                        onHorizontalDragPreview(deltaMs)
                                    }
                                }
                            }
                            DragDirection.VERTICAL -> {
                                change.consume()
                                val isLeftSide = startX < size.width / 2
                                if (isLeftSide) {
                                    onVerticalDragLeft(delta.y)
                                } else {
                                    onVerticalDragRight(delta.y)
                                }
                            }
                            null -> {}
                        }
                    }

                    if (dragDirection == DragDirection.HORIZONTAL && isHorizontalDragActive) {
                        val deltaMs = HorizontalSeekGestureHelper.deltaMs(accumulatedHorizontalDrag)
                        if (deltaMs != 0L) {
                            val targetMs = (startPositionMs + deltaMs).coerceAtLeast(0L)
                            onHorizontalDragSeekTo(targetMs)
                            onHorizontalDragPreview(deltaMs)
                        }
                    }
                    onHorizontalDragPreview(null)
                }
            }
    )
}

private enum class DragDirection {
    HORIZONTAL,
    VERTICAL
}
