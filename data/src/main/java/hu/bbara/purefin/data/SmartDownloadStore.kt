package hu.bbara.purefin.data

import java.util.UUID
import kotlinx.coroutines.flow.Flow

interface SmartDownloadStore {
    suspend fun enable(seriesId: UUID)
    suspend fun disable(seriesId: UUID)
    fun observe(seriesId: UUID): Flow<Boolean>
    suspend fun getEnabledSeriesIds(): List<UUID>
}
