package hu.bbara.purefin.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import hu.bbara.purefin.data.local.entity.SeasonEntity
import hu.bbara.purefin.data.local.relations.SeasonWithEpisodes
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@Dao
interface SeasonDao {

    @Upsert
    suspend fun upsertSeason(season: SeasonEntity)

    @Upsert
    suspend fun upsertSeasons(seasons: List<SeasonEntity>)

    @Query("DELETE FROM seasons WHERE id = :seasonId")
    suspend fun deleteSeason(seasonId: UUID)

    @Query("DELETE FROM seasons WHERE seriesId = :seriesId")
    suspend fun deleteSeasonsForSeries(seriesId: UUID)

    @Transaction
    @Query("SELECT * FROM seasons WHERE id = :seasonId")
    suspend fun getSeasonWithEpisodes(seasonId: UUID): SeasonWithEpisodes?

    @Query("SELECT * FROM seasons WHERE id = :seasonId")
    suspend fun getSeasonById(seasonId: UUID): SeasonEntity?

    @Query("SELECT * FROM seasons WHERE seriesId = :seriesId ORDER BY `index` ASC")
    suspend fun getSeasonsForSeries(seriesId: UUID): List<SeasonEntity>

    @Transaction
    @Query("SELECT * FROM seasons WHERE seriesId = :seriesId ORDER BY `index` ASC")
    fun observeSeasonsWithEpisodes(seriesId: UUID): Flow<List<SeasonWithEpisodes>>
}
