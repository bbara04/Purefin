package hu.bbara.purefin.player.model

data class PlaybackProgressSnapshot(
    val durationMs: Long = 0L,
    val positionMs: Long = 0L,
    val bufferedMs: Long = 0L,
    val isLive: Boolean = false
)