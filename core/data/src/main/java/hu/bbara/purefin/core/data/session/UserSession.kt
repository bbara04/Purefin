package hu.bbara.purefin.core.data.session

import kotlinx.serialization.Serializable
import org.jellyfin.sdk.model.serializer.UUIDSerializer
import java.util.UUID

@Serializable
data class UserSession(
    val accessToken: String,
    val url: String,
    @Serializable(with = UUIDSerializer::class)
    val userId: UUID?,
    val loggedIn: Boolean,
    val isOfflineMode: Boolean = false
)
