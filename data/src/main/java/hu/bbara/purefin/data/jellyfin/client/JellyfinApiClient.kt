package hu.bbara.purefin.data.jellyfin.client

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.bbara.purefin.data.PlaybackMethod
import hu.bbara.purefin.data.PlaybackReportContext
import hu.bbara.purefin.data.UserSessionRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.jellyfin.sdk.api.client.Response
import org.jellyfin.sdk.api.client.extensions.authenticateUserByName
import org.jellyfin.sdk.api.client.extensions.genresApi
import org.jellyfin.sdk.api.client.extensions.itemsApi
import org.jellyfin.sdk.api.client.extensions.mediaInfoApi
import org.jellyfin.sdk.api.client.extensions.mediaSegmentsApi
import org.jellyfin.sdk.api.client.extensions.playStateApi
import org.jellyfin.sdk.api.client.extensions.suggestionsApi
import org.jellyfin.sdk.api.client.extensions.tvShowsApi
import org.jellyfin.sdk.api.client.extensions.userApi
import org.jellyfin.sdk.api.client.extensions.userLibraryApi
import org.jellyfin.sdk.api.client.extensions.userViewsApi
import org.jellyfin.sdk.api.client.extensions.videosApi
import org.jellyfin.sdk.api.operations.SystemApi
import org.jellyfin.sdk.createJellyfin
import org.jellyfin.sdk.model.ClientInfo
import org.jellyfin.sdk.model.api.AuthenticationResult
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemDtoQueryResult
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.CollectionType
import org.jellyfin.sdk.model.api.DeviceProfile
import org.jellyfin.sdk.model.api.ItemFields
import org.jellyfin.sdk.model.api.MediaSegmentDto
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
    private val jellyfin = createJellyfin {
        context = applicationContext
        clientInfo = ClientInfo(name = "Purefin", version = "0.0.1")
    }

    private val api = jellyfin.createApi()

    private val itemFields =
        listOf(
            ItemFields.CHILD_COUNT,
            ItemFields.PARENT_ID,
            ItemFields.DATE_LAST_REFRESHED,
            ItemFields.OVERVIEW,
            ItemFields.SEASON_USER_DATA,
        )

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

    suspend fun configureFromSession(): Boolean = withContext(Dispatchers.IO) {
        logApiFailure("configureFromSession") {
            ensureConfigured()
        }
    }

    suspend fun authenticate(
        url: String,
        username: String,
        password: String,
    ): AuthenticationResult? = withContext(Dispatchers.IO) {
        logApiFailure("authenticate") {
            val trimmedUrl = url.trim()
            if (trimmedUrl.isBlank()) {
                return@logApiFailure null
            }

            api.update(baseUrl = trimmedUrl)
            api.userApi.authenticateUserByName(username = username, password = password).content
        }
    }

    suspend fun searchBySearchTerm(searchTerm: String): List<BaseItemDto> = withContext(Dispatchers.IO) {
        logApiFailure("searchBySearchTerm") {
            if (!ensureConfigured()) {
                return@logApiFailure emptyList()
            }
            val response = api.itemsApi.getItems(
                userId = getUserId(),
                includeItemTypes = listOf(BaseItemKind.MOVIE, BaseItemKind.SERIES),
                searchTerm = searchTerm,
            )
            Log.d("searchBySearchTerm", response.content.toString())
            response.content.items
        }
    }

    suspend fun searchByGenre(genres: List<String>): List<BaseItemDto> = withContext(Dispatchers.IO) {
        logApiFailure("searchMovie") {
            if (!ensureConfigured()) {
                return@logApiFailure emptyList()
            }
            val response = api.itemsApi.getItems(
                userId = getUserId(),
                includeItemTypes = listOf(BaseItemKind.MOVIE, BaseItemKind.SERIES),
                genres = genres
            )
            Log.d("searchByGenre", response.content.toString())
            response.content.items
        }
    }

    suspend fun getLibraries(): List<BaseItemDto> = withContext(Dispatchers.IO) {
        logApiFailure("getLibraries") {
            if (!ensureConfigured()) {
                return@logApiFailure emptyList()
            }
            val response = api.userViewsApi.getUserViews(
                userId = getUserId(),
                presetViews = listOf(CollectionType.MOVIES, CollectionType.TVSHOWS),
                includeHidden = false,
            )
            Log.d("getLibraries", response.content.toString())
            response.content.items
        }
    }

    suspend fun getLibraryContent(libraryId: UUID): List<BaseItemDto> = withContext(Dispatchers.IO) {
        logApiFailure("getLibraryContent($libraryId)") {
            if (!ensureConfigured()) {
                return@logApiFailure emptyList()
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
    }

    suspend fun getSuggestions(): List<BaseItemDto> = withContext(Dispatchers.IO) {
        logApiFailure("getSuggestions") {
            if (!ensureConfigured()) {
                return@logApiFailure emptyList()
            }
            val userId = getUserId() ?: return@logApiFailure emptyList()
            val response = api.suggestionsApi.getSuggestions(
                userId = userId,
                mediaType = listOf(MediaType.VIDEO),
                type = listOf(BaseItemKind.MOVIE, BaseItemKind.SERIES),
                limit = 8,
                enableTotalRecordCount = true,
            )
            Log.d("getSuggestions", response.content.toString())
            response.content.items
        }
    }

    suspend fun getContinueWatching(): List<BaseItemDto> = withContext(Dispatchers.IO) {
        logApiFailure("getContinueWatching") {
            if (!ensureConfigured()) {
                return@logApiFailure emptyList()
            }
            val userId = getUserId() ?: return@logApiFailure emptyList()
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
    }

    suspend fun getNextUpEpisodes(): List<BaseItemDto> = withContext(Dispatchers.IO) {
        logApiFailure("getNextUpEpisodes") {
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
    }

    suspend fun getLatestFromLibrary(libraryId: UUID): List<BaseItemDto> = withContext(Dispatchers.IO) {
        logApiFailure("getLatestFromLibrary($libraryId)") {
            if (!ensureConfigured()) {
                return@logApiFailure emptyList()
            }
            val response = api.userLibraryApi.getLatestMedia(
                userId = getUserId(),
                parentId = libraryId,
                fields = itemFields,
                includeItemTypes = listOf(BaseItemKind.MOVIE, BaseItemKind.EPISODE, BaseItemKind.SEASON),
                limit = 10,
            )
            Log.d("getLatestFromLibrary", response.content.toString())
            response.content
        }
    }

    suspend fun getItemInfo(mediaId: UUID): BaseItemDto? = withContext(Dispatchers.IO) {
        logApiFailure("getItemInfo($mediaId)") {
            if (!ensureConfigured()) {
                return@logApiFailure null
            }
            val result = api.userLibraryApi.getItem(itemId = mediaId, userId = getUserId())
            Log.d("getItemInfo", result.content.toString())
            result.content
        }
    }

    suspend fun getSeasons(seriesId: UUID): List<BaseItemDto> = withContext(Dispatchers.IO) {
        logApiFailure("getSeasons($seriesId)") {
            if (!ensureConfigured()) {
                return@logApiFailure emptyList()
            }
            val result = api.tvShowsApi.getSeasons(
                userId = getUserId(),
                seriesId = seriesId,
                fields = itemFields,
                enableUserData = true,
            )
            Log.d("getSeasons", result.content.toString())
            result.content.items
        }
    }

    suspend fun getEpisodesInSeason(seriesId: UUID, seasonId: UUID): List<BaseItemDto> = withContext(Dispatchers.IO) {
        logApiFailure("getEpisodesInSeason(series=$seriesId, season=$seasonId)") {
            if (!ensureConfigured()) {
                return@logApiFailure emptyList()
            }
            val result = api.tvShowsApi.getEpisodes(
                userId = getUserId(),
                seriesId = seriesId,
                seasonId = seasonId,
                fields = itemFields,
                enableUserData = true,
            )
            Log.d("getEpisodesInSeason", result.content.toString())
            result.content.items
        }
    }

    suspend fun getNextEpisodes(episodeId: UUID, count: Int): List<BaseItemDto> = withContext(Dispatchers.IO) {
        logApiFailure("getNextEpisodes($episodeId, count=$count)") {
            if (!ensureConfigured()) {
                return@logApiFailure emptyList()
            }
            val episodeInfo = getItemInfo(episodeId) ?: return@logApiFailure emptyList()
            val seriesId = episodeInfo.seriesId ?: return@logApiFailure emptyList()
            val nextUpEpisodesResult = api.tvShowsApi.getEpisodes(
                userId = getUserId(),
                seriesId = seriesId,
                enableUserData = true,
                startItemId = episodeId,
                limit = count,
            )
            val nextUpEpisodes = nextUpEpisodesResult.content.items
            Log.d("getNextEpisodes", nextUpEpisodes.toString())
            nextUpEpisodes
        }
    }

    suspend fun getGenres(id: UUID? = null) : List<BaseItemDto> = withContext(Dispatchers.IO) {
        logApiFailure("getGenres($id)") {
            if (!ensureConfigured()) {
                return@logApiFailure emptyList()
            }
            val result = api.genresApi.getGenres(
                userId = getUserId(),
                parentId = id,
            )
            Log.d("getGenres", result.toString())
            result.content.items
        }
    }

    suspend fun getMediaSources(mediaId: UUID): List<MediaSourceInfo> = withContext(Dispatchers.IO) {
        logApiFailure("getMediaSources($mediaId)") {
            if (!ensureConfigured()) {
                return@logApiFailure emptyList()
            }
            val result = api.mediaInfoApi.getPostedPlaybackInfo(
                mediaId,
                PlaybackInfoDto(
                    userId = getUserId(),
                    deviceProfile = null,
                    maxStreamingBitrate = 100_000_000,
                ),
            )
            Log.d("getMediaSources", result.toString())
            result.content.mediaSources
        }
    }

    suspend fun getMediaSegments(mediaId: UUID) : List<MediaSegmentDto> = withContext(Dispatchers.IO) {
        logApiFailure("getMediaSegments($mediaId)") {
            if (!ensureConfigured()) {
                return@logApiFailure emptyList()
            }
            val result = api.mediaSegmentsApi.getItemSegments(
                itemId = mediaId,
                //includeSegmentTypes = listOf(MediaSegmentType.INTRO)
            )
            Log.d("getMediaSegments", result.toString())
            result.content.items
        }
    }

    suspend fun getPlaybackInfo(
        mediaId: UUID,
        deviceProfile: DeviceProfile,
    ): PlaybackInfoResponse? = withContext(Dispatchers.IO) {
        logApiFailure("getPlaybackInfo($mediaId)") {
            if (!ensureConfigured()) {
                return@logApiFailure null
            }
            api.mediaInfoApi.getPostedPlaybackInfo(
                mediaId,
                PlaybackInfoDto(
                    userId = getUserId(),
                    deviceProfile = deviceProfile,
                    enableDirectPlay = true,
                    enableDirectStream = true,
                    enableTranscoding = true,
                    allowVideoStreamCopy = true,
                    allowAudioStreamCopy = true,
                    autoOpenLiveStream = false,
                ),
            ).content
        }
    }

    fun getVideoStreamUrl(
        itemId: UUID,
        mediaSourceId: String?,
        container: String? = null,
        tag: String? = null,
        playSessionId: String? = null,
        liveStreamId: String? = null,
    ): String = try {
        api.videosApi.getVideoStreamUrl(
            itemId = itemId,
            container = container,
            mediaSourceId = mediaSourceId,
            static = true,
            tag = tag,
            playSessionId = playSessionId,
            liveStreamId = liveStreamId,
        )
    } catch (error: Exception) {
        Log.e(TAG, "getVideoStreamUrl($itemId) failed", error)
        throw error
    }

    suspend fun getPublicSystemInfoVersion(): String? = withContext(Dispatchers.IO) {
        logApiFailure("getPublicSystemInfoVersion") {
            if (!ensureConfigured()) {
                return@logApiFailure null
            }
            SystemApi(api).getPublicSystemInfo().content.version
        }
    }

    suspend fun reportPlaybackStart(
        itemId: UUID,
        positionTicks: Long = 0L,
        reportContext: PlaybackReportContext,
    ) = withContext(Dispatchers.IO) {
        logApiFailure("reportPlaybackStart($itemId)") {
            if (!ensureConfigured()) return@logApiFailure
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
                    playMethod = reportContext.playMethod.toJellyfinPlayMethod(),
                    repeatMode = RepeatMode.REPEAT_NONE,
                    playbackOrder = PlaybackOrder.DEFAULT,
                ),
            )
        }
    }

    suspend fun reportPlaybackProgress(
        itemId: UUID,
        positionTicks: Long,
        isPaused: Boolean,
        reportContext: PlaybackReportContext,
    ) = withContext(Dispatchers.IO) {
        logApiFailure("reportPlaybackProgress($itemId)") {
            if (!ensureConfigured()) return@logApiFailure
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
                    playMethod = reportContext.playMethod.toJellyfinPlayMethod(),
                    repeatMode = RepeatMode.REPEAT_NONE,
                    playbackOrder = PlaybackOrder.DEFAULT,
                ),
            )
        }
    }

    suspend fun reportPlaybackStopped(
        itemId: UUID,
        positionTicks: Long,
        reportContext: PlaybackReportContext,
    ) = withContext(Dispatchers.IO) {
        logApiFailure("reportPlaybackStopped($itemId)") {
            if (!ensureConfigured()) return@logApiFailure
            api.playStateApi.reportPlaybackStopped(
                PlaybackStopInfo(
                    itemId = itemId,
                    positionTicks = positionTicks,
                    mediaSourceId = reportContext.mediaSourceId,
                    liveStreamId = reportContext.liveStreamId,
                    playSessionId = reportContext.playSessionId,
                    failed = false,
                ),
            )
        }
    }

    private suspend fun <T> logApiFailure(operation: String, block: suspend () -> T): T {
        return try {
            block()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Log.e(TAG, "$operation failed", error)
            throw error
        }
    }

    private fun PlaybackMethod.toJellyfinPlayMethod(): PlayMethod = when (this) {
        PlaybackMethod.DIRECT_PLAY -> PlayMethod.DIRECT_PLAY
        PlaybackMethod.DIRECT_STREAM -> PlayMethod.DIRECT_STREAM
        PlaybackMethod.TRANSCODE -> PlayMethod.TRANSCODE
    }

    companion object {
        private const val TAG = "JellyfinApiClient"
    }
}
