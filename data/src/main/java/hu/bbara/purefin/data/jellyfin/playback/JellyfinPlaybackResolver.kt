package hu.bbara.purefin.data.jellyfin.playback

import hu.bbara.purefin.core.data.UserSessionRepository
import hu.bbara.purefin.data.jellyfin.client.JellyfinApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.jellyfin.sdk.model.ServerVersion
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JellyfinPlaybackResolver @Inject constructor(
    private val jellyfinApiClient: JellyfinApiClient,
    private val userSessionRepository: UserSessionRepository,
    private val playbackProfilePolicy: PlaybackProfilePolicy,
) {
    private val serverVersionCache = ConcurrentHashMap<String, ServerVersion>()

    suspend fun getPlaybackDecision(mediaId: UUID): PlaybackDecision? = withContext(Dispatchers.IO) {
        val serverUrl = userSessionRepository.serverUrl.first().trim()
        if (serverUrl.isBlank()) {
            return@withContext null
        }

        val serverVersion = getServerVersion(serverUrl)
        val playbackInfo = jellyfinApiClient.getPlaybackInfo(
            mediaId = mediaId,
            deviceProfile = playbackProfilePolicy.create(serverVersion),
        ) ?: return@withContext null

        if (playbackInfo.errorCode != null) {
            Timber.tag(TAG).w("Playback info failed for $mediaId with ${playbackInfo.errorCode}")
            return@withContext null
        }

        val directPlayUrl = jellyfinApiClient.getVideoStreamUrl(
            itemId = mediaId,
        )
        if (directPlayUrl.isBlank()) {
            Timber.tag(TAG).e("Direct play URL is blank for $mediaId")
            return@withContext null
        }

        val decision = PlaybackDecisionResolver.resolve(
            mediaSources = playbackInfo.mediaSources,
            playSessionId = playbackInfo.playSessionId,
            serverUrl = serverUrl,
            directPlayUrl = directPlayUrl
        )

        if (decision == null) {
            Timber.tag(TAG).w("No compatible playback path for $mediaId")
        } else {
            Timber.tag(TAG).d("Playback decision for $mediaId resolved as ${decision.reportContext.playMethod}")
        }
        decision
    }

    private suspend fun getServerVersion(serverUrl: String): ServerVersion {
        serverVersionCache[serverUrl]?.let { return it }

        val resolvedVersion = runCatching {
            jellyfinApiClient.getPublicSystemInfoVersion()?.let(ServerVersion::fromString)
        }.onFailure { error ->
            Timber.tag(TAG).w(error, "Unable to fetch server version for $serverUrl")
        }.getOrNull() ?: PlaybackProfileDefaults.fallbackServerVersion

        serverVersionCache[serverUrl] = resolvedVersion
        return resolvedVersion
    }

    private companion object {
        private const val TAG = "PlaybackResolver"
    }
}
