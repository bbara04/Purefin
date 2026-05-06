package hu.bbara.purefin.core.player.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ControlsAutoHidePolicyTest {

    @Test
    fun activeBlockerCancelsScheduledAutoHide() {
        val policy = ControlsAutoHidePolicy(defaultDelayMs = 3_500L)

        assertEquals(
            ControlsAutoHideCommand.Schedule(3_500L),
            policy.onPlaybackChanged(isPlaying = true)
        )
        assertEquals(
            ControlsAutoHideCommand.Cancel,
            policy.setBlocked(ControlsAutoHideBlocker.TRACK_PANEL, blocked = true)
        )
        assertTrue(policy.controlsVisible)
    }

    @Test
    fun playlistBlockerPreventsAutoHideUntilCleared() {
        val policy = ControlsAutoHidePolicy(defaultDelayMs = 3_500L)

        policy.onPlaybackChanged(isPlaying = true)
        policy.showControls(delayMs = 5_000L)

        assertEquals(
            ControlsAutoHideCommand.Cancel,
            policy.setBlocked(ControlsAutoHideBlocker.PLAYLIST, blocked = true)
        )
        assertEquals(
            ControlsAutoHideCommand.Schedule(5_000L),
            policy.setBlocked(ControlsAutoHideBlocker.PLAYLIST, blocked = false)
        )
    }

    @Test
    fun trackPanelBlockerUsesRememberedDelayWhenRemoved() {
        val policy = ControlsAutoHidePolicy(defaultDelayMs = 3_500L)

        policy.onPlaybackChanged(isPlaying = true)
        assertEquals(
            ControlsAutoHideCommand.Cancel,
            policy.setBlocked(ControlsAutoHideBlocker.TRACK_PANEL, blocked = true)
        )
        policy.showControls(delayMs = 5_000L)

        assertEquals(
            ControlsAutoHideCommand.Schedule(5_000L),
            policy.setBlocked(ControlsAutoHideBlocker.TRACK_PANEL, blocked = false)
        )
    }

    @Test
    fun autoHideResumesOnlyAfterLastBlockerIsCleared() {
        val policy = ControlsAutoHidePolicy(defaultDelayMs = 3_500L)

        policy.onPlaybackChanged(isPlaying = true)
        policy.showControls(delayMs = 5_000L)
        policy.setBlocked(ControlsAutoHideBlocker.PLAYLIST, blocked = true)
        policy.setBlocked(ControlsAutoHideBlocker.TRACK_PANEL, blocked = true)

        assertEquals(
            ControlsAutoHideCommand.Cancel,
            policy.setBlocked(ControlsAutoHideBlocker.PLAYLIST, blocked = false)
        )
        assertEquals(
            ControlsAutoHideCommand.Schedule(5_000L),
            policy.setBlocked(ControlsAutoHideBlocker.TRACK_PANEL, blocked = false)
        )
    }

    @Test
    fun blockingStateMakesHiddenControlsVisibleAgain() {
        val policy = ControlsAutoHidePolicy(defaultDelayMs = 3_500L)

        policy.onPlaybackChanged(isPlaying = true)
        policy.toggleControlsVisibility()

        assertEquals(
            ControlsAutoHideCommand.Cancel,
            policy.setBlocked(ControlsAutoHideBlocker.PLAYLIST, blocked = true)
        )
        assertTrue(policy.controlsVisible)
    }
}
