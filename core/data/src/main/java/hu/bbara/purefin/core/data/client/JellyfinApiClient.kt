package hu.bbara.purefin.core.data.client

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.bbara.purefin.core.data.session.UserSessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.jellyfin.sdk.api.client.Response
import org.jellyfin.sdk.api.client.extensions.authenticateUserByName
import org.jellyfin.sdk.api.client.extensions.itemsApi
import org.jellyfin.sdk.api.client.extensions.mediaInfoApi
import org.jellyfin.sdk.api.client.extensions.playStateApi
import org.jellyfin.sdk.api.client.extensions.suggestionsApi
import org.jellyfin.sdk.api.client.extensions.tvShowsApi
import org.jellyfin.sdk.api.client.extensions.userApi
import org.jellyfin.sdk.api.client.extensions.userLibraryApi
import org.jellyfin.sdk.api.client.extensions.userViewsApi
import org.jellyfin.sdk.api.client.extensions.videosApi
import org.jellyfin.sdk.createJellyfin
import org.jellyfin.sdk.model.ClientInfo
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemDtoQueryResult
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.CollectionType
import org.jellyfin.sdk.model.api.ItemFields
import org.jellyfin.sdk.model.api.MediaSourceInfo
import org.jellyfin.sdk.model.api.MediaType
import org.jellyfin.sdk.model.api.PlayMethod
import org.jellyfin.sdk.model.api.PlaybackInfoDto
import org.jellyfin.sdk.model.api.PlaybackInfoResponse
import org.jellyfin.sdk.model.api.PlaybackOrder
import org.jellyfin.sdk.model.api.PlaybackProgressInfo
import org.jellyfin.sdk.model.api.PlaybackStartInfo
import org.jellyfin.sdk.model.api.PlaybackStopInfo
import org.jellyfin.sdk.model.api.RepeatMode
import org.jellyfin.sdk.model.api.request.GetItemsRequest
import org.jellyfin.sdk.model.api.request.GetNextUpRequest
import org.jellyfin.sdk.model.api.request.GetResumeItemsRequest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JellyfinApiClient @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val userSessionRepository: UserSessionRepository,
) {
    companion object {
        private const val TAG = "JellyfinApiClient"
        private const val MAX_STREAMING_BITRATE = 100_000_000
    }

    private val jellyfin = createJellyfin {
        context = applicationContext
        clientInfo = ClientInfo(name = "Purefin", version = "0.0.1")
    }

    private val api = jellyfin.createApi()
    
    private suspend fun getUserId(): UUID? = userSessionRepository.userId.first()

    private suspend fun ensureConfigured(): Boolean {
        val serverUrl = userSessionRepository.serverUrl.first().trim()
        val accessToken = userSessionRepository.accessToken.first().trim()
        if (serverUrl.isBlank() || accessToken.isBlank()) {
            userSessionRepository.setLoggedIn(false)
            return false
        }
        api.update(baseUrl = serverUrl, accessToken = accessToken)
        return true
    }

    suspend fun login(url: String, username: String, password: String): Boolean = withContext(Dispatchers.IO) {
        val trimmedUrl = url.trim()
        if (trimmedUrl.isBlank()) {
            return@withContext false
        }
        api.update(baseUrl = trimmedUrl)
        try {
            val response = api.userApi.authenticateUserByName(username = username, password = password)
            val authResult = response.content

            val token = authResult.accessToken ?: return@withContext false
            val userId = authResult.user?.id ?: return@withContext false
            userSessionRepository.setAccessToken(accessToken = token)
            userSessionRepository.setUserId(userId)
            userSessionRepository.setLoggedIn(true)
            api.update(accessToken = token)
            true
        } catch (e: Exception) {
            Log.e("JellyfinApiClient", "Login failed", e)
            false
        }
    }

    suspend fun updateApiClient() = withContext(Dispatchers.IO) {
        ensureConfigured()
    }

    suspend fun getLibraries(): List<BaseItemDto> = withContext(Dispatchers.IO) {
        if (!ensureConfigured()) {
            return@withContext emptyList()
        }
        val response = api.userViewsApi.getUserViews(
            userId = getUserId(),
            presetViews = listOf(CollectionType.MOVIES, CollectionType.TVSHOWS),
            includeHidden = false,
        )
        Log.d("getLibraries", response.content.toString())
        response.content.items
    }

    private val itemFields =
        listOf(
            ItemFields.CHILD_COUNT,
            ItemFields.PARENT_ID,
            ItemFields.DATE_LAST_REFRESHED,
            ItemFields.OVERVIEW,
            ItemFields.SEASON_USER_DATA,
        )

    suspend fun getLibraryContent(libraryId: UUID): List<BaseItemDto> = withContext(Dispatchers.IO) {
        if (!ensureConfigured()) {
            return@withContext emptyList()
        }
        val getItemsRequest = GetItemsRequest(
            userId = getUserId(),
            enableImages = true,
            parentId = libraryId,
            fields = itemFields,
            enableUserData = true,
            includeItemTypes = listOf(BaseItemKind.MOVIE, BaseItemKind.SERIES),
            recursive = true,
        )
        val response = api.itemsApi.getItems(getItemsRequest)
        Log.d("getLibraryContent", response.content.toString())
        response.content.items
    }

    suspend fun getSuggestions(): List<BaseItemDto> = withContext(Dispatchers.IO) {
        if (!ensureConfigured()) {
            return@withContext emptyList()
        }
        val userId = getUserId()
        if (userId == null) {
            return@withContext emptyList()
        }
        val response = api.suggestionsApi.getSuggestions(
            userId = userId,
            mediaType = listOf(MediaType.VIDEO),
            type = listOf(BaseItemKind.MOVIE, BaseItemKind.SERIES),
            limit = 8,
            enableTotalRecordCount = true
        )
        Log.d("getSuggestions", response.content.toString())
        response.content.items
    }

    suspend fun getContinueWatching(): List<BaseItemDto> = withContext(Dispatchers.IO) {
        if (!ensureConfigured()) {
            return@withContext emptyList()
        }
        val userId = getUserId()
        if (userId == null) {
            return@withContext emptyList()
        }
        val getResumeItemsRequest = GetResumeItemsRequest(
            userId = userId,
            fields = itemFields,
            includeItemTypes = listOf(BaseItemKind.MOVIE, BaseItemKind.EPISODE),
            enableUserData = true,
            startIndex = 0,
        )
        val response: Response<BaseItemDtoQueryResult> = api.itemsApi.getResumeItems(getResumeItemsRequest)
        Log.d("getContinueWatching", response.content.toString())
        response.content.items
    }

    suspend fun getNextUpEpisodes(): List<BaseItemDto> = withContext(Dispatchers.IO) {
        if (!ensureConfigured()) {
            throw IllegalStateException("Not configured")
        }
        val getNextUpRequest = GetNextUpRequest(
            userId = getUserId(),
            fields = itemFields,
            enableResumable = false,
        )
        val result = api.tvShowsApi.getNextUp(getNextUpRequest)
        Log.d("getNextUpEpisodes", result.content.toString())
        result.content.items
    }

    /**
     * Fetches the latest media items from a specified library including Movie, Episode, Season.
     *
     * @param libraryId The UUID of the library to fetch from
     * @return A list of [BaseItemDto] representing the latest media items that includes Movie, Episode, Season, or an empty list if not configured
     */
    suspend fun getLatestFromLibrary(libraryId: UUID): List<BaseItemDto> = withContext(Dispatchers.IO) {
        if (!ensureConfigured()) {
            return@withContext emptyList()
        }
        val response = api.userLibraryApi.getLatestMedia(
            userId = getUserId(),
            parentId = libraryId,
            fields = itemFields,
            includeItemTypes = listOf(BaseItemKind.MOVIE, BaseItemKind.EPISODE, BaseItemKind.SEASON),
            limit = 10
        )
        Log.d("getLatestFromLibrary", response.content.toString())
        response.content
    }

    suspend fun getItemInfo(mediaId: UUID): BaseItemDto? = withContext(Dispatchers.IO) {
        if (!ensureConfigured()) {
            return@withContext null
        }
        val result = api.userLibraryApi.getItem(
            itemId = mediaId,
            userId = getUserId()
        )
        Log.d("getItemInfo", result.content.toString())
        result.content
    }

    suspend fun getSeasons(seriesId: UUID): List<BaseItemDto> = withContext(Dispatchers.IO) {
        if (!ensureConfigured()) {
            return@withContext emptyList()
        }
        val result = api.tvShowsApi.getSeasons(
            userId = getUserId(),
            seriesId = seriesId,
            fields = itemFields,
            enableUserData = true
        )
        Log.d("getSeasons", result.content.toString())
        result.content.items
    }

    suspend fun getEpisodesInSeason(seriesId: UUID, seasonId: UUID): List<BaseItemDto> = withContext(Dispatchers.IO) {
        if (!ensureConfigured()) {
            return@withContext emptyList()
        }
        val result = api.tvShowsApi.getEpisodes(
            userId = getUserId(),
            seriesId = seriesId,
            seasonId = seasonId,
            fields = itemFields,
            enableUserData = true
        )
        Log.d("getEpisodesInSeason", result.content.toString())
        result.content.items
    }

    suspend fun getNextEpisodes(episodeId: UUID, count: Int = 10): List<BaseItemDto> = withContext(Dispatchers.IO) {
        if (!ensureConfigured()) {
            return@withContext emptyList()
        }
        // TODO pass complete Episode object not only an id
        val episodeInfo = getItemInfo(episodeId) ?: return@withContext emptyList()
        val seriesId = episodeInfo.seriesId ?: return@withContext emptyList()
        val nextUpEpisodesResult = api.tvShowsApi.getEpisodes(
            userId = getUserId(),
            seriesId = seriesId,
            enableUserData = true,
            startItemId = episodeId,
            limit = count + 1
        )
        //Remove first element as we need only the next episodes
        val nextUpEpisodes = nextUpEpisodesResult.content.items.drop(1)
        Log.d("getNextEpisodes", nextUpEpisodes.toString())
        nextUpEpisodes
    }

    suspend fun getMediaSources(mediaId: UUID): List<MediaSourceInfo> = withContext(Dispatchers.IO) {
        requestPlaybackInfo(mediaId)?.mediaSources ?: emptyList()
    }

    suspend fun getPlaybackDecision(mediaId: UUID, forceTranscode: Boolean = false): PlaybackDecision? = withContext(Dispatchers.IO) {
        val playbackInfo = requestPlaybackInfo(mediaId, forceTranscode) ?: return@withContext null
        val capabilitySnapshot = AndroidDeviceProfile.getSnapshot(applicationContext)
        val selectedMediaSource = selectMediaSource(playbackInfo.mediaSources, forceTranscode) ?: return@withContext null

        val url = when {
            forceTranscode && selectedMediaSource.supportsTranscoding -> {
                resolveTranscodeUrl(
                    mediaId = mediaId,
                    mediaSource = selectedMediaSource,
                    playSessionId = playbackInfo.playSessionId,
                    maxAudioChannels = capabilitySnapshot.maxAudioChannels
                )
            }

            !forceTranscode && selectedMediaSource.supportsDirectPlay -> {
                api.videosApi.getVideoStreamUrl(
                    itemId = mediaId,
                    static = true,
                    mediaSourceId = selectedMediaSource.id,
                    playSessionId = playbackInfo.playSessionId,
                    liveStreamId = selectedMediaSource.liveStreamId
                )
            }

            !forceTranscode && selectedMediaSource.supportsDirectStream -> {
                api.videosApi.getVideoStreamUrl(
                    itemId = mediaId,
                    static = false,
                    mediaSourceId = selectedMediaSource.id,
                    playSessionId = playbackInfo.playSessionId,
                    liveStreamId = selectedMediaSource.liveStreamId,
                    container = selectedMediaSource.transcodingContainer ?: selectedMediaSource.container,
                    enableAutoStreamCopy = true,
                    allowVideoStreamCopy = true,
                    allowAudioStreamCopy = true,
                    maxAudioChannels = capabilitySnapshot.maxAudioChannels
                )
            }

            selectedMediaSource.supportsTranscoding -> {
                resolveTranscodeUrl(
                    mediaId = mediaId,
                    mediaSource = selectedMediaSource,
                    playSessionId = playbackInfo.playSessionId,
                    maxAudioChannels = capabilitySnapshot.maxAudioChannels
                )
            }

            else -> null
        } ?: return@withContext null

        val playMethod = when {
            forceTranscode -> PlayMethod.TRANSCODE
            selectedMediaSource.supportsDirectPlay -> PlayMethod.DIRECT_PLAY
            selectedMediaSource.supportsDirectStream -> PlayMethod.DIRECT_STREAM
            selectedMediaSource.supportsTranscoding -> PlayMethod.TRANSCODE
            else -> return@withContext null
        }

        val reportContext = PlaybackReportContext(
            playMethod = playMethod,
            mediaSourceId = selectedMediaSource.id,
            audioStreamIndex = selectedMediaSource.defaultAudioStreamIndex,
            subtitleStreamIndex = selectedMediaSource.defaultSubtitleStreamIndex,
            liveStreamId = selectedMediaSource.liveStreamId,
            playSessionId = playbackInfo.playSessionId,
            canRetryWithTranscoding = !forceTranscode &&
                playMethod != PlayMethod.TRANSCODE &&
                selectedMediaSource.supportsTranscoding
        )

        Log.d(TAG, "Playback decision for $mediaId: $playMethod using ${selectedMediaSource.id}")
        PlaybackDecision(
            url = url,
            mediaSource = selectedMediaSource,
            reportContext = reportContext
        )
    }

    suspend fun getMediaPlaybackUrl(mediaId: UUID, mediaSource: MediaSourceInfo): String? = withContext(Dispatchers.IO) {
        if (!ensureConfigured()) {
            return@withContext null
        }

        val capabilitySnapshot = AndroidDeviceProfile.getSnapshot(applicationContext)

        val url = when {
            mediaSource.supportsDirectPlay -> api.videosApi.getVideoStreamUrl(
                itemId = mediaId,
                static = true,
                mediaSourceId = mediaSource.id,
                liveStreamId = mediaSource.liveStreamId
            )

            mediaSource.supportsDirectStream -> api.videosApi.getVideoStreamUrl(
                itemId = mediaId,
                static = false,
                mediaSourceId = mediaSource.id,
                liveStreamId = mediaSource.liveStreamId,
                container = mediaSource.transcodingContainer ?: mediaSource.container,
                enableAutoStreamCopy = true,
                allowVideoStreamCopy = true,
                allowAudioStreamCopy = true,
                maxAudioChannels = capabilitySnapshot.maxAudioChannels
            )

            mediaSource.supportsTranscoding -> resolveTranscodeUrl(
                mediaId = mediaId,
                mediaSource = mediaSource,
                playSessionId = null,
                maxAudioChannels = capabilitySnapshot.maxAudioChannels
            )

            else -> null
        }

        Log.d(TAG, "Resolved standalone playback URL for $mediaId -> $url")
        url
    }

    suspend fun reportPlaybackStart(itemId: UUID, positionTicks: Long = 0L, reportContext: PlaybackReportContext) = withContext(Dispatchers.IO) {
        if (!ensureConfigured()) return@withContext
        api.playStateApi.reportPlaybackStart(
            PlaybackStartInfo(
                itemId = itemId,
                positionTicks = positionTicks,
                canSeek = true,
                isPaused = false,
                isMuted = false,
                mediaSourceId = reportContext.mediaSourceId,
                audioStreamIndex = reportContext.audioStreamIndex,
                subtitleStreamIndex = reportContext.subtitleStreamIndex,
                liveStreamId = reportContext.liveStreamId,
                playSessionId = reportContext.playSessionId,
                playMethod = reportContext.playMethod,
                repeatMode = RepeatMode.REPEAT_NONE,
                playbackOrder = PlaybackOrder.DEFAULT
            )
        )
    }

    suspend fun reportPlaybackProgress(
        itemId: UUID,
        positionTicks: Long,
        isPaused: Boolean,
        reportContext: PlaybackReportContext
    ) = withContext(Dispatchers.IO) {
        if (!ensureConfigured()) return@withContext
        api.playStateApi.reportPlaybackProgress(
            PlaybackProgressInfo(
                itemId = itemId,
                positionTicks = positionTicks,
                canSeek = true,
                isPaused = isPaused,
                isMuted = false,
                mediaSourceId = reportContext.mediaSourceId,
                audioStreamIndex = reportContext.audioStreamIndex,
                subtitleStreamIndex = reportContext.subtitleStreamIndex,
                liveStreamId = reportContext.liveStreamId,
                playSessionId = reportContext.playSessionId,
                playMethod = reportContext.playMethod,
                repeatMode = RepeatMode.REPEAT_NONE,
                playbackOrder = PlaybackOrder.DEFAULT
            )
        )
    }

    suspend fun reportPlaybackStopped(itemId: UUID, positionTicks: Long, reportContext: PlaybackReportContext) = withContext(Dispatchers.IO) {
        if (!ensureConfigured()) return@withContext
        api.playStateApi.reportPlaybackStopped(
            PlaybackStopInfo(
                itemId = itemId,
                positionTicks = positionTicks,
                mediaSourceId = reportContext.mediaSourceId,
                liveStreamId = reportContext.liveStreamId,
                playSessionId = reportContext.playSessionId,
                failed = false
            )
        )
    }

    private suspend fun requestPlaybackInfo(mediaId: UUID, forceTranscode: Boolean = false): PlaybackInfoResponse? {
        if (!ensureConfigured()) {
            return null
        }

        val capabilitySnapshot = AndroidDeviceProfile.getSnapshot(applicationContext)
        val response = api.mediaInfoApi.getPostedPlaybackInfo(
            mediaId,
            PlaybackInfoDto(
                userId = getUserId(),
                deviceProfile = capabilitySnapshot.deviceProfile,
                maxStreamingBitrate = MAX_STREAMING_BITRATE,
                maxAudioChannels = capabilitySnapshot.maxAudioChannels,
                enableDirectPlay = !forceTranscode,
                enableDirectStream = !forceTranscode,
                enableTranscoding = true,
                allowVideoStreamCopy = !forceTranscode,
                allowAudioStreamCopy = !forceTranscode,
                alwaysBurnInSubtitleWhenTranscoding = false
            )
        )

        Log.d(TAG, "PlaybackInfo for $mediaId -> sources=${response.content.mediaSources.size}, session=${response.content.playSessionId}")
        return response.content
    }

    private fun selectMediaSource(mediaSources: List<MediaSourceInfo>, forceTranscode: Boolean): MediaSourceInfo? {
        return when {
            forceTranscode -> mediaSources.firstOrNull { it.supportsTranscoding }
            else -> mediaSources.firstOrNull { it.supportsDirectPlay }
                ?: mediaSources.firstOrNull { it.supportsDirectStream }
                ?: mediaSources.firstOrNull { it.supportsTranscoding }
                ?: mediaSources.firstOrNull()
        }
    }

    private suspend fun resolveTranscodeUrl(
        mediaId: UUID,
        mediaSource: MediaSourceInfo,
        playSessionId: String?,
        maxAudioChannels: Int
    ): String? {
        mediaSource.transcodingUrl?.takeIf { it.isNotBlank() }?.let { transcodingUrl ->
            if (transcodingUrl.startsWith("http", ignoreCase = true)) {
                return transcodingUrl
            }
            val baseUrl = userSessionRepository.serverUrl.first().trim().trimEnd('/')
            return "$baseUrl$transcodingUrl"
        }

        return api.videosApi.getVideoStreamUrl(
            itemId = mediaId,
            static = false,
            mediaSourceId = mediaSource.id,
            playSessionId = playSessionId,
            liveStreamId = mediaSource.liveStreamId,
            container = mediaSource.transcodingContainer ?: mediaSource.container ?: "mp4",
            enableAutoStreamCopy = false,
            allowVideoStreamCopy = false,
            allowAudioStreamCopy = false,
            maxAudioChannels = maxAudioChannels
        )
    }
}
