package hu.bbara.purefin.common.ui.components

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TvMediaDetailBringIntoViewSpecTest {

    @Test
    fun returnsZero_whenFocusedItemIsAlreadyFullyVisible() {
        assertEquals(
            0f,
            TvMediaDetailBringIntoViewSpec.calculateScrollDistance(
                offset = 320f,
                size = 52f,
                containerSize = 1080f
            ),
            0.0001f
        )
    }

    @Test
    fun scrollsOnlyEnoughToRevealFocusedItemBelowViewport() {
        assertEquals(
            36f,
            TvMediaDetailBringIntoViewSpec.calculateScrollDistance(
                offset = 1064f,
                size = 52f,
                containerSize = 1080f
            ),
            0.0001f
        )
    }

    @Test
    fun scrollsBackJustToTopEdge_whenFocusedItemMovesAboveViewport() {
        assertEquals(
            -24f,
            TvMediaDetailBringIntoViewSpec.calculateScrollDistance(
                offset = -24f,
                size = 52f,
                containerSize = 1080f
            ),
            0.0001f
        )
    }
}
