package hu.bbara.purefin.app.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.app.home.ui.ContinueWatchingItem
import hu.bbara.purefin.app.home.ui.HomeNavItem
import hu.bbara.purefin.app.home.ui.LibraryItem
import hu.bbara.purefin.app.home.ui.PosterItem
import hu.bbara.purefin.client.JellyfinApiClient
import hu.bbara.purefin.data.InMemoryMediaRepository
import hu.bbara.purefin.data.model.Media
import hu.bbara.purefin.image.JellyfinImageHelper
import hu.bbara.purefin.navigation.EpisodeDto
import hu.bbara.purefin.navigation.LibraryDto
import hu.bbara.purefin.navigation.MovieDto
import hu.bbara.purefin.navigation.NavigationManager
import hu.bbara.purefin.navigation.Route
import hu.bbara.purefin.navigation.SeriesDto
import hu.bbara.purefin.session.UserSessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jellyfin.sdk.model.UUID
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.ImageType
import javax.inject.Inject

@HiltViewModel
class HomePageViewModel @Inject constructor(
    private val mediaRepository: InMemoryMediaRepository,
    private val userSessionRepository: UserSessionRepository,
    private val navigationManager: NavigationManager,
    private val jellyfinApiClient: JellyfinApiClient
) : ViewModel() {

    private val _url = userSessionRepository.serverUrl.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ""
    )

    private val _libraries = MutableStateFlow<List<LibraryItem>>(emptyList())
    val libraries = _libraries.asStateFlow()

    val continueWatching = mediaRepository.continueWatching.map { list ->
        list.map {
            when ( it ) {
                is Media.MovieMedia -> {
                    val movie = mediaRepository.getMovie(it.movieId)
                    ContinueWatchingItem(
                        type = BaseItemKind.MOVIE,
                        movie = movie
                    )
                }
                is Media.EpisodeMedia -> {
                    val episode = mediaRepository.getEpisode(
                        seriesId = it.seriesId,
                        episodeId = it.episodeId
                    )
                    ContinueWatchingItem(
                        type = BaseItemKind.EPISODE,
                        episode = episode
                    )
                }
                else -> throw UnsupportedOperationException("Unsupported item type: $it")
            }
        }
    }.distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _latestLibraryContent = MutableStateFlow<Map<UUID, List<PosterItem>>>(emptyMap())
    val latestLibraryContent = _latestLibraryContent.asStateFlow()

    init {
        viewModelScope.launch { mediaRepository.ensureReady() }
        loadHomePageData()
    }

    fun onLibrarySelected(library : HomeNavItem) {
        viewModelScope.launch {
            navigationManager.navigate(Route.LibraryRoute(library = LibraryDto(id = library.id, name = library.label)))
        }
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

    fun onEpisodeSelected(seriesId: UUID, seasonId: UUID, episodeId: UUID) {
        viewModelScope.launch {
            navigationManager.navigate(Route.EpisodeRoute(
                EpisodeDto(
                    id = episodeId,
                    seasonId = seasonId,
                    seriesId = seriesId
                )
            ))
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
//            mediaRepository.loadContinueWatching()
        }
    }

    fun loadLibraries() {
        viewModelScope.launch {
//            mediaRepository.loadLibraries()
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
        viewModelScope.launch {
            val latestLibraryItems = jellyfinApiClient.getLatestFromLibrary(libraryId)
            val latestLibraryPosterItem = latestLibraryItems.map {
                when (it.type) {
                    BaseItemKind.MOVIE -> {
                        val movie = mediaRepository.getMovie(it.id)
                        PosterItem(
                            type = BaseItemKind.MOVIE,
                            movie = movie
                        )
                    }
                    BaseItemKind.EPISODE -> {
                        val episode = mediaRepository.getEpisode(
                            it.seriesId!!,
                            it.parentId!!,
                            it.id
                        )
                        PosterItem(
                            type = BaseItemKind.EPISODE,
                            episode = episode
                        )
                    }
                    BaseItemKind.SEASON -> {
                        val series = mediaRepository.getSeries(
                            seriesId = it.seriesId!!
                        )
                        PosterItem(
                            type = BaseItemKind.SERIES,
                            series = series
                        )
                    }
                    BaseItemKind.SERIES -> {
                        val series = mediaRepository.getSeries(
                            seriesId = it.id
                        )
                        PosterItem(
                            type = BaseItemKind.SERIES,
                            series = series
                        )
                    }
                    else -> throw UnsupportedOperationException("Unsupported item type: ${it.type}")
                }
            }.distinctBy { it.id }
            _latestLibraryContent.update { currentMap ->
                currentMap + (libraryId to latestLibraryPosterItem)
            }
        }
    }

    fun loadHomePageData() {
        loadContinueWatching()
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
