package hu.bbara.purefin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "series")
data class SeriesEntity(
    @PrimaryKey val id: UUID,
    val name: String,
    val synopsis: String,
    val year: String,
    val heroImageUrl: String,
    val seasonCount: Int
)
