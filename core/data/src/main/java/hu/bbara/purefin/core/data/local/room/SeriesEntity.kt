package hu.bbara.purefin.core.data.local.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "series",
    foreignKeys = [
        ForeignKey(
            entity = LibraryEntity::class,
            parentColumns = ["id"],
            childColumns = ["libraryId"]
        ),
    ],
    indices = [Index("libraryId")]
)
data class SeriesEntity(
    @PrimaryKey val id: UUID,
    val libraryId: UUID,
    val name: String,
    val synopsis: String,
    val year: String,
    val heroImageUrl: String,
    val unwatchedEpisodeCount: Int,
    val seasonCount: Int
)
