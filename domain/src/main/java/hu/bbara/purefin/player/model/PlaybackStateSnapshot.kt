package hu.bbara.purefin.player.model

data class PlaybackStateSnapshot(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val isEnded: Boolean = false,
    val error: String? = null
)

