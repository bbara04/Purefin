package hu.bbara.purefin.core.data.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "series",
    indices = [Index("libraryId")]
)
data class SeriesEntity(
    @PrimaryKey val id: UUID,
    val libraryId: UUID,
    val name: String,
    val synopsis: String,
    val year: String,
    val imageUrlPrefix: String,
    val unwatchedEpisodeCount: Int,
    val seasonCount: Int
)
