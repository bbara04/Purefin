package hu.bbara.purefin.data.jellyfin.session

import android.util.Log
import hu.bbara.purefin.data.AuthenticationRepository
import hu.bbara.purefin.data.session.UserSessionRepository
import hu.bbara.purefin.data.jellyfin.client.JellyfinApiClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JellyfinAuthenticationRepository @Inject constructor(
    private val jellyfinApiClient: JellyfinApiClient,
    private val userSessionRepository: UserSessionRepository,
) : AuthenticationRepository {
    override suspend fun login(url: String, username: String, password: String): Boolean {
        return try {
            val authResult = jellyfinApiClient.authenticate(url = url, username = username, password = password)
                ?: return false
            val token = authResult.accessToken ?: return false
            val userId = authResult.user?.id ?: return false

            userSessionRepository.setAccessToken(accessToken = token)
            userSessionRepository.setUserId(userId)
            userSessionRepository.setLoggedIn(true)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Login failed", e)
            false
        }
    }

    private companion object {
        private const val TAG = "JellyfinAuthRepo"
    }
}
