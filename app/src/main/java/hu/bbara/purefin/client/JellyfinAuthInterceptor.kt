package hu.bbara.purefin.client

import hu.bbara.purefin.session.UserSessionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class JellyfinAuthInterceptor @Inject constructor (
    private val userSessionRepository: UserSessionRepository
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { userSessionRepository.accessToken.first() }
        val request = chain.request().newBuilder()
            .addHeader("X-Emby-Token", token)
            // Some Jellyfin versions prefer the Authorization header:
            // .addHeader("Authorization", "MediaBrowser Client=\"YourAppName\", Device=\"YourDevice\", DeviceId=\"123\", Version=\"1.0.0\", Token=\"$token\"")
            .build()
        return chain.proceed(request)
    }
}