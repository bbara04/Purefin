package hu.bbara.purefin.core.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface SmartDownloadStore {
    suspend fun enable(seriesId: UUID, count: Int)
    suspend fun disable(seriesId: UUID)
    fun observe(seriesId: UUID): Flow<Boolean>
    suspend fun getEnabledSeriesIds(): List<UUID>
    suspend fun getCount(seriesId: UUID): Int?

    companion object {
        const val DEFAULT_SMART_DOWNLOAD_COUNT = 5
    }
}