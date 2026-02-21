package hu.bbara.purefin.core.data.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "episodes",
    foreignKeys = [
        ForeignKey(
            entity = SeriesEntity::class,
            parentColumns = ["id"],
            childColumns = ["seriesId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("seriesId"), Index("seasonId")]
)
data class EpisodeEntity(
    @PrimaryKey val id: UUID,
    val seriesId: UUID,
    val seasonId: UUID,
    val index: Int,
    val title: String,
    val synopsis: String,
    val releaseDate: String,
    val rating: String,
    val runtime: String,
    val progress: Double?,
    val watched: Boolean,
    val format: String,
    val heroImageUrl: String
)
