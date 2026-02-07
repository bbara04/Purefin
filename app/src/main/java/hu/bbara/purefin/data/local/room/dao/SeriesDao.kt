package hu.bbara.purefin.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import hu.bbara.purefin.data.local.room.SeriesEntity
import java.util.UUID

@Dao
interface SeriesDao {
    @Upsert
    suspend fun upsert(series: SeriesEntity)

    @Upsert
    suspend fun upsertAll(series: List<SeriesEntity>)

    @Query("SELECT * FROM series")
    suspend fun getAll(): List<SeriesEntity>

    @Query("SELECT * FROM series WHERE id = :id")
    suspend fun getById(id: UUID): SeriesEntity?

    @Query("UPDATE series SET unwatchedEpisodeCount = :count WHERE id = :id")
    suspend fun updateUnwatchedCount(id: UUID, count: Int)

    @Query("DELETE FROM series")
    suspend fun clear()
}
