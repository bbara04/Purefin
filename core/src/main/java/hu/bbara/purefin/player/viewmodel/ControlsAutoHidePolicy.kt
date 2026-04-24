package hu.bbara.purefin.player.viewmodel

enum class ControlsAutoHideBlocker {
    PLAYLIST,
    TRACK_PANEL
}

internal sealed interface ControlsAutoHideCommand {
    data object Cancel : ControlsAutoHideCommand
    data class Schedule(val delayMs: Long) : ControlsAutoHideCommand
}

internal class ControlsAutoHidePolicy(
    private val defaultDelayMs: Long
) {
    private val blockers = mutableSetOf<ControlsAutoHideBlocker>()
    private var isPlaying = false

    var controlsVisible: Boolean = true
        private set

    var lastAutoHideDelayMs: Long = defaultDelayMs
        private set

    fun onPlaybackChanged(isPlaying: Boolean): ControlsAutoHideCommand {
        this.isPlaying = isPlaying
        return nextCommand()
    }

    fun setAutoHideDelay(delayMs: Long): ControlsAutoHideCommand {
        lastAutoHideDelayMs = delayMs
        return nextCommand()
    }

    fun showControls(delayMs: Long? = null): ControlsAutoHideCommand {
        delayMs?.let { lastAutoHideDelayMs = it }
        controlsVisible = true
        return nextCommand()
    }

    fun toggleControlsVisibility(): ControlsAutoHideCommand {
        controlsVisible = !controlsVisible
        return nextCommand()
    }

    fun hideControls(): ControlsAutoHideCommand {
        controlsVisible = false
        return ControlsAutoHideCommand.Cancel
    }

    fun setBlocked(
        blocker: ControlsAutoHideBlocker,
        blocked: Boolean
    ): ControlsAutoHideCommand {
        if (blocked) {
            blockers += blocker
            controlsVisible = true
        } else {
            blockers -= blocker
        }
        return nextCommand()
    }

    private fun nextCommand(): ControlsAutoHideCommand {
        return if (!controlsVisible || !isPlaying || blockers.isNotEmpty()) {
            ControlsAutoHideCommand.Cancel
        } else {
            ControlsAutoHideCommand.Schedule(lastAutoHideDelayMs)
        }
    }
}
