package hu.bbara.purefin.core.data.session

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class UserSessionRepository @Inject constructor(
    private val userSessionDataStore: DataStore<UserSession>
) {
    val session: Flow<UserSession> = userSessionDataStore.data

    val serverUrl: Flow<String> = session
        .map { it.url }

    suspend fun setServerUrl(serverUrl: String) {
        userSessionDataStore.updateData {
            it.copy(url = serverUrl)
        }
    }

    val accessToken: Flow<String> = session
        .map { it.accessToken }

    suspend fun setAccessToken(accessToken: String) {
        userSessionDataStore.updateData {
            it.copy(accessToken = accessToken)
        }
    }

    val userId: Flow<UUID?> = session
        .map { it.userId }

    suspend fun setUserId(userId: UUID?) {
        userSessionDataStore.updateData {
            it.copy(userId = userId)
        }
    }

    suspend fun getUserId(): UUID? = userId.first()

    val isLoggedIn: Flow<Boolean> = session.map { it.loggedIn }.distinctUntilChanged()

    suspend fun setLoggedIn(isLoggedIn: Boolean) {
        userSessionDataStore.updateData {
            it.copy(loggedIn = isLoggedIn)
        }
    }

    val isOfflineMode: Flow<Boolean> = session.map { it.isOfflineMode }.distinctUntilChanged()

    suspend fun setOfflineMode(isOffline: Boolean) {
        userSessionDataStore.updateData {
            it.copy(isOfflineMode = isOffline)
        }
    }
}
