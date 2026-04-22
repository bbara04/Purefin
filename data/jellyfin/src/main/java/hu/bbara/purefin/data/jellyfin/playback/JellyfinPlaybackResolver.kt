package hu.bbara.purefin.data.jellyfin.playback

import android.util.Log
import hu.bbara.purefin.core.data.session.UserSessionRepository
import hu.bbara.purefin.data.jellyfin.client.JellyfinApiClient
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.jellyfin.sdk.model.ServerVersion

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
            Log.w(TAG, "Playback info failed for $mediaId with ${playbackInfo.errorCode}")
            return@withContext null
        }

        val decision = PlaybackDecisionResolver.resolve(
            mediaSources = playbackInfo.mediaSources,
            playSessionId = playbackInfo.playSessionId,
            serverUrl = serverUrl,
            directPlayUrl = { mediaSource ->
                jellyfinApiClient.getVideoStreamUrl(
                    itemId = mediaId,
                    container = mediaSource.container,
                    mediaSourceId = mediaSource.id,
                    tag = mediaSource.eTag,
                    playSessionId = playbackInfo.playSessionId,
                    liveStreamId = mediaSource.liveStreamId,
                )
            },
        )

        if (decision == null) {
            Log.w(TAG, "No compatible playback path for $mediaId")
        } else {
            Log.d(TAG, "Playback decision for $mediaId resolved as ${decision.reportContext.playMethod}")
        }
        decision
    }

    private suspend fun getServerVersion(serverUrl: String): ServerVersion {
        serverVersionCache[serverUrl]?.let { return it }

        val resolvedVersion = runCatching {
            jellyfinApiClient.getPublicSystemInfoVersion()?.let(ServerVersion::fromString)
        }.onFailure { error ->
            Log.w(TAG, "Unable to fetch server version for $serverUrl", error)
        }.getOrNull() ?: PlaybackProfileDefaults.fallbackServerVersion

        serverVersionCache[serverUrl] = resolvedVersion
        return resolvedVersion
    }

    private companion object {
        private const val TAG = "PlaybackResolver"
    }
}
