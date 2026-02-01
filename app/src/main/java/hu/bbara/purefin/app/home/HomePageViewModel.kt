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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    init {
        viewModelScope.launch {
            loadLibraries()
        }
    }

    val continueWatching = mediaRepository.continueWatching
        .mapLatest { list ->
            withContext(Dispatchers.IO) {
                list.map { media ->
                    when (media) {
                        is Media.MovieMedia -> {
                            val movie = mediaRepository.getMovie(media.movieId)
                            ContinueWatchingItem(
                                type = BaseItemKind.MOVIE,
                                movie = movie
                            )
                        }

                        is Media.EpisodeMedia -> {
                            val episode = mediaRepository.getEpisode(
                                seriesId = media.seriesId,
                                episodeId = media.episodeId
                            )
                            ContinueWatchingItem(
                                type = BaseItemKind.EPISODE,
                                episode = episode
                            )
                        }

                        else -> throw UnsupportedOperationException("Unsupported item type: $media")
                    }
                }.distinctBy { it.id }
            }
        }
        .distinctUntilChanged()
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val latestLibraryContent = mediaRepository.latestLibraryContent
        .mapLatest { libraryMap ->
            withContext(Dispatchers.IO) {
                libraryMap.mapValues { (_, items) ->
                    items.map { media ->
                        when (media) {
                            is Media.MovieMedia -> {
                                val movie = mediaRepository.getMovie(media.movieId)
                                PosterItem(
                                    type = BaseItemKind.MOVIE,
                                    movie = movie
                                )
                            }

                            is Media.EpisodeMedia -> {
                                val episode = mediaRepository.getEpisode(
                                    seriesId = media.seriesId,
                                    episodeId = media.episodeId
                                )
                                PosterItem(
                                    type = BaseItemKind.EPISODE,
                                    episode = episode
                                )
                            }

                            is Media.SeriesMedia -> {
                                val series = mediaRepository.getSeries(media.id)
                                PosterItem(
                                    type = BaseItemKind.SERIES,
                                    series = series
                                )
                            }

                            is Media.SeasonMedia -> {
                                val series = mediaRepository.getSeries(media.seriesId)
                                PosterItem(
                                    type = BaseItemKind.SERIES,
                                    series = series
                                )
                            }

                            else -> throw UnsupportedOperationException("Unsupported item type: $media")
                        }
                    }.distinctBy { it.id }
                }
            }
        }
        .distinctUntilChanged()
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap()
        )

    init {
        viewModelScope.launch { mediaRepository.ensureReady() }
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

    private suspend fun loadLibraries() {
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
