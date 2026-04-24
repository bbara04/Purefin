package hu.bbara.purefin.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface SmartDownloadStore {
    suspend fun enable(seriesId: UUID)
    suspend fun disable(seriesId: UUID)
    fun observe(seriesId: UUID): Flow<Boolean>
    suspend fun getEnabledSeriesIds(): List<UUID>
}
