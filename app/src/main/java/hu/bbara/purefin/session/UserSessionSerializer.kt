package hu.bbara.purefin.session

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object UserSessionSerializer : Serializer<UserSession> {
    override val defaultValue: UserSession
        get() = UserSession(accessToken = "", url = "", loggedIn = false, userId = null, isOfflineMode = false)

    override suspend fun readFrom(input: InputStream): UserSession {
        try {
            return Json.decodeFromString<UserSession>(
                input.readBytes().decodeToString()
            )
        } catch (serialization: SerializationException) {
            throw CorruptionException("proto", serialization)
        }
    }

    override suspend fun writeTo(t: UserSession, output: OutputStream) {
        output.write(
            Json.encodeToString(t)
                .encodeToByteArray()
        )
    }


}