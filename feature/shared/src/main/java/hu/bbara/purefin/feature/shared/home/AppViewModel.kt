package hu.bbara.purefin.feature.shared.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.core.data.HomeRepository
import hu.bbara.purefin.core.data.MediaCatalogReader
import hu.bbara.purefin.core.data.domain.usecase.RefreshHomeDataUseCase
import hu.bbara.purefin.core.data.download.MediaDownloadController
import hu.bbara.purefin.core.data.session.UserSessionRepository
import hu.bbara.purefin.core.model.LibraryKind
import hu.bbara.purefin.core.model.Media
import hu.bbara.purefin.core.model.MediaKind
import hu.bbara.purefin.feature.shared.navigation.EpisodeDto
import hu.bbara.purefin.feature.shared.navigation.LibraryDto
import hu.bbara.purefin.feature.shared.navigation.MovieDto
import hu.bbara.purefin.feature.shared.navigation.NavigationManager
import hu.bbara.purefin.feature.shared.navigation.Route
import hu.bbara.purefin.feature.shared.navigation.SeriesDto
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val mediaCatalogReader: MediaCatalogReader,
    private val userSessionRepository: UserSessionRepository,
    private val navigationManager: NavigationManager,
    private val refreshHomeDataUseCase: RefreshHomeDataUseCase,
    private val mediaDownloadManager: MediaDownloadController,
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val serverUrl: StateFlow<String> = userSessionRepository.serverUrl
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            ""
        )

    val libraries = homeRepository.libraries.map { libraries ->
        libraries.map {
            LibraryItem(
                id = it.id,
                name = it.name,
                type = it.type,
                posterUrl = it.posterUrl,
                isEmpty = when(it.type) {
                    LibraryKind.MOVIES -> mediaCatalogReader.movies.value.isEmpty()
                    LibraryKind.SERIES -> mediaCatalogReader.series.value.isEmpty()
                }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val suggestions = combine(
        homeRepository.suggestions,
        mediaCatalogReader.movies,
        mediaCatalogReader.series,
        mediaCatalogReader.episodes
    ) { list, moviesMap, seriesMap, episodesMap ->
        list.mapNotNull { media ->
            when (media) {
                is Media.MovieMedia -> moviesMap[media.movieId]?.let {
                    SuggestedMovie(movie = it)
                }
                is Media.SeriesMedia -> seriesMap[media.seriesId]?.let {
                    SuggestedSeries(series = it)
                }
                is Media.EpisodeMedia -> episodesMap[media.episodeId]?.let {
                    SuggestedEpisode(episode = it)
                }
                else -> null
            }
        }.distinctBy { it.id }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val continueWatching = combine(
        homeRepository.continueWatching,
        mediaCatalogReader.movies,
        mediaCatalogReader.episodes
    ) { list, moviesMap, episodesMap ->
        list.mapNotNull { media ->
            when (media) {
                is Media.MovieMedia -> moviesMap[media.movieId]?.let {
                    ContinueWatchingItem(type = MediaKind.MOVIE, movie = it)
                }
                is Media.EpisodeMedia -> episodesMap[media.episodeId]?.let {
                    ContinueWatchingItem(type = MediaKind.EPISODE, episode = it)
                }
                else -> null
            }
        }.distinctBy { it.id }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val nextUp = combine(
        homeRepository.nextUp,
        mediaCatalogReader.episodes
    ) { list, episodesMap ->
        list.mapNotNull { media ->
            when (media) {
                is Media.EpisodeMedia -> episodesMap[media.episodeId]?.let {
                    NextUpItem(episode = it)
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
        homeRepository.latestLibraryContent,
        mediaCatalogReader.movies,
        mediaCatalogReader.series,
        mediaCatalogReader.episodes
    ) { libraryMap, moviesMap, seriesMap, episodesMap ->
        libraryMap.mapValues { (_, items) ->
            items.mapNotNull { media ->
                when (media) {
                    is Media.MovieMedia -> moviesMap[media.movieId]?.let {
                        PosterItem(type = MediaKind.MOVIE, movie = it)
                    }
                    is Media.EpisodeMedia -> episodesMap[media.episodeId]?.let {
                        PosterItem(type = MediaKind.EPISODE, episode = it)
                    }
                    is Media.SeriesMedia -> seriesMap[media.seriesId]?.let {
                        PosterItem(type = MediaKind.SERIES, series = it)
                    }
                    is Media.SeasonMedia -> seriesMap[media.seriesId]?.let {
                        PosterItem(type = MediaKind.SERIES, series = it)
                    }
                }
            }.distinctBy { it.id }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyMap()
    )

    fun onLibrarySelected(id: UUID, name: String) {
        viewModelScope.launch {
            navigationManager.navigate(Route.LibraryRoute(library = LibraryDto(id = id, name = name)))
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

    fun onResumed() {
        viewModelScope.launch {
            try {
                refreshHomeDataUseCase()
            } catch (e: Exception) {
                // Refresh is best-effort; don't crash on failure
            }
        }
        viewModelScope.launch {
            try {
                mediaDownloadManager.syncSmartDownloads()
            } catch (_: Exception) { }
        }
    }

    fun onRefresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                refreshHomeDataUseCase()
            } catch (e: Exception) {
                // Refresh is best-effort; don't crash on failure
            } finally {
                _isRefreshing.value = false
            }
        }
        viewModelScope.launch {
            try {
                mediaDownloadManager.syncSmartDownloads()
            } catch (_: Exception) { }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userSessionRepository.setLoggedIn(false)
        }
    }
}
