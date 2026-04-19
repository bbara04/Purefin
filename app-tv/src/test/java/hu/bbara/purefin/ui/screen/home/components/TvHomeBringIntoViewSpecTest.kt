package hu.bbara.purefin.ui.screen.home.components

import org.junit.Assert.assertEquals
import org.junit.Test

class TvHomeBringIntoViewSpecTest {

    private val topOffsetPx = 56f
    private val spec = tvHomeColumnBringIntoViewSpec(topOffsetPx)

    @Test
    fun returnsZero_whenFocusedItemAlreadyAtTopOffset() {
        assertEquals(
            0f,
            spec.calculateScrollDistance(
                offset = topOffsetPx,
                size = 52f,
                containerSize = 240f
            ),
            0.0001f
        )
    }

    @Test
    fun scrollsDownToTopOffset_whenFocusedItemIsBelowTarget() {
        assertEquals(
            64f,
            spec.calculateScrollDistance(
                offset = 120f,
                size = 52f,
                containerSize = 240f
            ),
            0.0001f
        )
    }

    @Test
    fun scrollsUpToTopOffset_whenFocusedItemIsAboveTarget() {
        assertEquals(
            -32f,
            spec.calculateScrollDistance(
                offset = 24f,
                size = 52f,
                containerSize = 240f
            ),
            0.0001f
        )
    }
}
