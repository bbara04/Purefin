package hu.bbara.purefin.data.session

import java.util.UUID
import kotlinx.coroutines.flow.Flow

interface UserSessionRepository {
    val serverUrl: Flow<String>
    suspend fun setServerUrl(serverUrl: String)

    val accessToken: Flow<String>
    suspend fun setAccessToken(accessToken: String)

    val userId: Flow<UUID?>
    suspend fun setUserId(userId: UUID?)
    suspend fun getUserId(): UUID?

    val isLoggedIn: Flow<Boolean>
    suspend fun setLoggedIn(isLoggedIn: Boolean)

    val isOfflineMode: Flow<Boolean>
}
