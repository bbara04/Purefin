package hu.bbara.purefin.core.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import hu.bbara.purefin.core.data.local.room.SeasonEntity
import java.util.UUID

@Dao
interface SeasonDao {
    @Upsert
    suspend fun upsert(season: SeasonEntity)

    @Upsert
    suspend fun upsertAll(seasons: List<SeasonEntity>)

    @Query("SELECT * FROM seasons WHERE seriesId = :seriesId")
    suspend fun getBySeriesId(seriesId: UUID): List<SeasonEntity>

    @Query("SELECT * FROM seasons WHERE id = :id")
    suspend fun getById(id: UUID): SeasonEntity?

    @Query("UPDATE seasons SET unwatchedEpisodeCount = :count WHERE id = :id")
    suspend fun updateUnwatchedCount(id: UUID, count: Int)

    @Query("DELETE FROM seasons WHERE seriesId = :seriesId")
    suspend fun deleteBySeriesId(seriesId: UUID)
}
