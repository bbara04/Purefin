package hu.bbara.purefin.settings

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import hu.bbara.purefin.model.Settings
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object SettingsSerializer : Serializer<Settings> {
    override val defaultValue: Settings
        get() = Settings()

    override suspend fun readFrom(input: InputStream): Settings {
        try {
            return Json.decodeFromString<Settings>(
                input.readBytes().decodeToString()
            )
        } catch (serialization: SerializationException) {
            throw CorruptionException("Unable to read Settings", serialization)
        }
    }

    override suspend fun writeTo(t: Settings, output: OutputStream) {
        output.write(
            Json.encodeToString(Settings.serializer(), t)
                .encodeToByteArray()
        )
    }
}
