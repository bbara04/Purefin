package hu.bbara.purefin.domain.usecase

import hu.bbara.purefin.data.MediaRepository
import java.util.UUID
import javax.inject.Inject

class UpdateWatchProgressUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    suspend operator fun invoke(mediaId: UUID, positionMs: Long, durationMs: Long) {
        repository.updateWatchProgress(mediaId, positionMs, durationMs)
    }
}
