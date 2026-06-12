package hu.bbara.purefin.core.feature.content.episode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.core.Offline
import hu.bbara.purefin.core.data.LocalMediaRepository
import hu.bbara.purefin.core.download.DownloadState
import hu.bbara.purefin.core.download.MediaDownloadController
import hu.bbara.purefin.core.data.MediaMetadataUpdater
import hu.bbara.purefin.core.navigation.EpisodeDto
import hu.bbara.purefin.core.navigation.NavigationManager
import hu.bbara.purefin.core.navigation.Route
import hu.bbara.purefin.core.navigation.SeriesDto
import hu.bbara.purefin.model.Episode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EpisodeScreenViewModel @Inject constructor(
    private val defaultMediaCatalogReader: LocalMediaRepository,
    @param:Offline private val offlineMediaCatalogReader: LocalMediaRepository,
    private val navigationManager: NavigationManager,
    private val mediaDownloadManager: MediaDownloadController,
    private val mediaMetadataUpdater: MediaMetadataUpdater,
): ViewModel() {

    private val _episode = MutableStateFlow<EpisodeDto?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val episode: StateFlow<Episode?> = _episode
        .flatMapLatest { episode ->
            if (episode == null) {
                flowOf(null)
            } else {
                mediaCatalogReader(episode.offline).getEpisode(episode.id)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val seriesTitle: StateFlow<String?> = _episode
        .flatMapLatest { episode ->
            if (episode == null) {
                flowOf(null)
            } else {
                mediaCatalogReader(episode.offline).getSeries(episode.seriesId).map { it?.name }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.NotDownloaded)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    fun onBack() {
        navigationManager.pop()
    }

    fun onSeriesClick() {
        val episode = _episode.value ?: return
        navigationManager.navigate(
            Route.SeriesRoute(
                SeriesDto(id = episode.seriesId, offline = episode.offline)
            )
        )
    }

    fun selectEpisode(episode: EpisodeDto) {
        _episode.value = episode
        viewModelScope.launch {
            mediaDownloadManager.observeDownloadState(episode.id.toString()).collect {
                _downloadState.value = it
            }
        }
    }

    fun markAsWatched(watched: Boolean) {
        val episodeId = _episode.value?.id ?: return
        viewModelScope.launch {
            mediaMetadataUpdater.markAsWatched(episodeId, watched)
        }
    }

    fun onDownloadClick() {
        val episodeId = _episode.value?.id ?: return
        viewModelScope.launch {
            when (_downloadState.value) {
                is DownloadState.NotDownloaded, is DownloadState.Failed -> {
                    mediaDownloadManager.downloadEpisode(episodeId)
                }
                is DownloadState.Downloading -> {
                    mediaDownloadManager.cancelEpisodeDownload(episodeId)
                }
                is DownloadState.Downloaded -> {
                    mediaDownloadManager.cancelEpisodeDownload(episodeId)
                    if (_episode.value?.offline == true) {
                        navigationManager.pop()
                    }
                }
            }
        }
    }

    private fun mediaCatalogReader(offline: Boolean): LocalMediaRepository {
        return if (offline) offlineMediaCatalogReader else defaultMediaCatalogReader
    }
}
