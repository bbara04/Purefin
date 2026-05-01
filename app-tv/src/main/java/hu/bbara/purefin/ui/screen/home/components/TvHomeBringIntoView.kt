@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package hu.bbara.purefin.ui.screen.home.components

import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.ui.unit.dp
import kotlin.math.abs

internal val TvHomeFocusedItemTopOffset = 48.dp
internal val TvHomeBringIntoViewTrailingSpace = 120.dp

private const val TvHomeRowPivotParentFraction = 0.3f
private const val TvHomeRowPivotChildFraction = 0f

internal fun tvHomeColumnBringIntoViewSpec(topOffsetPx: Float): BringIntoViewSpec {
    return object : BringIntoViewSpec {
        override fun calculateScrollDistance(
            offset: Float,
            size: Float,
            containerSize: Float,
        ): Float {
            val leadingEdge = offset
            val trailingEdge = offset + size
            val childSize = abs(trailingEdge - leadingEdge)
            val childSmallerThanParent = childSize <= containerSize
            val spaceAvailableToShowItem = containerSize - topOffsetPx
            val targetForLeadingEdge =
                if (childSmallerThanParent && spaceAvailableToShowItem < childSize) {
                    containerSize - childSize
                } else {
                    topOffsetPx
                }

            return leadingEdge - targetForLeadingEdge
        }
    }
}

internal val TvHomeRowBringIntoViewSpec: BringIntoViewSpec =
    object : BringIntoViewSpec {
        override fun calculateScrollDistance(
            offset: Float,
            size: Float,
            containerSize: Float,
        ): Float {
            val leadingEdge = offset
            val trailingEdge = offset + size
            val childSize = abs(trailingEdge - leadingEdge)
            val childSmallerThanParent = childSize <= containerSize
            val initialTargetForLeadingEdge =
                TvHomeRowPivotParentFraction * containerSize -
                    (TvHomeRowPivotChildFraction * childSize)
            val spaceAvailableToShowItem = containerSize - initialTargetForLeadingEdge
            val targetForLeadingEdge =
                if (childSmallerThanParent && spaceAvailableToShowItem < childSize) {
                    containerSize - childSize
                } else {
                    initialTargetForLeadingEdge
                }

            return leadingEdge - targetForLeadingEdge
        }
    }
