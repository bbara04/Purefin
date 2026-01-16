package hu.bbara.purefin.session

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserSessionRepository @Inject constructor(
    private val userSessionDataStore: DataStore<UserSession>
) {
    val session: Flow<UserSession> = userSessionDataStore.data

    val serverUrl: Flow<String> = session
        .map { it.url }
        .distinctUntilChanged()

    suspend fun getUrl(): String = serverUrl.first()

    val accessToken: Flow<String> = session
        .map { it.accessToken }
        .distinctUntilChanged()

    suspend fun updateAccessToken(accessToken: String) {
        userSessionDataStore.updateData {
            it.copy(accessToken = accessToken)
        }
    }

    val isLoggedIn: Flow<Boolean> = session.map { it.loggedIn }.distinctUntilChanged()

    suspend fun setLoggedIn(isLoggedIn: Boolean) {
        userSessionDataStore.updateData {
            it.copy(loggedIn = isLoggedIn)
        }
    }

    suspend fun updateServerUrl(serverUrl: String) {
        userSessionDataStore.updateData {
            it.copy(url = serverUrl)
        }
    }
}
