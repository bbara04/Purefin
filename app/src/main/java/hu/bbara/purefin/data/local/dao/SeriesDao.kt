package hu.bbara.purefin.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import hu.bbara.purefin.data.local.entity.SeriesEntity
import hu.bbara.purefin.data.local.relations.SeriesWithSeasonsAndEpisodes
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@Dao
interface SeriesDao {

    @Upsert
    suspend fun upsertSeries(series: SeriesEntity)

    @Upsert
    suspend fun upsertSeries(series: List<SeriesEntity>)

    @Query("DELETE FROM series WHERE id = :seriesId")
    suspend fun deleteSeries(seriesId: UUID)

    @Transaction
    @Query("SELECT * FROM series WHERE id = :seriesId")
    suspend fun getSeriesWithContent(seriesId: UUID): SeriesWithSeasonsAndEpisodes?

    @Transaction
    @Query("SELECT * FROM series WHERE id = :seriesId")
    fun observeSeriesWithContent(seriesId: UUID): Flow<SeriesWithSeasonsAndEpisodes?>

    @Transaction
    @Query("SELECT * FROM series")
    fun observeAllSeriesWithContent(): Flow<List<SeriesWithSeasonsAndEpisodes>>

    @Query("SELECT * FROM series WHERE id = :seriesId")
    suspend fun getSeriesById(seriesId: UUID): SeriesEntity?

    @Query("SELECT * FROM series")
    fun observeSeriesEntities(): Flow<List<SeriesEntity>>
}
