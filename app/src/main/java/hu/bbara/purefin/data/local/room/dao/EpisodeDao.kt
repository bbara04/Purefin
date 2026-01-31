package hu.bbara.purefin.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import hu.bbara.purefin.data.local.room.EpisodeEntity
import java.util.UUID

@Dao
interface EpisodeDao {
    @Upsert
    suspend fun upsert(episode: EpisodeEntity)

    @Upsert
    suspend fun upsertAll(episodes: List<EpisodeEntity>)

    @Query("SELECT * FROM episodes WHERE seriesId = :seriesId")
    suspend fun getBySeriesId(seriesId: UUID): List<EpisodeEntity>

    @Query("SELECT * FROM episodes WHERE seasonId = :seasonId")
    suspend fun getBySeasonId(seasonId: UUID): List<EpisodeEntity>

    @Query("SELECT * FROM episodes WHERE id = :id")
    suspend fun getById(id: UUID): EpisodeEntity?

    @Query("DELETE FROM episodes WHERE seriesId = :seriesId")
    suspend fun deleteBySeriesId(seriesId: UUID)

    @Query("DELETE FROM episodes WHERE seasonId = :seasonId")
    suspend fun deleteBySeasonId(seasonId: UUID)
}
