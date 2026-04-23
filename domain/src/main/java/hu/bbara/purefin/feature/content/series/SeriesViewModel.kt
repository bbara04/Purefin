package hu.bbara.purefin.feature.content.series

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.core.data.MediaCatalogReader
import hu.bbara.purefin.core.download.DownloadState
import hu.bbara.purefin.core.download.MediaDownloadController
import hu.bbara.purefin.core.model.Episode
import hu.bbara.purefin.core.model.Series
import hu.bbara.purefin.core.navigation.EpisodeDto
import hu.bbara.purefin.core.navigation.NavigationManager
import hu.bbara.purefin.core.navigation.Route
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SeriesViewModel @Inject constructor(
    private val mediaCatalogReader: MediaCatalogReader,
    private val navigationManager: NavigationManager,
    private val mediaDownloadManager: MediaDownloadController,
) : ViewModel() {

    private val _seriesId = MutableStateFlow<UUID?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val series: StateFlow<Series?> = _seriesId
        .flatMapLatest { id ->
            if (id != null) mediaCatalogReader.observeSeriesWithContent(id) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _seriesDownloadState = MutableStateFlow<DownloadState>(DownloadState.NotDownloaded)
    val seriesDownloadState: StateFlow<DownloadState> = _seriesDownloadState

    private val _seasonDownloadState = MutableStateFlow<DownloadState>(DownloadState.NotDownloaded)
    val seasonDownloadState: StateFlow<DownloadState> = _seasonDownloadState

    fun observeSeasonDownloadState(episodes: List<Episode>) {
        viewModelScope.launch {
            if (episodes.isEmpty()) {
                _seasonDownloadState.value = DownloadState.NotDownloaded
                return@launch
            }
            val flows = episodes.map { mediaDownloadManager.observeDownloadState(it.id.toString()) }
            combine(flows) { states -> aggregateDownloadStates(states.toList()) }
                .collect { _seasonDownloadState.value = it }
        }
    }

    fun observeSeriesDownloadState(series: Series) {
        viewModelScope.launch {
            val allEpisodes = series.seasons.flatMap { it.episodes }
            if (allEpisodes.isEmpty()) {
                _seriesDownloadState.value = DownloadState.NotDownloaded
                return@launch
            }
            val flows = allEpisodes.map { mediaDownloadManager.observeDownloadState(it.id.toString()) }
            combine(flows) { states -> aggregateDownloadStates(states.toList()) }
                .collect { _seriesDownloadState.value = it }
        }
    }

    fun downloadSeason(episodes: List<Episode>) {
        viewModelScope.launch {
            mediaDownloadManager.downloadEpisodes(episodes.map { it.id })
        }
    }

    fun enableSmartDownload(seriesId: UUID) {
        viewModelScope.launch {
            mediaDownloadManager.enableSmartDownload(seriesId)
        }
    }

    fun downloadSeries(series: Series) {
        viewModelScope.launch {
            val allEpisodeIds = series.seasons.flatMap { season ->
                season.episodes.map { it.id }
            }
            mediaDownloadManager.downloadEpisodes(allEpisodeIds)
        }
    }

    private fun aggregateDownloadStates(states: List<DownloadState>): DownloadState {
        if (states.isEmpty()) return DownloadState.NotDownloaded
        if (states.all { it is DownloadState.Downloaded }) return DownloadState.Downloaded
        if (states.any { it is DownloadState.Downloading }) {
            val avg = states.filterIsInstance<DownloadState.Downloading>()
                .map { it.progressPercent }
                .average()
                .toFloat()
            return DownloadState.Downloading(avg)
        }
        return DownloadState.NotDownloaded
    }

    fun onSelectEpisode(seriesId: UUID, seasonId: UUID, episodeId: UUID) {
        navigationManager.navigate(Route.EpisodeRoute(
            EpisodeDto(
                id = episodeId,
                seasonId = seasonId,
                seriesId = seriesId
            )
        ))
    }

    fun onPlayEpisode(episodeId: UUID) {
        navigationManager.navigate(Route.PlayerRoute(mediaId = episodeId.toString()))
    }

    fun onBack() {
        navigationManager.pop()
    }

    fun onGoHome() {
        navigationManager.replaceAll(Route.Home)
    }

    fun selectSeries(seriesId: UUID) {
        _seriesId.value = seriesId
    }
}
