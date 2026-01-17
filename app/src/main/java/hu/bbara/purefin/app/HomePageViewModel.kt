package hu.bbara.purefin.app

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.app.home.ContinueWatchingItem
import hu.bbara.purefin.app.home.LibraryItem
import hu.bbara.purefin.app.home.PosterItem
import hu.bbara.purefin.client.JellyfinApiClient
import hu.bbara.purefin.session.UserSessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jellyfin.sdk.model.UUID
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject

@HiltViewModel
class HomePageViewModel @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val jellyfinApiClient: JellyfinApiClient
) : ViewModel() {

    private val _continueWatching = MutableStateFlow<List<ContinueWatchingItem>>(emptyList())
    val continueWatching = _continueWatching.asStateFlow()

    private val _libraries = MutableStateFlow<List<LibraryItem>>(emptyList())
    val libraries = _libraries.asStateFlow()

    private val _libraryItems = MutableStateFlow<Map<UUID, List<PosterItem>>>(emptyMap())
    val libraryItems = _libraryItems.asStateFlow()

    private val _latestLibraryContent = MutableStateFlow<Map<UUID, List<PosterItem>>>(emptyMap())
    val latestLibraryContent = _latestLibraryContent.asStateFlow()

    init {
        loadHomePageData()
    }

    fun loadContinueWatching() {
        viewModelScope.launch {
            val continueWatching: List<BaseItemDto> = jellyfinApiClient.getContinueWatching()
            _continueWatching.value = continueWatching.map {
                if (it.type == BaseItemKind.EPISODE) {
                    ContinueWatchingItem(
                        id = it.id,
                        primaryText = it.seriesName!!,
                        secondaryText = it.name!!,
                        progress = it.userData!!.playedPercentage!!,
                        colors = listOf(Color.Red, Color.Green),
                    )
                } else {
                    ContinueWatchingItem(
                        id = it.id,
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
                isEmpty = it.childCount!! == 0
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
            val libraryPosterItems = libraryItems.map {
                PosterItem(
                    id = it.id,
                    title = it.name ?: "Unknown"
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
        val libraryItems = _libraryItems.value[libraryId]
        viewModelScope.launch {
            val latestLibraryItems = jellyfinApiClient.getLatestFromLibrary(libraryId)
            val latestLibraryPosterItem = latestLibraryItems.mapNotNull {
                when (it.type) {
                    BaseItemKind.MOVIE -> PosterItem(
                        id = it.id,
                        title = it.name ?: "Unknown"
                    )
                    BaseItemKind.EPISODE -> PosterItem(
                        id = it.seriesId!!,
                        title = it.seriesName ?: "Unknown"
                    )
            
                    BaseItemKind.SEASON -> PosterItem(
                        id = it.seriesId!!,
                        title = it.seriesName ?: "Unknown"
                    )
                    else -> null
                }
            }
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

    fun logout() {
        viewModelScope.launch {
            userSessionRepository.setLoggedIn(false)
        }
    }

}
