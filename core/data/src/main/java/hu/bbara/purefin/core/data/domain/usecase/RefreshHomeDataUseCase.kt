package hu.bbara.purefin.core.data.domain.usecase

import hu.bbara.purefin.core.data.HomeRepository
import javax.inject.Inject

class RefreshHomeDataUseCase @Inject constructor(
    private val repository: HomeRepository
) {
    suspend operator fun invoke() {
        repository.refreshHomeData()
    }
}
