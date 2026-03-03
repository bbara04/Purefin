package hu.bbara.purefin.core.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import hu.bbara.purefin.core.data.room.entity.SmartDownloadEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface SmartDownloadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SmartDownloadEntity)

    @Query("DELETE FROM smart_downloads WHERE seriesId = :seriesId")
    suspend fun delete(seriesId: UUID)

    @Query("SELECT * FROM smart_downloads")
    suspend fun getAll(): List<SmartDownloadEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM smart_downloads WHERE seriesId = :seriesId)")
    suspend fun exists(seriesId: UUID): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM smart_downloads WHERE seriesId = :seriesId)")
    fun observe(seriesId: UUID): Flow<Boolean>

    @Query("SELECT * FROM smart_downloads")
    fun observeAll(): Flow<List<SmartDownloadEntity>>
}
