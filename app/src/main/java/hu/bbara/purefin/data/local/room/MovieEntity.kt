package hu.bbara.purefin.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "movies")
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
