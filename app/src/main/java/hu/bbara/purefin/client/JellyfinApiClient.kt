package hu.bbara.purefin.client

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.bbara.purefin.session.UserSessionRepository
import kotlinx.coroutines.flow.first
import org.jellyfin.sdk.api.client.Response
import org.jellyfin.sdk.api.client.extensions.authenticateUserByName
import org.jellyfin.sdk.api.client.extensions.itemsApi
import org.jellyfin.sdk.api.client.extensions.userApi
import org.jellyfin.sdk.api.client.extensions.userLibraryApi
import org.jellyfin.sdk.api.client.extensions.userViewsApi
import org.jellyfin.sdk.createJellyfin
import org.jellyfin.sdk.model.ClientInfo
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemDtoQueryResult
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.request.GetItemsRequest
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

    suspend fun login(url: String, username: String, password: String): Boolean {
        val trimmedUrl = url.trim()
        if (trimmedUrl.isBlank()) {
            return false
        }
        api.update(baseUrl = trimmedUrl)
        val response = api.userApi.authenticateUserByName(username = username, password = password)
        val authResult = response.content

        val token = authResult.accessToken ?: return false
        val userId = authResult.user?.id ?: return false
        userSessionRepository.setAccessToken(accessToken = token)
        userSessionRepository.setUserId(userId)
        userSessionRepository.setLoggedIn(true)
        api.update(accessToken = token)
        return true
    }

    suspend fun updateApiClient() {
        ensureConfigured()
    }

    suspend fun getContinueWatching(): List<BaseItemDto> {
        if (!ensureConfigured()) {
            return emptyList()
        }
        val userId = getUserId()
        if (userId == null) {
            return emptyList()
        }
        val getResumeItemsRequest = GetResumeItemsRequest(
            userId = userId,
            startIndex = 0,
            //TODO remove this limit if needed
//            limit = 10
        )
        val response: Response<BaseItemDtoQueryResult> = api.itemsApi.getResumeItems(getResumeItemsRequest)
        Log.d("getContinueWatching response: {}", response.content.toString())
        return response.content.items
    }

    suspend fun getLibraries(): List<BaseItemDto> {
        if (!ensureConfigured()) {
            return emptyList()
        }
        val response = api.userViewsApi.getUserViews(
            userId = getUserId(),
            includeHidden = false
        )
        Log.d("getLibraries response: {}", response.content.toString())
        return response.content.items
    }

    suspend fun getLibrary(libraryId: UUID): List<BaseItemDto> {
        if (!ensureConfigured()) {
            return emptyList()
        }
        val getItemsRequest = GetItemsRequest(
            userId = getUserId(),
            enableImages = false,
            parentId = libraryId,
            enableUserData = false,
            includeItemTypes = listOf(BaseItemKind.MOVIE, BaseItemKind.SERIES),
//            recursive = true,
            // TODO remove this limit
//            limit = 10
        )
        val response = api.itemsApi.getItems(getItemsRequest)
        Log.d("getLibrary response: {}", response.content.toString())
        return response.content.items
    }

    /**
     * Fetches the latest media items from a specified library including Movie, Episode, Season.
     *
     * @param libraryId The UUID of the library to fetch from
     * @return A list of [BaseItemDto] representing the latest media items that includes Movie, Episode, Season, or an empty list if not configured
     */
    suspend fun getLatestFromLibrary(libraryId: UUID): List<BaseItemDto> {
        if (!ensureConfigured()) {
            return emptyList()
        }
        val response = api.userLibraryApi.getLatestMedia(
            userId = getUserId(),
            parentId = libraryId,
            includeItemTypes = listOf(BaseItemKind.MOVIE, BaseItemKind.EPISODE, BaseItemKind.SEASON),
            limit = 10
        )
        Log.d("getLatestFromLibrary response: {}", response.content.toString())
        return response.content
    }

}
