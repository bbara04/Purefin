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

    init {
        loadHomePageData()
    }

    private val _continueWatching = MutableStateFlow<List<ContinueWatchingItem>>(emptyList())
    val continueWatching = _continueWatching.asStateFlow()

    private val _libraries = MutableStateFlow<List<LibraryItem>>(emptyList())
    val libraries = _libraries.asStateFlow()

    private val _libraryContent = MutableStateFlow<Map<UUID, List<PosterItem>>>(emptyMap())
    val libraryContent = _libraryContent.asStateFlow()


    fun loadContinueWatching() {
        viewModelScope.launch {
            val continueWatching: List<BaseItemDto> = jellyfinApiClient.getContinueWatching()
            _continueWatching.value = continueWatching.map {
                if (it.type == BaseItemKind.EPISODE) {
                    ContinueWatchingItem(
                        id = it.id,
                        primaryText = it.seriesName!!,
                        secondaryText = it.name!!,
                        progress = it.userData!!.playedPercentage!!.toFloat(),
                        colors = listOf(Color.Red, Color.Green),
                    )
                } else {
                    ContinueWatchingItem(
                        id = it.id,
                        primaryText = it.name!!,
                        secondaryText = it.premiereDate!!.format(DateTimeFormatter.ofLocalizedDate(
                            FormatStyle.MEDIUM)),
                        progress = it.userData!!.playedPercentage!!.toFloat(),
                        colors = listOf(Color.Red, Color.Green)
                    )
                }
            }
        }
    }

    fun loadLibraries() {
        viewModelScope.launch {
            val libraries: List<BaseItemDto> = jellyfinApiClient.getLibraries()
            val mappedLibraries = libraries.map {
                LibraryItem(
                    name = it.name!!,
                    id = it.id
                )
            }
            _libraries.value = mappedLibraries
            mappedLibraries.forEach { library ->
                loadLibrary(library.id)
            }
        }
    }

    fun loadLibrary(libraryId: UUID) {
        if (_libraryContent.value.containsKey(libraryId)) return
        viewModelScope.launch {
            val libraryItems = jellyfinApiClient.getLibrary(libraryId)
            val posterItems = libraryItems.map {
                PosterItem(
                    id = it.id,
                    title = it.name ?: "Unknown",
                    colors = listOf(Color.Blue, Color.Cyan),
                    isLatest = false
                )
            }
            _libraryContent.update { currentMap ->
                currentMap + (libraryId to posterItems)
            }
        }
    }

    fun loadHomePageData() {
        loadContinueWatching()
        loadLibraries()
    }

    fun logout() {
        viewModelScope.launch {
            userSessionRepository.setLoggedIn(false)
        }
    }

}
