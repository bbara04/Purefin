package hu.bbara.purefin.core.data

import hu.bbara.purefin.core.data.room.dao.SmartDownloadDao
import hu.bbara.purefin.core.data.room.entity.SmartDownloadEntity
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class RoomSmartDownloadStore @Inject constructor(
    private val smartDownloadDao: SmartDownloadDao,
) : SmartDownloadStore {
    override suspend fun enable(seriesId: UUID) {
        smartDownloadDao.insert(SmartDownloadEntity(seriesId))
    }

    override suspend fun disable(seriesId: UUID) {
        smartDownloadDao.delete(seriesId)
    }

    override fun observe(seriesId: UUID): Flow<Boolean> {
        return smartDownloadDao.observe(seriesId)
    }

    override suspend fun getEnabledSeriesIds(): List<UUID> {
        return smartDownloadDao.getAll().map { it.seriesId }
    }
}
