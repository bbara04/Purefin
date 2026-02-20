package hu.bbara.purefin.core.data.local.room

import androidx.room.TypeConverter
import java.util.UUID

/**
 * Stores UUIDs as strings for Room in-memory database.
 */
class UuidConverters {
    @TypeConverter
    fun fromString(value: String?): UUID? = value?.let(UUID::fromString)

    @TypeConverter
    fun uuidToString(uuid: UUID?): String? = uuid?.toString()
}
