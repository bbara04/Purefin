package hu.bbara.purefin.domain.usecase

import hu.bbara.purefin.data.MediaRepository
import javax.inject.Inject

class RefreshHomeDataUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    suspend operator fun invoke() {
        repository.refreshHomeData()
    }
}
