package hu.bbara.purefin.app.home

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.app.home.ui.ContinueWatchingItem
import hu.bbara.purefin.app.home.ui.HomeNavItem
import hu.bbara.purefin.app.home.ui.LibraryItem
import hu.bbara.purefin.app.home.ui.PosterItem
import hu.bbara.purefin.client.JellyfinApiClient
import hu.bbara.purefin.image.JellyfinImageHelper
import hu.bbara.purefin.navigation.ItemDto
import hu.bbara.purefin.navigation.LibraryDto
import hu.bbara.purefin.navigation.NavigationManager
import hu.bbara.purefin.navigation.Route
import hu.bbara.purefin.session.UserSessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jellyfin.sdk.model.UUID
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.ImageType
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject

@HiltViewModel
class HomePageViewModel @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val navigationManager: NavigationManager,
    private val jellyfinApiClient: JellyfinApiClient
) : ViewModel() {

    private val _url = MutableStateFlow("")

    private val _continueWatching = MutableStateFlow<List<ContinueWatchingItem>>(emptyList())
    val continueWatching = _continueWatching.asStateFlow()

    private val _libraries = MutableStateFlow<List<LibraryItem>>(emptyList())
    val libraries = _libraries.asStateFlow()

    private val _libraryItems = MutableStateFlow<Map<UUID, List<PosterItem>>>(emptyMap())
    val libraryItems = _libraryItems.asStateFlow()

    private val _latestLibraryContent = MutableStateFlow<Map<UUID, List<PosterItem>>>(emptyMap())
    val latestLibraryContent = _latestLibraryContent.asStateFlow()

    init {
        viewModelScope.launch {
            userSessionRepository.serverUrl.collect {
                _url.value = it
            }
        }
        loadHomePageData()
    }

    fun onLibrarySelected(library : HomeNavItem) {
        viewModelScope.launch {
            navigationManager.navigate(Route.Library(library = LibraryDto(id = library.id, name = library.label)))
        }
    }

    fun onMovieSelected(movieId: String) {
        navigationManager.navigate(Route.Movie(ItemDto(id = UUID.fromString(movieId), type = BaseItemKind.MOVIE)))
    }

    fun onSeriesSelected(seriesId: String) {
        viewModelScope.launch {
            navigationManager.navigate(Route.Series(ItemDto(id = UUID.fromString(seriesId), type = BaseItemKind.SERIES)))
        }
    }

    fun onEpisodeSelected(episodeId: String) {
        viewModelScope.launch {
            navigationManager.navigate(Route.Episode(ItemDto(id = UUID.fromString(episodeId), type = BaseItemKind.EPISODE)))
        }
    }

    fun onBack() {
        navigationManager.pop()
    }


    fun onGoHome() {
        navigationManager.replaceAll(Route.Home)
    }

    fun loadContinueWatching() {
        viewModelScope.launch {
            val continueWatching: List<BaseItemDto> = jellyfinApiClient.getContinueWatching()
            _continueWatching.value = continueWatching.map {
                if (it.type == BaseItemKind.EPISODE) {
                    ContinueWatchingItem(
                        id = it.id,
                        type = BaseItemKind.EPISODE,
                        primaryText = it.seriesName!!,
                        secondaryText = it.name!!,
                        progress = it.userData!!.playedPercentage!!,
                        colors = listOf(Color.Red, Color.Green),
                    )
                } else {
                    ContinueWatchingItem(
                        id = it.id,
                        type = BaseItemKind.MOVIE,
                        primaryText = it.name!!,
                        secondaryText = it.premiereDate!!.format(DateTimeFormatter.ofLocalizedDate(
                            FormatStyle.MEDIUM)),
                        progress = it.userData!!.playedPercentage!!,
                        colors = listOf(Color.Red, Color.Green)
                    )
                }
            }
        }
    }

    fun loadLibraries() {
        viewModelScope.launch {
            loadLibrariesInternal()
        }
    }

    private suspend fun loadLibrariesInternal() {
        val libraries: List<BaseItemDto> = jellyfinApiClient.getLibraries()
        val mappedLibraries = libraries.map {
            LibraryItem(
                name = it.name!!,
                id = it.id,
                isEmpty = it.childCount!! == 0,
                type = it.collectionType!!
            )
        }
        _libraries.value = mappedLibraries
    }

    fun loadAllLibraryItems() {
        viewModelScope.launch {
            if (_libraries.value.isEmpty()) {
                loadLibrariesInternal()
            }
            _libraries.value.forEach { library ->
                loadLibraryItems(library.id)
            }
        }
    }

    private fun loadLibraryItems(libraryId: UUID) {
        viewModelScope.launch {
            val libraryItems: List<BaseItemDto> = jellyfinApiClient.getLibrary(libraryId)
            // It return only Movie or Series
            val libraryPosterItems = libraryItems.map {
                PosterItem(
                    id = it.id,
                    title = it.name ?: "Unknown",
                    type = it.type
                )
            }
            _libraryItems.update { currentMap ->
                currentMap + (libraryId to libraryPosterItems)
            }
        }
    }

    fun loadAllShownLibraryItems() {
        viewModelScope.launch {
            if (_libraries.value.isEmpty()) {
                loadLibrariesInternal()
            }
            _libraries.value.forEach { library ->
                loadLatestLibraryItems(library.id)
            }
        }
    }

    fun loadLatestLibraryItems(libraryId: UUID) {
        if (_libraryItems.value.containsKey(libraryId)) return
        viewModelScope.launch {
            val latestLibraryItems = jellyfinApiClient.getLatestFromLibrary(libraryId)
            val latestLibraryPosterItem = latestLibraryItems.mapNotNull {
                when (it.type) {
                    BaseItemKind.MOVIE -> PosterItem(
                        id = it.id,
                        title = it.name ?: "Unknown",
                        type = BaseItemKind.MOVIE
                    )
                    BaseItemKind.EPISODE -> PosterItem(
                        id = it.id,
                        title = it.seriesName ?: "Unknown",
                        type = BaseItemKind.EPISODE,
                        parentId = it.seriesId!!
                    )
                    BaseItemKind.SEASON -> PosterItem(
                        id = it.seriesId!!,
                        title = it.seriesName ?: "Unknown",
                        type = BaseItemKind.SERIES,
                        parentId = it.seriesId
                    )
                    else -> null
                }
            }.distinctBy { it.id }
            _latestLibraryContent.update { currentMap ->
                currentMap + (libraryId to latestLibraryPosterItem)
            }
        }
    }

    fun loadHomePageData() {
        loadContinueWatching()
        loadLibraries()
        loadAllLibraryItems()
        loadAllShownLibraryItems()
    }

    fun getImageUrl(itemId: UUID, type: ImageType): String {
        return JellyfinImageHelper.toImageUrl(
            url = _url.value,
            itemId = itemId,
            type = type
        )
    }

    fun logout() {
        viewModelScope.launch {
            userSessionRepository.setLoggedIn(false)
        }
    }

}
