package hu.bbara.purefin.data.jellyfin.session

import android.util.Log
import hu.bbara.purefin.core.data.AuthenticationRepository
import hu.bbara.purefin.core.data.JellyfinServerCandidate
import hu.bbara.purefin.core.data.QuickConnectSession
import hu.bbara.purefin.core.data.UserSessionRepository
import hu.bbara.purefin.data.jellyfin.client.JellyfinApiClient
import kotlinx.coroutines.flow.Flow
import org.jellyfin.sdk.model.api.AuthenticationResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JellyfinAuthenticationRepository @Inject constructor(
    private val jellyfinApiClient: JellyfinApiClient,
    private val userSessionRepository: UserSessionRepository,
) : AuthenticationRepository {
    override fun discoverServers(): Flow<JellyfinServerCandidate> =
        jellyfinApiClient.discoverServers()

    override suspend fun findServer(input: String): JellyfinServerCandidate? =
        runCatching { jellyfinApiClient.findServer(input) }
            .onFailure { Log.e(TAG, "Server search failed", it) }
            .getOrNull()

    override suspend fun isQuickConnectEnabled(url: String): Boolean =
        runCatching { jellyfinApiClient.isQuickConnectEnabled(url) }
            .onFailure { Log.e(TAG, "Quick Connect check failed", it) }
            .getOrDefault(false)

    override suspend fun initiateQuickConnect(url: String): QuickConnectSession? =
        runCatching { jellyfinApiClient.initiateQuickConnect(url) }
            .onFailure { Log.e(TAG, "Quick Connect initiation failed", it) }
            .getOrNull()

    override suspend fun getQuickConnectState(url: String, secret: String): QuickConnectSession? =
        runCatching { jellyfinApiClient.getQuickConnectState(url, secret) }
            .onFailure { Log.e(TAG, "Quick Connect state check failed", it) }
            .getOrNull()

    override suspend fun login(url: String, username: String, password: String): Boolean {
        return try {
            val authResult = jellyfinApiClient.authenticate(url = url, username = username, password = password)
                ?: return false
            saveAuthenticationResult(authResult)
        } catch (e: Exception) {
            Log.e(TAG, "Login failed", e)
            false
        }
    }

    override suspend fun loginWithQuickConnect(url: String, secret: String): Boolean {
        return try {
            val authResult = jellyfinApiClient.authenticateWithQuickConnect(url = url, secret = secret)
                ?: return false
            saveAuthenticationResult(authResult)
        } catch (e: Exception) {
            Log.e(TAG, "Quick Connect login failed", e)
            false
        }
    }

    private suspend fun saveAuthenticationResult(authResult: AuthenticationResult): Boolean {
        val token = authResult.accessToken ?: return false
        val userId = authResult.user?.id ?: return false

        userSessionRepository.setAccessToken(accessToken = token)
        userSessionRepository.setUserId(userId)
        userSessionRepository.setLoggedIn(true)
        return true
    }

    private companion object {
        private const val TAG = "JellyfinAuthRepo"
    }
}
