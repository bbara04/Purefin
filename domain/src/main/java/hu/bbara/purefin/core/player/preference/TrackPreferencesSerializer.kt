package hu.bbara.purefin.core.player.preference

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object TrackPreferencesSerializer : Serializer<TrackPreferences> {
    override val defaultValue: TrackPreferences
        get() = TrackPreferences()

    override suspend fun readFrom(input: InputStream): TrackPreferences {
        try {
            return Json.decodeFromString<TrackPreferences>(
                input.readBytes().decodeToString()
            )
        } catch (serialization: SerializationException) {
            throw CorruptionException("Unable to read TrackPreferences", serialization)
        }
    }

    override suspend fun writeTo(t: TrackPreferences, output: OutputStream) {
        output.write(
            Json.encodeToString(TrackPreferences.serializer(), t)
                .encodeToByteArray()
        )
    }
}
