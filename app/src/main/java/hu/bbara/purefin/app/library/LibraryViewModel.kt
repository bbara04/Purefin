package hu.bbara.purefin.app.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.app.home.ui.PosterItem
import hu.bbara.purefin.client.JellyfinApiClient
import hu.bbara.purefin.data.InMemoryMediaRepository
import hu.bbara.purefin.image.JellyfinImageHelper
import hu.bbara.purefin.navigation.MovieDto
import hu.bbara.purefin.navigation.NavigationManager
import hu.bbara.purefin.navigation.Route
import hu.bbara.purefin.navigation.SeriesDto
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
    private val mediaRepository: InMemoryMediaRepository,
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

    init {
        viewModelScope.launch { mediaRepository.ensureReady() }
    }

    fun onMovieSelected(movieId: String) {
        navigationManager.navigate(Route.MovieRoute(
            MovieDto(
                id = UUID.fromString(movieId),
            )
        ))
    }

    fun onSeriesSelected(seriesId: String) {
        viewModelScope.launch {
            navigationManager.navigate(Route.SeriesRoute(
                SeriesDto(
                    id = UUID.fromString(seriesId),
                )
            ))
        }
    }

    fun onBack() {
        navigationManager.pop()
    }

    fun selectLibrary(libraryId: UUID) {
        viewModelScope.launch {
            val libraryItems = jellyfinApiClient.getLibraryContent(libraryId)
            _contents.value = libraryItems.map {
                when (it.type) {
                    BaseItemKind.MOVIE -> {
                        val movie = mediaRepository.getMovie(it.id)
                        PosterItem(
                            type = BaseItemKind.MOVIE,
                            movie = movie
                        )
                    }
                    BaseItemKind.SERIES -> {
                        val series = mediaRepository.getSeries(it.id)
                        PosterItem(
                            type = BaseItemKind.SERIES,
                            series = series
                        )
                    }
                    else -> throw UnsupportedOperationException("Unsupported item type: ${it.type}")
                }
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
