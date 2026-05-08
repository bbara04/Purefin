package hu.bbara.purefin.core.feature.content.episode

import hu.bbara.purefin.core.data.LocalMediaRepository
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class SeriesIdLookUpUseCase @Inject constructor(
    private val localMediaRepository: LocalMediaRepository,
) {
    suspend operator fun invoke(episodeId: UUID): UUID? {
        return localMediaRepository.getEpisode(episodeId).first()?.seriesId
    }
}
