package hu.bbara.purefin.feature.browse.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.data.HomeRepository
import hu.bbara.purefin.data.LocalMediaRepository
import hu.bbara.purefin.data.UserSessionRepository
import hu.bbara.purefin.download.MediaDownloadController
import hu.bbara.purefin.jellyfin.JellyfinMediaMetadataUpdater
import hu.bbara.purefin.model.LibraryKind
import hu.bbara.purefin.model.Media
import hu.bbara.purefin.navigation.EpisodeDto
import hu.bbara.purefin.navigation.LibraryDto
import hu.bbara.purefin.navigation.MovieDto
import hu.bbara.purefin.navigation.NavigationManager
import hu.bbara.purefin.navigation.Route
import hu.bbara.purefin.navigation.SeriesDto
import hu.bbara.purefin.ui.model.EpisodeUiModel
import hu.bbara.purefin.ui.model.LibraryUiModel
import hu.bbara.purefin.ui.model.MediaUiModel
import hu.bbara.purefin.ui.model.MovieUiModel
import hu.bbara.purefin.ui.model.SeriesUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val localMediaRepository: LocalMediaRepository,
    private val userSessionRepository: UserSessionRepository,
    private val jellyfinMediaMetadataUpdater: JellyfinMediaMetadataUpdater,
    private val navigationManager: NavigationManager,
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
            LibraryUiModel(
                id = it.id,
                name = it.name,
                type = it.type,
                posterUrl = it.posterUrl,
                size = it.size,
                isEmpty = when (it.type) {
                    LibraryKind.MOVIES -> localMediaRepository.movies.value.isEmpty()
                    LibraryKind.SERIES -> localMediaRepository.series.value.isEmpty()
                }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val suggestions = combine(
        homeRepository.suggestions,
        localMediaRepository.movies,
        localMediaRepository.series,
        localMediaRepository.episodes
    ) { list, moviesMap, seriesMap, episodesMap ->
        list.mapNotNull { media ->
            when (media) {
                is Media.MovieMedia -> moviesMap[media.movieId]?.let {
                    MovieUiModel(movie = it)
                }
                is Media.SeriesMedia -> seriesMap[media.seriesId]?.let {
                    SeriesUiModel(series = it)
                }
                is Media.EpisodeMedia -> episodesMap[media.episodeId]?.let {
                    EpisodeUiModel(episode = it)
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
        localMediaRepository.movies,
        localMediaRepository.episodes
    ) { list, moviesMap, episodesMap ->
        list.mapNotNull { media ->
            when (media) {
                is Media.MovieMedia -> moviesMap[media.movieId]?.let {
                    MovieUiModel(movie = it)
                }
                is Media.EpisodeMedia -> episodesMap[media.episodeId]?.let {
                    EpisodeUiModel(episode = it)
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
        localMediaRepository.episodes
    ) { list, episodesMap ->
        list.mapNotNull { media ->
            when (media) {
                is Media.EpisodeMedia -> episodesMap[media.episodeId]?.let {
                    EpisodeUiModel(episode = it)
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
        localMediaRepository.movies,
        localMediaRepository.series,
        localMediaRepository.episodes
    ) { libraryMap, moviesMap, seriesMap, episodesMap ->
        libraryMap.mapValues { (_, items) ->
            items.mapNotNull { media ->
                when (media) {
                    is Media.MovieMedia -> moviesMap[media.movieId]?.let {
                        MovieUiModel(movie = it)
                    }
                    is Media.EpisodeMedia -> episodesMap[media.episodeId]?.let {
                        EpisodeUiModel(episode = it)
                    }
                    is Media.SeriesMedia -> seriesMap[media.seriesId]?.let {
                        SeriesUiModel(series = it)
                    }
                    is Media.SeasonMedia -> seriesMap[media.seriesId]?.let {
                        SeriesUiModel(series = it)
                    }
                }
            }.distinctBy { it.id }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyMap()
    )

    fun markAsWatched(mediaUiModel: MediaUiModel, watched: Boolean) {
        viewModelScope.launch {
            jellyfinMediaMetadataUpdater.markAsWatched(mediaUiModel.id, watched)
        }
    }

    fun onLibrarySelected(id: UUID, name: String) {
        viewModelScope.launch {
            navigationManager.navigate(Route.LibraryRoute(library = LibraryDto(id = id, name = name)))
        }
    }

    fun onMediaSelected(mediaUiModel : MediaUiModel) {
        when (mediaUiModel) {
            is MovieUiModel -> onMovieSelected(mediaUiModel.id)
            is SeriesUiModel -> onSeriesSelected(mediaUiModel.id)
            is EpisodeUiModel -> onEpisodeSelected(
                seriesId = mediaUiModel.seriesId,
                seasonId = mediaUiModel.seasonId,
                episodeId = mediaUiModel.id
            )
        }
    }

    private fun onMovieSelected(movieId: UUID) {
        navigationManager.navigate(Route.MovieRoute(
            MovieDto(
                id = movieId,
            )
        ))
    }

    private fun onSeriesSelected(seriesId: UUID) {
        viewModelScope.launch {
            navigationManager.navigate(Route.SeriesRoute(
                SeriesDto(
                    id = seriesId,
                )
            ))
        }
    }

    private fun onEpisodeSelected(seriesId: UUID, seasonId: UUID, episodeId: UUID) {
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
                homeRepository.refreshHomeData()
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
                homeRepository.refreshHomeData()
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

    fun openSearch() {
        navigationManager.navigate(Route.HomeSearchRoute)
    }

    fun openSettings() {
        navigationManager.navigate(Route.SettingsRoute)
    }

    fun logout() {
        viewModelScope.launch {
            userSessionRepository.setLoggedIn(false)
        }
    }
}
