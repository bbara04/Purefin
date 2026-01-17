package hu.bbara.purefin.client

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.bbara.purefin.session.UserSessionRepository
import kotlinx.coroutines.flow.first
import org.jellyfin.sdk.api.client.Response
import org.jellyfin.sdk.api.client.extensions.authenticateUserByName
import org.jellyfin.sdk.api.client.extensions.itemsApi
import org.jellyfin.sdk.api.client.extensions.libraryApi
import org.jellyfin.sdk.api.client.extensions.userApi
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

    suspend fun login(url: String, username: String, password: String): Boolean {
        api.update(baseUrl = url)
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
        val serverUrl = userSessionRepository.serverUrl.first()
        val accessToken = userSessionRepository.accessToken.first()
        api.update(baseUrl = serverUrl, accessToken = accessToken)
    }

    suspend fun getContinueWatching(): List<BaseItemDto> {
        val userId = getUserId()
        if (userId == null) {
            return emptyList()
        }
        val getResumeItemsRequest = GetResumeItemsRequest(
            userId = userId,
            startIndex = 0,
            //TODO remove this limit if needed
            limit = 10
        )
        val response: Response<BaseItemDtoQueryResult> = api.itemsApi.getResumeItems(getResumeItemsRequest)
        Log.d("getContinueWatching response: {}", response.content.toString())
        return response.content.items
    }

    suspend fun getLibraries(): List<BaseItemDto> {
        val response = api.libraryApi.getMediaFolders(isHidden = false)
        Log.d("getLibraries response: {}", response.content.toString())
        return response.content.items
    }

    suspend fun getLibrary(libraryId: UUID): List<BaseItemDto> {
        val getItemsRequest = GetItemsRequest(
            userId = getUserId(),
            enableImages = false,
            parentId = libraryId,
            enableUserData = false,
            includeItemTypes = listOf(BaseItemKind.MOVIE, BaseItemKind.SERIES),
            recursive = true,
            // TODO remove this limit
            limit = 10
        )
        val response = api.itemsApi.getItems(getItemsRequest)
        Log.d("getLibrary response: {}", response.content.toString())
        return response.content.items
    }

}
