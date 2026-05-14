package hu.bbara.purefin.data.jellyfin.session

import hu.bbara.purefin.core.data.AuthenticationRepository
import hu.bbara.purefin.core.data.JellyfinServerCandidate
import hu.bbara.purefin.core.data.QuickConnectSession
import hu.bbara.purefin.core.data.UserSessionRepository
import hu.bbara.purefin.data.jellyfin.client.JellyfinApiClient
import kotlinx.coroutines.flow.Flow
import org.jellyfin.sdk.model.api.AuthenticationResult
import timber.log.Timber
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
            .onFailure { Timber.tag(TAG).e(it, "Server search failed") }
            .getOrNull()

    override suspend fun isQuickConnectEnabled(url: String): Boolean =
        runCatching { jellyfinApiClient.isQuickConnectEnabled(url) }
            .onFailure { Timber.tag(TAG).e(it, "Quick Connect check failed") }
            .getOrDefault(false)

    override suspend fun initiateQuickConnect(url: String): QuickConnectSession? =
        runCatching { jellyfinApiClient.initiateQuickConnect(url) }
            .onFailure { Timber.tag(TAG).e(it, "Quick Connect initiation failed") }
            .getOrNull()

    override suspend fun getQuickConnectState(url: String, secret: String): QuickConnectSession? =
        runCatching { jellyfinApiClient.getQuickConnectState(url, secret) }
            .onFailure { Timber.tag(TAG).e(it, "Quick Connect state check failed") }
            .getOrNull()

    override suspend fun login(url: String, username: String, password: String): Boolean {
        return try {
            val authResult = jellyfinApiClient.authenticate(url = url, username = username, password = password)
                ?: return false
            saveAuthenticationResult(authResult)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Login failed")
            false
        }
    }

    override suspend fun loginWithQuickConnect(url: String, secret: String): Boolean {
        return try {
            val authResult = jellyfinApiClient.authenticateWithQuickConnect(url = url, secret = secret)
                ?: return false
            saveAuthenticationResult(authResult)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Quick Connect login failed")
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
