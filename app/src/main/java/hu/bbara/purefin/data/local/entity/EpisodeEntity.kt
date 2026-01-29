package hu.bbara.purefin.data.local.entity

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
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SeasonEntity::class,
            parentColumns = ["id"],
            childColumns = ["seasonId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["seriesId"]), Index(value = ["seasonId"])]
)
data class EpisodeEntity(
    @PrimaryKey val id: UUID,
    val seriesId: UUID,
    val seasonId: UUID,
    val title: String,
    val index: Int,
    val releaseDate: String,
    val rating: String,
    val runtime: String,
    val format: String,
    val synopsis: String,
    val heroImageUrl: String,
    val cast: List<EpisodeCastMemberEntity>
)

data class EpisodeCastMemberEntity(
    val name: String,
    val role: String,
    val imageUrl: String?
)
