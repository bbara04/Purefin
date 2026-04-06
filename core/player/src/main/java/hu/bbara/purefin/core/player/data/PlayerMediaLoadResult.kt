package hu.bbara.purefin.core.player.data

import androidx.media3.common.MediaItem
import hu.bbara.purefin.core.player.model.PlayerError

sealed interface PlayerMediaLoadResult {
    data class Success(
        val mediaItem: MediaItem,
        val resumePositionMs: Long?
    ) : PlayerMediaLoadResult

    data class Failure(
        val error: PlayerError
    ) : PlayerMediaLoadResult
}
