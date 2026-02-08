package hu.bbara.purefin.app.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.app.home.ui.PosterItem
import hu.bbara.purefin.client.JellyfinApiClient
import hu.bbara.purefin.data.InMemoryMediaRepository
import hu.bbara.purefin.data.model.Media
import hu.bbara.purefin.image.JellyfinImageHelper
import hu.bbara.purefin.navigation.MovieDto
import hu.bbara.purefin.navigation.NavigationManager
import hu.bbara.purefin.navigation.Route
import hu.bbara.purefin.navigation.SeriesDto
import hu.bbara.purefin.session.UserSessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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

    private val _libraryItems = MutableStateFlow<List<Media>>(emptyList())

    val contents: StateFlow<List<PosterItem>> = combine(
        _libraryItems,
        mediaRepository.movies,
        mediaRepository.series
    ) { items, moviesMap, seriesMap ->
        items.mapNotNull { media ->
            when (media) {
                is Media.MovieMedia -> moviesMap[media.movieId]?.let {
                    PosterItem(type = BaseItemKind.MOVIE, movie = it)
                }
                is Media.SeriesMedia -> seriesMap[media.seriesId]?.let {
                    PosterItem(type = BaseItemKind.SERIES, series = it)
                }
                else -> null
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch { mediaRepository.ensureReady() }
    }

    fun onMovieSelected(movieId: UUID) {
        navigationManager.navigate(Route.MovieRoute(
            MovieDto(
                id = movieId,
            )
        ))
    }

    fun onSeriesSelected(seriesId: UUID) {
        viewModelScope.launch {
            navigationManager.navigate(Route.SeriesRoute(
                SeriesDto(
                    id = seriesId,
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
            _libraryItems.value = libraryItems.map {
                when (it.type) {
                    BaseItemKind.MOVIE -> Media.MovieMedia(movieId = it.id)
                    BaseItemKind.SERIES -> Media.SeriesMedia(seriesId = it.id)
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
