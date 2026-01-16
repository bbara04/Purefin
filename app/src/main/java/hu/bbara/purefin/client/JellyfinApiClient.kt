package hu.bbara.purefin.client

import android.content.Context
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import hu.bbara.purefin.session.UserSessionRepository
import jakarta.inject.Inject
import org.jellyfin.sdk.api.client.extensions.authenticateUserByName
import org.jellyfin.sdk.api.client.extensions.userApi
import org.jellyfin.sdk.createJellyfin
import org.jellyfin.sdk.model.ClientInfo

@Module
@InstallIn(SingletonComponent::class)
class JellyfinApiClient @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val userSessionRepository: UserSessionRepository,
) {
    val jellyfin = createJellyfin {
        context = applicationContext
        clientInfo = ClientInfo(name = "Purefin", version = "0.0.1")
    }

    suspend fun login(username: String, password: String): Boolean {
        val api = jellyfin.createApi(baseUrl = userSessionRepository.getUrl())
        val response = api.userApi.authenticateUserByName(username = username, password = password)
        val authResult = response.content

        //TODO set loggedIn false?
        val token = authResult.accessToken ?: return false
        userSessionRepository.updateAccessToken(accessToken = token)
        userSessionRepository.setLoggedIn(true)
        return true
    }

}