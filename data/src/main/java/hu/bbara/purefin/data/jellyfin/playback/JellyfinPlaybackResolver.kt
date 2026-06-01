package hu.bbara.purefin.data.jellyfin.playback

import hu.bbara.purefin.core.data.PlaybackMethod
import hu.bbara.purefin.core.data.PlaybackReportContext
import hu.bbara.purefin.core.data.UserSessionRepository
import hu.bbara.purefin.data.jellyfin.client.JellyfinApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JellyfinPlaybackResolver @Inject constructor(
    private val jellyfinApiClient: JellyfinApiClient,
    private val userSessionRepository: UserSessionRepository,
    private val playbackProfilePolicy: PlaybackProfilePolicy,
) {

    suspend fun getPlaybackSource(mediaId: UUID): PlaybackSource? = withContext(Dispatchers.IO) {
        val serverUrl = userSessionRepository.serverUrl.first().trim()
        if (serverUrl.isBlank()) {
            return@withContext null
        }

        val playbackInfo = jellyfinApiClient.getPlaybackInfo(
            mediaId = mediaId,
            deviceProfile = playbackProfilePolicy.create(),
        ) ?: return@withContext null

        if (playbackInfo.errorCode != null) {
            Timber.tag(TAG).w("Playback info failed for $mediaId with ${playbackInfo.errorCode}")
            return@withContext null
        }

        if (playbackInfo.mediaSources.isEmpty()) {
            Timber.tag(TAG).w("No media sources available for $mediaId")
            return@withContext null
        }

        val directPlayUrl = jellyfinApiClient.getVideoStreamUrl(
            itemId = mediaId,
        )
        if (directPlayUrl.isBlank()) {
            Timber.tag(TAG).e("Direct play URL is blank for $mediaId")
            return@withContext null
        }

        PlaybackSource(
            mediaSource = playbackInfo.mediaSources.first(),
            directPlayUrl = directPlayUrl,
            transcodingUrl = playbackInfo.mediaSources.firstOrNull()?.transcodingUrl,
            playbackReportContext = PlaybackReportContext(
                playMethod = PlaybackMethod.DIRECT_PLAY,
                mediaSourceId = playbackInfo.mediaSources.firstOrNull()?.id,
                audioStreamIndex = playbackInfo.mediaSources.firstOrNull()?.defaultAudioStreamIndex,
                subtitleStreamIndex = playbackInfo.mediaSources.firstOrNull()?.defaultSubtitleStreamIndex,
                playSessionId = playbackInfo.playSessionId,
            ),
        )
    }

    private companion object {
        private const val TAG = "PlaybackResolver"
    }
}
