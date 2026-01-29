package hu.bbara.purefin.app.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.app.home.ui.PosterItem
import hu.bbara.purefin.client.JellyfinApiClient
import hu.bbara.purefin.image.JellyfinImageHelper
import hu.bbara.purefin.navigation.ItemDto
import hu.bbara.purefin.navigation.NavigationManager
import hu.bbara.purefin.navigation.Route
import hu.bbara.purefin.session.UserSessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jellyfin.sdk.model.UUID
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.ImageType
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val jellyfinApiClient: JellyfinApiClient,
    private val navigationManager: NavigationManager
) : ViewModel() {

    private val _url = userSessionRepository.serverUrl.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ""
    )
    private val _contents = MutableStateFlow<List<PosterItem>>(emptyList())
    val contents = _contents.asStateFlow()

    fun onMovieSelected(movieId: String) {
        navigationManager.navigate(Route.MovieRoute(ItemDto(id = UUID.fromString(movieId), type = BaseItemKind.MOVIE)))
    }

    fun onSeriesSelected(seriesId: String) {
        viewModelScope.launch {
            navigationManager.navigate(Route.SeriesRoute(ItemDto(id = UUID.fromString(seriesId), type = BaseItemKind.SERIES)))
        }
    }

    fun onBack() {
        navigationManager.pop()
    }

    fun selectLibrary(libraryId: UUID) {
        viewModelScope.launch {
            val libraryItems = jellyfinApiClient.getLibrary(libraryId)
            _contents.value = libraryItems.map {
                PosterItem(
                    id = it.id,
                    title = it.name ?: "Unknown",
                    type = it.type,
                    imageUrl = getImageUrl(it.id, ImageType.PRIMARY)
                )
            }
        }
    }

    fun getImageUrl(itemId: UUID, type: ImageType): String {
        return JellyfinImageHelper.toImageUrl(
            url = _url.value,
            itemId = itemId,
            type = type
        )
    }
}