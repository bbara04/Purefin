package hu.bbara.purefin.core.player.manager

import org.junit.Assert.assertEquals
import org.junit.Test

class PendingSeekTrackerTest {

    @Test
    fun repeatedForwardSeeksAccumulateWhilePlayerPositionIsStale() {
        val tracker = PendingSeekTracker()

        val firstBase = tracker.currentPosition(playerPositionMs = 30_000L)
        tracker.recordSeek(basePositionMs = firstBase, targetPositionMs = 40_000L)

        val secondBase = tracker.currentPosition(playerPositionMs = 30_000L)
        tracker.recordSeek(basePositionMs = secondBase, targetPositionMs = secondBase + 10_000L)

        assertEquals(50_000L, tracker.currentPosition(playerPositionMs = 30_000L))
    }

    @Test
    fun repeatedBackwardSeeksAccumulateWhilePlayerPositionIsStale() {
        val tracker = PendingSeekTracker()

        val firstBase = tracker.currentPosition(playerPositionMs = 30_000L)
        tracker.recordSeek(basePositionMs = firstBase, targetPositionMs = 20_000L)

        val secondBase = tracker.currentPosition(playerPositionMs = 30_000L)
        tracker.recordSeek(basePositionMs = secondBase, targetPositionMs = secondBase - 10_000L)

        assertEquals(10_000L, tracker.currentPosition(playerPositionMs = 30_000L))
    }

    @Test
    fun pendingForwardSeekClearsOncePlayerCatchesUp() {
        val tracker = PendingSeekTracker()

        tracker.recordSeek(basePositionMs = 30_000L, targetPositionMs = 40_000L)

        assertEquals(40_000L, tracker.currentPosition(playerPositionMs = 30_000L))
        assertEquals(39_700L, tracker.currentPosition(playerPositionMs = 39_700L))
        assertEquals(41_000L, tracker.currentPosition(playerPositionMs = 41_000L))
    }

    @Test
    fun pendingBackwardSeekClearsOncePlayerCatchesUp() {
        val tracker = PendingSeekTracker()

        tracker.recordSeek(basePositionMs = 30_000L, targetPositionMs = 20_000L)

        assertEquals(20_000L, tracker.currentPosition(playerPositionMs = 30_000L))
        assertEquals(20_300L, tracker.currentPosition(playerPositionMs = 20_300L))
        assertEquals(19_000L, tracker.currentPosition(playerPositionMs = 19_000L))
    }
}
