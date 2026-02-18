package hu.bbara.purefin.data.local.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "movies",
    foreignKeys = [
        ForeignKey(
            entity = LibraryEntity::class,
            parentColumns = ["id"],
            childColumns = ["libraryId"],
        ),
    ],
    indices = [Index("libraryId")]
)
data class MovieEntity(
    @PrimaryKey val id: UUID,
    val libraryId: UUID,
    val title: String,
    val progress: Double?,
    val watched: Boolean,
    val year: String,
    val rating: String,
    val runtime: String,
    val format: String,
    val synopsis: String,
    val heroImageUrl: String,
    val audioTrack: String,
    val subtitles: String
)
