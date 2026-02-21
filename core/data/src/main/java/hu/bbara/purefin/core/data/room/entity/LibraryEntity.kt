package hu.bbara.purefin.core.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "library")
data class LibraryEntity (
    @PrimaryKey
    val id: UUID,
    val name: String,
    val type: String,
)