package hu.bbara.purefin.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import hu.bbara.purefin.data.local.room.SeriesEntity
import hu.bbara.purefin.data.local.room.SeriesWithSeasonsAndEpisodes
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface SeriesDao {
    @Upsert
    suspend fun upsert(series: SeriesEntity)

    @Upsert
    suspend fun upsertAll(series: List<SeriesEntity>)

    @Query("SELECT * FROM series")
    suspend fun getAll(): List<SeriesEntity>

    @Query("SELECT * FROM series")
    fun observeAll(): Flow<List<SeriesEntity>>

    @Transaction
    @Query("SELECT * FROM series WHERE id = :id")
    fun observeWithContent(id: UUID): Flow<SeriesWithSeasonsAndEpisodes?>

    @Query("SELECT * FROM series WHERE id = :id")
    suspend fun getById(id: UUID): SeriesEntity?

    @Query("UPDATE series SET unwatchedEpisodeCount = :count WHERE id = :id")
    suspend fun updateUnwatchedCount(id: UUID, count: Int)

    @Query("DELETE FROM series")
    suspend fun clear()
}
