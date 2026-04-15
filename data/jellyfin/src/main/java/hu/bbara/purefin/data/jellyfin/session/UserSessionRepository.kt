package hu.bbara.purefin.data.jellyfin.session

import androidx.datastore.core.DataStore
import hu.bbara.purefin.core.data.session.UserSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class DataStoreUserSessionRepository @Inject constructor(
    private val userSessionDataStore: DataStore<UserSession>
) : UserSessionRepository {
    val session: Flow<UserSession> = userSessionDataStore.data

    override val serverUrl: Flow<String> = session
        .map { it.url }

    override suspend fun setServerUrl(serverUrl: String) {
        userSessionDataStore.updateData {
            it.copy(url = serverUrl)
        }
    }

    override val accessToken: Flow<String> = session
        .map { it.accessToken }

    override suspend fun setAccessToken(accessToken: String) {
        userSessionDataStore.updateData {
            it.copy(accessToken = accessToken)
        }
    }

    override val userId: Flow<UUID?> = session
        .map { it.userId }

    override suspend fun setUserId(userId: UUID?) {
        userSessionDataStore.updateData {
            it.copy(userId = userId)
        }
    }

    override suspend fun getUserId(): UUID? = userId.first()

    override val isLoggedIn: Flow<Boolean> = session.map { it.loggedIn }.distinctUntilChanged()

    override suspend fun setLoggedIn(isLoggedIn: Boolean) {
        userSessionDataStore.updateData {
            it.copy(loggedIn = isLoggedIn)
        }
    }

    override val isOfflineMode: Flow<Boolean> = session.map { it.isOfflineMode }.distinctUntilChanged()
}
