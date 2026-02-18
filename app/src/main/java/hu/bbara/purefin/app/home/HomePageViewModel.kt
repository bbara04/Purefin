package hu.bbara.purefin.app.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.app.home.ui.ContinueWatchingItem
import hu.bbara.purefin.app.home.ui.HomeNavItem
import hu.bbara.purefin.app.home.ui.LibraryItem
import hu.bbara.purefin.app.home.ui.PosterItem
import hu.bbara.purefin.data.MediaRepository
import hu.bbara.purefin.data.model.Media
import hu.bbara.purefin.domain.usecase.RefreshHomeDataUseCase
import hu.bbara.purefin.image.JellyfinImageHelper
import hu.bbara.purefin.navigation.EpisodeDto
import hu.bbara.purefin.navigation.LibraryDto
import hu.bbara.purefin.navigation.MovieDto
import hu.bbara.purefin.navigation.NavigationManager
import hu.bbara.purefin.navigation.Route
import hu.bbara.purefin.navigation.SeriesDto
import hu.bbara.purefin.session.UserSessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jellyfin.sdk.model.UUID
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.CollectionType
import org.jellyfin.sdk.model.api.ImageType
import javax.inject.Inject

@HiltViewModel
class HomePageViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val userSessionRepository: UserSessionRepository,
    private val navigationManager: NavigationManager,
    private val refreshHomeDataUseCase: RefreshHomeDataUseCase
) : ViewModel() {

    private val _url = userSessionRepository.serverUrl.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ""
    )

    val libraries = mediaRepository.libraries.map { libraries ->
        libraries.map {
            LibraryItem(
                id = it.id,
                name = it.name,
                type = it.type,
                isEmpty = when(it.type) {
                    CollectionType.MOVIES -> mediaRepository.movies.value.isEmpty()
                    CollectionType.TVSHOWS -> mediaRepository.series.value.isEmpty()
                    else -> true
                }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val isOfflineMode = userSessionRepository.isOfflineMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    val continueWatching = combine(
        mediaRepository.continueWatching,
        mediaRepository.movies,
        mediaRepository.episodes
    ) { list, moviesMap, episodesMap ->
        list.mapNotNull { media ->
            when (media) {
                is Media.MovieMedia -> moviesMap[media.movieId]?.let {
                    ContinueWatchingItem(type = BaseItemKind.MOVIE, movie = it)
                }
                is Media.EpisodeMedia -> episodesMap[media.episodeId]?.let {
                    ContinueWatchingItem(type = BaseItemKind.EPISODE, episode = it)
                }
                else -> null
            }
        }.distinctBy { it.id }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val latestLibraryContent = combine(
        mediaRepository.latestLibraryContent,
        mediaRepository.movies,
        mediaRepository.series,
        mediaRepository.episodes
    ) { libraryMap, moviesMap, seriesMap, episodesMap ->
        libraryMap.mapValues { (_, items) ->
            items.mapNotNull { media ->
                when (media) {
                    is Media.MovieMedia -> moviesMap[media.movieId]?.let {
                        PosterItem(type = BaseItemKind.MOVIE, movie = it)
                    }
                    is Media.EpisodeMedia -> episodesMap[media.episodeId]?.let {
                        PosterItem(type = BaseItemKind.EPISODE, episode = it)
                    }
                    is Media.SeriesMedia -> seriesMap[media.seriesId]?.let {
                        PosterItem(type = BaseItemKind.SERIES, series = it)
                    }
                    is Media.SeasonMedia -> seriesMap[media.seriesId]?.let {
                        PosterItem(type = BaseItemKind.SERIES, series = it)
                    }
                    else -> null
                }
            }.distinctBy { it.id }
        }
    }.stateIn(
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

    fun getImageUrl(itemId: UUID, type: ImageType): String {
        return JellyfinImageHelper.toImageUrl(
            url = _url.value,
            itemId = itemId,
            type = type
        )
    }

    fun onResumed() {
        viewModelScope.launch {
            try {
                refreshHomeDataUseCase()
            } catch (e: Exception) {
                // Refresh is best-effort; don't crash on failure
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userSessionRepository.setLoggedIn(false)
        }
    }

    fun toggleOfflineMode() {
        viewModelScope.launch {
            userSessionRepository.setOfflineMode(!isOfflineMode.value)
        }
    }

}
