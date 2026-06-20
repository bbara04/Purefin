package hu.bbara.purefin.data.jellyfin.session

import hu.bbara.purefin.core.data.SessionBootstrapper
import hu.bbara.purefin.data.jellyfin.client.JellyfinApiClient
import hu.bbara.purefin.data.jellyfin.websocket.JellyfinWebSocketService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JellyfinSessionBootstrapper @Inject constructor(
    private val jellyfinApiClient: JellyfinApiClient,
    // Referenced to ensure Hilt instantiates the singleton at app startup.
    // The service starts its own subscription in its init block.
    private val webSocketService: JellyfinWebSocketService,
) : SessionBootstrapper {
    override suspend fun initialize() {
        jellyfinApiClient.configureFromSession()
    }
}
