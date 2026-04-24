package hu.bbara.purefin.data.jellyfin.session

import hu.bbara.purefin.data.SessionBootstrapper
import hu.bbara.purefin.data.jellyfin.client.JellyfinApiClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JellyfinSessionBootstrapper @Inject constructor(
    private val jellyfinApiClient: JellyfinApiClient,
) : SessionBootstrapper {
    override suspend fun initialize() {
        jellyfinApiClient.configureFromSession()
    }
}
