package hu.bbara.purefin.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import hu.bbara.purefin.data.local.room.CastMemberEntity
import java.util.UUID

@Dao
interface CastMemberDao {
    @Upsert
    suspend fun upsertAll(cast: List<CastMemberEntity>)

    @Query("SELECT * FROM cast_members WHERE movieId = :movieId")
    suspend fun getByMovieId(movieId: UUID): List<CastMemberEntity>

    @Query("SELECT * FROM cast_members WHERE seriesId = :seriesId")
    suspend fun getBySeriesId(seriesId: UUID): List<CastMemberEntity>

    @Query("SELECT * FROM cast_members WHERE episodeId = :episodeId")
    suspend fun getByEpisodeId(episodeId: UUID): List<CastMemberEntity>

    @Query("DELETE FROM cast_members WHERE movieId = :movieId")
    suspend fun deleteByMovieId(movieId: UUID)

    @Query("DELETE FROM cast_members WHERE seriesId = :seriesId")
    suspend fun deleteBySeriesId(seriesId: UUID)

    @Query("DELETE FROM cast_members WHERE episodeId = :episodeId")
    suspend fun deleteByEpisodeId(episodeId: UUID)
}
