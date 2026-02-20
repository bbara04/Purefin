package hu.bbara.purefin.core.data.domain.usecase

import hu.bbara.purefin.core.data.MediaRepository
import javax.inject.Inject

class RefreshHomeDataUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    suspend operator fun invoke() {
        repository.refreshHomeData()
    }
}
