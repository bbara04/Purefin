package hu.bbara.purefin.core.data.domain.usecase

import hu.bbara.purefin.core.data.AppContentRepository
import javax.inject.Inject

class RefreshHomeDataUseCase @Inject constructor(
    private val repository: AppContentRepository
) {
    suspend operator fun invoke() {
        repository.refreshHomeData()
    }
}
