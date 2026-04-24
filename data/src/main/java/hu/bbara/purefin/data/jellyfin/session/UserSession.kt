package hu.bbara.purefin.data.jellyfin.session

import kotlinx.serialization.Serializable
import hu.bbara.purefin.navigation.UuidSerializer
import java.util.UUID

@Serializable
data class UserSession(
    val accessToken: String,
    val url: String,
    @Serializable(with = UuidSerializer::class)
    val userId: UUID?,
    val loggedIn: Boolean,
    val isOfflineMode: Boolean = false
)
