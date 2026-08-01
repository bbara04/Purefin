package hu.bbara.purefin.data.offline.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import hu.bbara.purefin.data.offline.room.entity.SubtitleEntity
import java.util.UUID

@Dao
interface SubtitleDao {
    @Upsert
    suspend fun upsertAll(subtitles: List<SubtitleEntity>)

    @Query("SELECT * FROM subtitles WHERE mediaId = :mediaId ORDER BY `index` ASC")
    suspend fun getByMediaId(mediaId: UUID): List<SubtitleEntity>

    @Query("DELETE FROM subtitles WHERE mediaId = :mediaId")
    suspend fun deleteByMediaId(mediaId: UUID)
}