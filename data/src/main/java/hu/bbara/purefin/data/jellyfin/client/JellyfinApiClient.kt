package hu.bbara.purefin.data.jellyfin.client

import android.content.Context
import android.os.SystemClock
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.bbara.purefin.core.data.PlaybackMethod
import hu.bbara.purefin.core.data.PlaybackReportContext
import hu.bbara.purefin.core.data.UserSessionRepository
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

    private val defaultItemFields =
        listOf(
            ItemFields.CHILD_COUNT,
            ItemFields.PARENT_ID,
//            ItemFields.SEASON_USER_DATA,
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
        logRequest("configureFromSession") {
            ensureConfigured()
        }
    }

    suspend fun authenticate(
        url: String,
        username: String,
        password: String,
    ): AuthenticationResult? = withContext(Dispatchers.IO) {
        logRequest("authenticate") {
            val trimmedUrl = url.trim()
            if (trimmedUrl.isBlank()) {
                return@logRequest null
            }

            api.update(baseUrl = trimmedUrl)
            api.userApi.authenticateUserByName(username = username, password = password).content
        }
    }

    suspend fun searchBySearchTerm(searchTerm: String): List<BaseItemDto> = withContext(Dispatchers.IO) {
        logRequest("searchBySearchTerm") {
            if (!ensureConfigured()) {
                return@logRequest emptyList()
            }
            val response = api.itemsApi.getItems(
                userId = getUserId(),
                includeItemTypes = listOf(BaseItemKind.MOVIE, BaseItemKind.SERIES),
                searchTerm = searchTerm,
                recursive = true
            )
            Log.d("searchBySearchTerm", response.content.toString())
            response.content.items
        }
    }

    suspend fun searchByGenre(genres: Set<String>): List<BaseItemDto> = withContext(Dispatchers.IO) {
        logRequest("searchMovie") {
            if (!ensureConfigured()) {
                return@logRequest emptyList()
            }
            val response = api.itemsApi.getItems(
                userId = getUserId(),
                includeItemTypes = listOf(BaseItemKind.MOVIE, BaseItemKind.SERIES),
                genres = genres,
                recursive = true
            )
            Log.d("searchByGenre", response.content.toString())
            response.content.items
        }
    }

    suspend fun getLibraries(): List<BaseItemDto> = withContext(Dispatchers.IO) {
        logRequest("getLibraries") {
            if (!ensureConfigured()) {
                return@logRequest emptyList()
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
        logRequest("getLibraryContent($libraryId)") {
            if (!ensureConfigured()) {
                return@logRequest emptyList()
            }
            val getItemsRequest = GetItemsRequest(
                userId = getUserId(),
                enableImages = true,
                parentId = libraryId,
                fields = defaultItemFields + ItemFields.OVERVIEW,
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
        logRequest("getSuggestions") {
            if (!ensureConfigured()) {
                return@logRequest emptyList()
            }
            val userId = getUserId() ?: return@logRequest emptyList()
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
        logRequest("getContinueWatching") {
            if (!ensureConfigured()) {
                return@logRequest emptyList()
            }
            val userId = getUserId() ?: return@logRequest emptyList()
            val getResumeItemsRequest = GetResumeItemsRequest(
                userId = userId,
                fields = defaultItemFields + ItemFields.OVERVIEW,
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
        logRequest("getNextUpEpisodes") {
            if (!ensureConfigured()) {
                throw IllegalStateException("Not configured")
            }
            val getNextUpRequest = GetNextUpRequest(
                userId = getUserId(),
                fields = defaultItemFields + ItemFields.OVERVIEW,
                enableResumable = false,
            )
            val result = api.tvShowsApi.getNextUp(getNextUpRequest)
            Log.d("getNextUpEpisodes", result.content.toString())
            result.content.items
        }
    }

    suspend fun getLatestFromLibrary(libraryId: UUID): List<BaseItemDto> = withContext(Dispatchers.IO) {
        logRequest("getLatestFromLibrary($libraryId)") {
            if (!ensureConfigured()) {
                return@logRequest emptyList()
            }
            val response = api.userLibraryApi.getLatestMedia(
                userId = getUserId(),
                parentId = libraryId,
                fields = defaultItemFields + ItemFields.OVERVIEW,
                includeItemTypes = listOf(BaseItemKind.MOVIE, BaseItemKind.EPISODE, BaseItemKind.SEASON),
                limit = 10,
            )
            Log.d("getLatestFromLibrary", response.content.toString())
            response.content
        }
    }

    suspend fun getItemInfo(mediaId: UUID): BaseItemDto? = withContext(Dispatchers.IO) {
        logRequest("getItemInfo($mediaId)") {
            if (!ensureConfigured()) {
                return@logRequest null
            }
            val result = api.userLibraryApi.getItem(itemId = mediaId, userId = getUserId())
            Log.d("getItemInfo", result.content.toString())
            result.content
        }
    }

    suspend fun getSeasons(seriesId: UUID): List<BaseItemDto> = withContext(Dispatchers.IO) {
        logRequest("getSeasons($seriesId)") {
            if (!ensureConfigured()) {
                return@logRequest emptyList()
            }
            val result = api.tvShowsApi.getSeasons(
                userId = getUserId(),
                seriesId = seriesId,
                fields = defaultItemFields,
                enableUserData = true,
            )
            Log.d("getSeasons", result.content.toString())
            result.content.items
        }
    }

    suspend fun getEpisodesInSeason(seriesId: UUID, seasonId: UUID): List<BaseItemDto> = withContext(Dispatchers.IO) {
        logRequest("getEpisodesInSeason(series=$seriesId, season=$seasonId)") {
            if (!ensureConfigured()) {
                return@logRequest emptyList()
            }
            val result = api.tvShowsApi.getEpisodes(
                userId = getUserId(),
                seriesId = seriesId,
                seasonId = seasonId,
                fields = defaultItemFields + ItemFields.OVERVIEW,
                enableUserData = true,
            )
            Log.d("getEpisodesInSeason", result.content.toString())
            result.content.items
        }
    }

    suspend fun getNextEpisodes(episodeId: UUID, count: Int): List<BaseItemDto> = withContext(Dispatchers.IO) {
        logRequest("getNextEpisodes($episodeId, count=$count)") {
            if (!ensureConfigured()) {
                return@logRequest emptyList()
            }
            val episodeInfo = getItemInfo(episodeId) ?: return@logRequest emptyList()
            val seriesId = episodeInfo.seriesId ?: return@logRequest emptyList()
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
        logRequest("getGenres($id)") {
            if (!ensureConfigured()) {
                return@logRequest emptyList()
            }
            val result = api.genresApi.getGenres(
                userId = getUserId(),
                parentId = id,
            )
            Log.d("getGenres", result.toString())
            result.content.items
        }
    }

    suspend fun markAsWatched(mediaId: UUID) = withContext(Dispatchers.IO) {
        logRequest("markAsWatched($mediaId)") {
            if (!ensureConfigured()) {
                return@logRequest
            }
            api.playStateApi.markPlayedItem(
                itemId = mediaId,
                userId = getUserId(),
            )
        }
    }

    suspend fun markAsUnwatched(mediaId: UUID) = withContext(Dispatchers.IO) {
        logRequest("markAsUnwatched($mediaId)") {
            if (!ensureConfigured()) {
                return@logRequest
            }
            api.playStateApi.markUnplayedItem(
                itemId = mediaId,
                userId = getUserId(),
            )
        }
    }

    suspend fun getMediaSources(mediaId: UUID): List<MediaSourceInfo> = withContext(Dispatchers.IO) {
        logRequest("getMediaSources($mediaId)") {
            if (!ensureConfigured()) {
                return@logRequest emptyList()
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
        logRequest("getMediaSegments($mediaId)") {
            if (!ensureConfigured()) {
                return@logRequest emptyList()
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
        logRequest("getPlaybackInfo($mediaId)") {
            if (!ensureConfigured()) {
                return@logRequest null
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
        logRequest("getPublicSystemInfoVersion") {
            if (!ensureConfigured()) {
                return@logRequest null
            }
            SystemApi(api).getPublicSystemInfo().content.version
        }
    }

    suspend fun reportPlaybackStart(
        itemId: UUID,
        positionTicks: Long = 0L,
        reportContext: PlaybackReportContext,
    ) = withContext(Dispatchers.IO) {
        logRequest("reportPlaybackStart($itemId)") {
            if (!ensureConfigured()) return@logRequest
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
        logRequest("reportPlaybackProgress($itemId)") {
            if (!ensureConfigured()) return@logRequest
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
        logRequest("reportPlaybackStopped($itemId)") {
            if (!ensureConfigured()) return@logRequest
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

    private suspend fun <T> logRequest(operation: String, block: suspend () -> T): T {
        val startedAt = SystemClock.elapsedRealtime()
        return try {
            val result = block()
            val elapsedMs = SystemClock.elapsedRealtime() - startedAt
            Log.d(
                TAG,
                "$operation finished in ${elapsedMs}ms, " +
                    "fetched ${result.approximateSizeBytes()} bytes${result.itemCountLogText()}"
            )
            result
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            val elapsedMs = SystemClock.elapsedRealtime() - startedAt
            Log.e(TAG, "$operation failed after ${elapsedMs}ms", error)
            throw error
        }
    }

    private fun Any?.approximateSizeBytes(): Int {
        return toString().toByteArray(Charsets.UTF_8).size
    }

    private fun Any?.itemCountLogText(): String {
        val count = when (this) {
            is Collection<*> -> size
            is Map<*, *> -> size
            is Array<*> -> size
            else -> null
        }
        return count?.let { ", items $it" }.orEmpty()
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
