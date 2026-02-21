package hu.bbara.purefin.core.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import hu.bbara.purefin.core.data.room.entity.EpisodeEntity
import kotlinx.coroutines.flow.Flow
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

    @Query("SELECT * FROM episodes")
    fun observeAll(): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes WHERE id = :id")
    suspend fun getById(id: UUID): EpisodeEntity?

    @Query("UPDATE episodes SET progress = :progress, watched = :watched WHERE id = :id")
    suspend fun updateProgress(id: UUID, progress: Double?, watched: Boolean)

    @Query("SELECT COUNT(*) FROM episodes WHERE seriesId = :seriesId AND watched = 0")
    suspend fun countUnwatchedBySeries(seriesId: UUID): Int

    @Query("SELECT COUNT(*) FROM episodes WHERE seasonId = :seasonId AND watched = 0")
    suspend fun countUnwatchedBySeason(seasonId: UUID): Int

    @Query("DELETE FROM episodes WHERE seriesId = :seriesId")
    suspend fun deleteBySeriesId(seriesId: UUID)

    @Query("DELETE FROM episodes WHERE seasonId = :seasonId")
    suspend fun deleteBySeasonId(seasonId: UUID)
}
