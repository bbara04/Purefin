package hu.bbara.purefin.data.local.db

import androidx.room.TypeConverter
import hu.bbara.purefin.data.local.entity.EpisodeCastMemberEntity
import java.util.UUID
import org.json.JSONArray
import org.json.JSONObject

class PurefinTypeConverters {

    @TypeConverter
    fun fromUuid(value: UUID?): String? = value?.toString()

    @TypeConverter
    fun toUuid(value: String?): UUID? = value?.let(UUID::fromString)

    @TypeConverter
    fun fromCastMembers(value: List<EpisodeCastMemberEntity>?): String? {
        if (value.isNullOrEmpty()) return null
        val jsonArray = JSONArray()
        value.forEach { member ->
            jsonArray.put(
                JSONObject().apply {
                    put("name", member.name)
                    put("role", member.role)
                    put("imageUrl", member.imageUrl)
                }
            )
        }
        return jsonArray.toString()
    }

    @TypeConverter
    fun toCastMembers(value: String?): List<EpisodeCastMemberEntity> {
        if (value.isNullOrBlank()) return emptyList()
        val jsonArray = JSONArray(value)
        val members = mutableListOf<EpisodeCastMemberEntity>()
        for (index in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.optJSONObject(index) ?: continue
            val imageUrl = when {
                jsonObject.has("imageUrl") && !jsonObject.isNull("imageUrl") -> jsonObject.optString("imageUrl")
                else -> null
            }
            members += EpisodeCastMemberEntity(
                name = jsonObject.optString("name"),
                role = jsonObject.optString("role"),
                imageUrl = imageUrl?.ifBlank { null }
            )
        }
        return members
    }
}
