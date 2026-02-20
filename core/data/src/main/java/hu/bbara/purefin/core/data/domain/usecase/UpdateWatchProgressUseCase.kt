package hu.bbara.purefin.core.data.domain.usecase

import hu.bbara.purefin.core.data.MediaRepository
import java.util.UUID
import javax.inject.Inject

class UpdateWatchProgressUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    suspend operator fun invoke(mediaId: UUID, positionMs: Long, durationMs: Long) {
        repository.updateWatchProgress(mediaId, positionMs, durationMs)
    }
}
