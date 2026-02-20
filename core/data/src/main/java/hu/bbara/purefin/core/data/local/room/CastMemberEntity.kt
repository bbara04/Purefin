package hu.bbara.purefin.core.data.local.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "cast_members",
    indices = [Index("movieId"), Index("seriesId"), Index("episodeId")]
)
data class CastMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val role: String,
    val imageUrl: String?,
    val movieId: UUID? = null,
    val seriesId: UUID? = null,
    val episodeId: UUID? = null
)
