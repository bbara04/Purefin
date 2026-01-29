package hu.bbara.purefin.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import hu.bbara.purefin.data.local.entity.EpisodeEntity
import java.util.UUID

@Dao
interface EpisodeDao {

    @Upsert
    suspend fun upsertEpisode(episode: EpisodeEntity)

    @Upsert
    suspend fun upsertEpisodes(episodes: List<EpisodeEntity>)

    @Query("DELETE FROM episodes WHERE id = :episodeId")
    suspend fun deleteEpisode(episodeId: UUID)

    @Query("DELETE FROM episodes WHERE seasonId = :seasonId")
    suspend fun deleteEpisodesForSeason(seasonId: UUID)

    @Query("DELETE FROM episodes WHERE seriesId = :seriesId")
    suspend fun deleteEpisodesForSeries(seriesId: UUID)

    @Query("SELECT * FROM episodes WHERE id = :episodeId")
    suspend fun getEpisodeById(episodeId: UUID): EpisodeEntity?

    @Query("SELECT * FROM episodes WHERE seasonId = :seasonId ORDER BY `index` ASC")
    suspend fun getEpisodesForSeason(seasonId: UUID): List<EpisodeEntity>

    @Query("SELECT * FROM episodes WHERE seriesId = :seriesId ORDER BY seasonId, `index` ASC")
    suspend fun getEpisodesForSeries(seriesId: UUID): List<EpisodeEntity>
}
