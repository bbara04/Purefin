package hu.bbara.purefin.core.data.cache

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object HomeCacheSerializer : Serializer<HomeCache> {
    override val defaultValue: HomeCache
        get() = HomeCache()

    override suspend fun readFrom(input: InputStream): HomeCache {
        try {
            return Json.decodeFromString<HomeCache>(
                input.readBytes().decodeToString()
            )
        } catch (serialization: SerializationException) {
            throw CorruptionException("proto", serialization)
        }
    }

    override suspend fun writeTo(t: HomeCache, output: OutputStream) {
        output.write(
            Json.encodeToString(t)
                .encodeToByteArray()
        )
    }
}
