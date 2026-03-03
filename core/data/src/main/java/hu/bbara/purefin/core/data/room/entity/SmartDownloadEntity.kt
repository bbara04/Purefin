package hu.bbara.purefin.core.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "smart_downloads")
data class SmartDownloadEntity(
    @PrimaryKey val seriesId: UUID
)
