package hu.bbara.purefin.client

import hu.bbara.purefin.session.UserSessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JellyfinAuthInterceptor @Inject constructor(
    userSessionRepository: UserSessionRepository
) : Interceptor {

    @Volatile
    private var cachedToken: String = ""

    init {
        userSessionRepository.accessToken
            .onEach { cachedToken = it }
            .launchIn(CoroutineScope(SupervisorJob() + Dispatchers.IO))
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = cachedToken
        val request = chain.request().newBuilder()
            .addHeader("X-Emby-Token", token)
            .build()
        return chain.proceed(request)
    }
}
