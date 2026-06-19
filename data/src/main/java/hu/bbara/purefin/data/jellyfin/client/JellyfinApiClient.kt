package hu.bbara.purefin.data.jellyfin.client

import android.content.Context
import android.os.SystemClock
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.bbara.purefin.core.data.JellyfinServerCandidate
import hu.bbara.purefin.core.data.PlaybackMethod
import hu.bbara.purefin.core.data.PlaybackReportContext
import hu.bbara.purefin.core.data.QuickConnectSession
import hu.bbara.purefin.core.data.UserSessionRepository
import hu.bbara.purefin.core.jellyfin.JellyfinSdkClient
import hu.bbara.purefin.data.jellyfin.etag.NotModifiedException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.jellyfin.sdk.api.client.Response
import org.jellyfin.sdk.api.client.extensions.authenticateUserByName
import org.jellyfin.sdk.api.client.extensions.authenticateWithQuickConnect
import org.jellyfin.sdk.api.client.extensions.clientLogApi
import org.jellyfin.sdk.api.client.extensions.genresApi
import org.jellyfin.sdk.api.client.extensions.itemsApi
import org.jellyfin.sdk.api.client.extensions.mediaInfoApi
import org.jellyfin.sdk.api.client.extensions.mediaSegmentsApi
import org.jellyfin.sdk.api.client.extensions.playStateApi
import org.jellyfin.sdk.api.client.extensions.quickConnectApi
import org.jellyfin.sdk.api.client.extensions.suggestionsApi
import org.jellyfin.sdk.api.client.extensions.systemApi
import org.jellyfin.sdk.api.client.extensions.tvShowsApi
import org.jellyfin.sdk.api.client.extensions.userApi
import org.jellyfin.sdk.api.client.extensions.userLibraryApi
import org.jellyfin.sdk.api.client.extensions.userViewsApi
import org.jellyfin.sdk.api.client.extensions.videosApi
import org.jellyfin.sdk.api.okhttp.OkHttpFactory
import org.jellyfin.sdk.createJellyfin
import org.jellyfin.sdk.discovery.RecommendedServerInfo
import org.jellyfin.sdk.discovery.RecommendedServerInfoScore
import org.jellyfin.sdk.model.ClientInfo
import org.jellyfin.sdk.model.api.AuthenticationResult
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemDtoQueryResult
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.CollectionType
import org.jellyfin.sdk.model.api.DeviceProfile
import org.jellyfin.sdk.model.api.ItemFields
import org.jellyfin.sdk.model.api.MediaSegmentDto
import org.jellyfin.sdk.model.api.MediaType
import org.jellyfin.sdk.model.api.PlayMethod
import org.jellyfin.sdk.model.api.PlaybackInfoDto
import org.jellyfin.sdk.model.api.PlaybackInfoResponse
import org.jellyfin.sdk.model.api.PlaybackOrder
import org.jellyfin.sdk.model.api.PlaybackProgressInfo
import org.jellyfin.sdk.model.api.PlaybackStartInfo
import org.jellyfin.sdk.model.api.PlaybackStopInfo
import org.jellyfin.sdk.model.api.RepeatMode
import org.jellyfin.sdk.model.api.UpdateUserItemDataDto
import org.jellyfin.sdk.model.api.request.GetItemsRequest
import org.jellyfin.sdk.model.api.request.GetNextUpRequest
import org.jellyfin.sdk.model.api.request.GetResumeItemsRequest
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JellyfinApiClient @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val userSessionRepository: UserSessionRepository,
    @JellyfinSdkClient private val okHttpFactory: OkHttpFactory,
) {
    private val jellyfin = createJellyfin {
        context = applicationContext
        clientInfo = ClientInfo(name = "Purefin", version = "0.0.1")
        apiClientFactory = okHttpFactory
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

    fun discoverServers(): Flow<JellyfinServerCandidate> =
        jellyfin.discovery.discoverLocalServers()
            .map { JellyfinServerCandidate(name = it.name, address = it.address) }
            .flowOn(Dispatchers.IO)

    suspend fun findServer(input: String): JellyfinServerCandidate? = withContext(Dispatchers.IO) {
        logRequest("findServer") {
            val address = input.trim()
            if (address.isBlank()) {
                return@logRequest null
            }

            val recommendedServers = jellyfin.discovery.getRecommendedServers(
                input = address,
                minimumScore = RecommendedServerInfoScore.OK
            )
            val server = recommendedServers.bestServer() ?: return@logRequest null
            JellyfinServerCandidate(
                name = server.systemInfo.getOrNull()?.serverName,
                address = server.address
            )
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

    suspend fun isQuickConnectEnabled(url: String): Boolean = withContext(Dispatchers.IO) {
        logRequest("isQuickConnectEnabled") {
            val trimmedUrl = url.trim()
            if (trimmedUrl.isBlank()) {
                return@logRequest false
            }
            api.update(baseUrl = trimmedUrl)
            api.quickConnectApi.getQuickConnectEnabled().content
        }
    }

    suspend fun initiateQuickConnect(url: String): QuickConnectSession? = withContext(Dispatchers.IO) {
        logRequest("initiateQuickConnect") {
            val trimmedUrl = url.trim()
            if (trimmedUrl.isBlank()) {
                return@logRequest null
            }
            api.update(baseUrl = trimmedUrl)
            api.quickConnectApi.initiateQuickConnect().content.toQuickConnectSession()
        }
    }

    suspend fun getQuickConnectState(url: String, secret: String): QuickConnectSession? = withContext(Dispatchers.IO) {
        logRequest("getQuickConnectState") {
            val trimmedUrl = url.trim()
            if (trimmedUrl.isBlank() || secret.isBlank()) {
                return@logRequest null
            }
            api.update(baseUrl = trimmedUrl)
            api.quickConnectApi.getQuickConnectState(secret).content.toQuickConnectSession()
        }
    }

    suspend fun authenticateWithQuickConnect(url: String, secret: String): AuthenticationResult? = withContext(Dispatchers.IO) {
        logRequest("authenticateWithQuickConnect") {
            val trimmedUrl = url.trim()
            if (trimmedUrl.isBlank() || secret.isBlank()) {
                return@logRequest null
            }
            api.update(baseUrl = trimmedUrl)
            api.userApi.authenticateWithQuickConnect(secret).content
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
            response.content.items
        }
    }

    suspend fun searchByGenre(genres: Set<String>): List<BaseItemDto> = withContext(Dispatchers.IO) {
        logRequest("searchByGenre") {
            if (!ensureConfigured()) {
                return@logRequest emptyList()
            }
            val response = api.itemsApi.getItems(
                userId = getUserId(),
                includeItemTypes = listOf(BaseItemKind.MOVIE, BaseItemKind.SERIES),
                genres = genres,
                recursive = true
            )
            response.content.items
        }
    }

    suspend fun getLibraries(): List<BaseItemDto>? = withContext(Dispatchers.IO) {
        logRequest("getLibraries") {
            if (!ensureConfigured()) {
                return@logRequest emptyList<BaseItemDto>()
            }
            try {
                val response = api.userViewsApi.getUserViews(
                    userId = getUserId(),
                    presetViews = listOf(CollectionType.MOVIES, CollectionType.TVSHOWS),
                    includeHidden = false,
                )
                response.content.items
            } catch (e: NotModifiedException) {
                null
            }
        }
    }

    suspend fun probeServer(): Boolean = withContext(Dispatchers.IO) {
        logRequest("probeServer") {
            if (!ensureConfigured()) {
                return@logRequest false
            }
            api.systemApi.getPingSystem()
            true
        }
    }

    suspend fun getLibraryContent(libraryId: UUID): List<BaseItemDto>? = withContext(Dispatchers.IO) {
        logRequest("getLibraryContent") {
            if (!ensureConfigured()) {
                return@logRequest emptyList<BaseItemDto>()
            }
            try {
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
                response.content.items
            } catch (e: NotModifiedException) {
                null
            }
        }
    }

    suspend fun getSuggestions(): List<BaseItemDto>? = withContext(Dispatchers.IO) {
        logRequest("getSuggestions") {
            if (!ensureConfigured()) {
                return@logRequest emptyList<BaseItemDto>()
            }
            val userId = getUserId() ?: return@logRequest emptyList<BaseItemDto>()
            try {
                val response = api.suggestionsApi.getSuggestions(
                    userId = userId,
                    mediaType = listOf(MediaType.VIDEO),
                    type = listOf(BaseItemKind.MOVIE, BaseItemKind.SERIES),
                    limit = 8,
                    enableTotalRecordCount = true,
                )
                response.content.items
            } catch (e: NotModifiedException) {
                null
            }
        }
    }

    suspend fun getContinueWatching(): List<BaseItemDto>? = withContext(Dispatchers.IO) {
        logRequest("getContinueWatching") {
            if (!ensureConfigured()) {
                return@logRequest emptyList<BaseItemDto>()
            }
            val userId = getUserId() ?: return@logRequest emptyList<BaseItemDto>()
            try {
                val getResumeItemsRequest = GetResumeItemsRequest(
                    userId = userId,
                    fields = defaultItemFields + ItemFields.OVERVIEW,
                    includeItemTypes = listOf(BaseItemKind.MOVIE, BaseItemKind.EPISODE),
                    enableUserData = true,
                    startIndex = 0,
                )
                val response: Response<BaseItemDtoQueryResult> = api.itemsApi.getResumeItems(getResumeItemsRequest)
                response.content.items
            } catch (e: NotModifiedException) {
                null
            }
        }
    }

    suspend fun getNextUpEpisodes(): List<BaseItemDto>? = withContext(Dispatchers.IO) {
        logRequest("getNextUpEpisodes") {
            if (!ensureConfigured()) {
                return@logRequest emptyList<BaseItemDto>()
            }
            try {
                val getNextUpRequest = GetNextUpRequest(
                    userId = getUserId(),
                    fields = defaultItemFields + ItemFields.OVERVIEW,
                    enableResumable = false,
                )
                val result = api.tvShowsApi.getNextUp(getNextUpRequest)
                result.content.items
            } catch (e: NotModifiedException) {
                null
            }
        }
    }

    suspend fun getLatestFromLibrary(libraryId: UUID): List<BaseItemDto>? = withContext(Dispatchers.IO) {
        logRequest("getLatestFromLibrary") {
            if (!ensureConfigured()) {
                return@logRequest emptyList<BaseItemDto>()
            }
            try {
                val response = api.userLibraryApi.getLatestMedia(
                    userId = getUserId(),
                    parentId = libraryId,
                    fields = defaultItemFields + ItemFields.OVERVIEW,
                    includeItemTypes = listOf(BaseItemKind.MOVIE, BaseItemKind.EPISODE, BaseItemKind.SEASON),
                    limit = 15,
                )
                response.content
            } catch (e: NotModifiedException) {
                null
            }
        }
    }

    suspend fun getItemInfo(mediaId: UUID): BaseItemDto? = withContext(Dispatchers.IO) {
        logRequest("getItemInfo") {
            if (!ensureConfigured()) {
                return@logRequest null
            }
            val result = api.userLibraryApi.getItem(itemId = mediaId, userId = getUserId())
            result.content
        }
    }

    suspend fun getSeasons(seriesId: UUID): List<BaseItemDto> = withContext(Dispatchers.IO) {
        logRequest("getSeasons") {
            if (!ensureConfigured()) {
                return@logRequest emptyList()
            }
            val result = api.tvShowsApi.getSeasons(
                userId = getUserId(),
                seriesId = seriesId,
                fields = defaultItemFields,
                enableUserData = true,
            )
            result.content.items
        }
    }

    suspend fun getEpisodesInSeason(seriesId: UUID, seasonId: UUID): List<BaseItemDto> = withContext(Dispatchers.IO) {
        logRequest("getEpisodesInSeason") {
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
            result.content.items
        }
    }

    suspend fun getNextEpisodes(episodeId: UUID, count: Int): List<BaseItemDto> = withContext(Dispatchers.IO) {
        logRequest("getNextEpisodes") {
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
            nextUpEpisodes
        }
    }

    suspend fun getGenres(id: UUID? = null) : List<BaseItemDto> = withContext(Dispatchers.IO) {
        logRequest("getGenres") {
            if (!ensureConfigured()) {
                return@logRequest emptyList()
            }
            val result = api.genresApi.getGenres(
                userId = getUserId(),
                parentId = id,
            )
            result.content.items
        }
    }

    suspend fun updatePlaybackPosition(
        mediaId: UUID,
        playbackPositionTicks: Long,
        runtimeTicks: Long,
    ) = withContext(Dispatchers.IO) {
        if (runtimeTicks <= 0L) return@withContext
        val normalizedPlaybackPositionTicks = playbackPositionTicks.coerceIn(0L, runtimeTicks)
        val isPastThreshold = normalizedPlaybackPositionTicks.toDouble() / runtimeTicks.toDouble() >= 0.8
        logRequest("updatePlaybackPosition") {
            if (!ensureConfigured()) {
                return@logRequest
            }
            val result = api.itemsApi.updateItemUserData(
                itemId = mediaId,
                userId = getUserId(),
                data = UpdateUserItemDataDto(
                    playbackPositionTicks = if (isPastThreshold) runtimeTicks else normalizedPlaybackPositionTicks,
                    played = isPastThreshold,
                )
            )
            result.content
        }
    }

    suspend fun markAsWatched(mediaId: UUID) = withContext(Dispatchers.IO) {
        logRequest("markAsWatched") {
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
        logRequest("markAsUnwatched") {
            if (!ensureConfigured()) {
                return@logRequest
            }
            api.playStateApi.markUnplayedItem(
                itemId = mediaId,
                userId = getUserId(),
            )
        }
    }

    suspend fun getMediaSegments(mediaId: UUID) : List<MediaSegmentDto> = withContext(Dispatchers.IO) {
        logRequest("getMediaSegments") {
            if (!ensureConfigured()) {
                return@logRequest emptyList()
            }
            val result = api.mediaSegmentsApi.getItemSegments(
                itemId = mediaId,
                //includeSegmentTypes = listOf(MediaSegmentType.INTRO)
            )
            result.content.items
        }
    }

    suspend fun getPlaybackInfo(
        mediaId: UUID,
        deviceProfile: DeviceProfile,
    ): PlaybackInfoResponse? = withContext(Dispatchers.IO) {
        logRequest("getPlaybackInfo") {
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
        itemId: UUID
    ): String = try {
        api.videosApi.getVideoStreamUrl(
            itemId = itemId,
            static = true,
        )
    } catch (error: Exception) {
        Timber.tag(TAG).e(error, "getVideoStreamUrl")
        throw error
    }

    suspend fun uploadLogFile(data: String): String? = withContext(Dispatchers.IO) {
        logRequest("uploadLogFile") {
            if (!ensureConfigured()) {
                return@logRequest null
            }
            api.clientLogApi.logFile(data).content.fileName
        }
    }

    suspend fun reportPlaybackStart(
        itemId: UUID,
        positionTicks: Long = 0L,
        reportContext: PlaybackReportContext,
    ) = withContext(Dispatchers.IO) {
        logRequest("reportPlaybackStart") {
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
        logRequest("reportPlaybackProgress") {
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
        logRequest("reportPlaybackStopped") {
            if (!ensureConfigured()) return@logRequest
            api.playStateApi.reportPlaybackStopped(
                PlaybackStopInfo(
                    itemId = itemId,
                    positionTicks = positionTicks,
                    mediaSourceId = reportContext.mediaSourceId,
                    playSessionId = reportContext.playSessionId,
                    failed = false,
                ),
            )
        }
    }

    private suspend fun <T> logRequest(functionName: String, block: suspend () -> T): T {
        val startedAt = SystemClock.elapsedRealtime()
        return try {
            val result = block()
            val elapsedMs = SystemClock.elapsedRealtime() - startedAt
            val sizeLog = when (result) {
                is List<*> -> {
                    val items = result.size
                    if (items == 0) {
                        " (empty)"
                    } else {
                        val bytes = result.toString().toByteArray().size
                        val sizeStr = when {
                            bytes < 1024 -> "$bytes B"
                            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
                            else -> "%.1f MB".format(bytes.toFloat() / (1024 * 1024))
                        }
                        " ($items items, $sizeStr)"
                    }
                }
                is Unit -> ""
                else -> {
                    val bytes = result.toString().toByteArray().size
                    when {
                        bytes < 1024 -> " ($bytes B)"
                        bytes < 1024 * 1024 -> " (${bytes / 1024} KB)"
                        else -> " (%.1f MB)".format(bytes.toFloat() / (1024 * 1024))
                    }
                }
            }
            Timber.tag(TAG).d("$functionName took ${elapsedMs}ms$sizeLog")
            result
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            val elapsedMs = SystemClock.elapsedRealtime() - startedAt
            Timber.tag(TAG).e(error, "$functionName failed after ${elapsedMs}ms")
            throw error
        }
    }

    private fun PlaybackMethod.toJellyfinPlayMethod(): PlayMethod = when (this) {
        PlaybackMethod.DIRECT_PLAY -> PlayMethod.DIRECT_PLAY
        PlaybackMethod.DIRECT_STREAM -> PlayMethod.DIRECT_STREAM
        PlaybackMethod.TRANSCODE -> PlayMethod.TRANSCODE
    }

    private fun Collection<RecommendedServerInfo>.bestServer(): RecommendedServerInfo? =
        firstOrNull { it.score == RecommendedServerInfoScore.GREAT }
            ?: firstOrNull { it.score == RecommendedServerInfoScore.GOOD }
            ?: firstOrNull { it.score == RecommendedServerInfoScore.OK }

    private fun org.jellyfin.sdk.model.api.QuickConnectResult.toQuickConnectSession(): QuickConnectSession =
        QuickConnectSession(
            code = code,
            secret = secret,
            authenticated = authenticated
        )

    companion object {
        private const val TAG = "JellyfinApiClient"
    }
}
