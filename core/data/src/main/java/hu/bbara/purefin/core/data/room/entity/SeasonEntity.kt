package hu.bbara.purefin.core.data.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "seasons",
    foreignKeys = [
        ForeignKey(
            entity = SeriesEntity::class,
            parentColumns = ["id"],
            childColumns = ["seriesId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("seriesId")]
)
data class SeasonEntity(
    @PrimaryKey val id: UUID,
    val seriesId: UUID,
    val name: String,
    val index: Int,
    val unwatchedEpisodeCount: Int,
    val episodeCount: Int
)
