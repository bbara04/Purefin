package hu.bbara.purefin.core.feature.content.series

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.core.Offline
import hu.bbara.purefin.core.data.LocalMediaRepository
import hu.bbara.purefin.core.data.MediaMetadataUpdater
import hu.bbara.purefin.core.download.DownloadState
import hu.bbara.purefin.core.download.MediaDownloadController
import hu.bbara.purefin.core.navigation.EpisodeDto
import hu.bbara.purefin.core.navigation.NavigationManager
import hu.bbara.purefin.core.navigation.Route
import hu.bbara.purefin.core.navigation.SeriesDto
import hu.bbara.purefin.model.Episode
import hu.bbara.purefin.model.Series
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SeriesViewModel @Inject constructor(
    private val defaultMediaCatalogReader: LocalMediaRepository,
    @param:Offline private val offlineMediaCatalogReader: LocalMediaRepository,
    private val navigationManager: NavigationManager,
    private val mediaDownloadManager: MediaDownloadController,
    private val mediaMetadataUpdater: MediaMetadataUpdater,
) : ViewModel() {

    private val _series = MutableStateFlow<SeriesDto?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val series: StateFlow<Series?> = _series
        .flatMapLatest { series ->
            if (series == null) {
                flowOf(null)
            } else {
                mediaCatalogReader(series.offline).getSeries(series.id)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val isSmartDownloadEnabled: StateFlow<Boolean> = _series
        .flatMapLatest { series ->
            if (series == null) {
                flowOf(false)
            } else {
                mediaDownloadManager.isSmartDownloadEnabled(series.id)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _seriesDownloadState = MutableStateFlow<DownloadState>(DownloadState.NotDownloaded)
    val seriesDownloadState: StateFlow<DownloadState> = _seriesDownloadState

    private val _seasonDownloadState = MutableStateFlow<DownloadState>(DownloadState.NotDownloaded)
    val seasonDownloadState: StateFlow<DownloadState> = _seasonDownloadState

    private var seasonDownloadStateJob: Job? = null
    private var seriesDownloadStateJob: Job? = null

    fun observeSeasonDownloadState(episodes: List<Episode>) {
        seasonDownloadStateJob?.cancel()
        seasonDownloadStateJob = viewModelScope.launch {
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
        seriesDownloadStateJob?.cancel()
        seriesDownloadStateJob = viewModelScope.launch {
            val allEpisodes = series.seasons.flatMap { it.episodes }
            val hasUnloadedSeasons = series.seasons.any { it.episodes.isEmpty() && it.episodeCount > 0 }
            if (allEpisodes.isEmpty() || hasUnloadedSeasons) {
                _seriesDownloadState.value = DownloadState.NotDownloaded
                return@launch
            }
            val flows = allEpisodes.map { mediaDownloadManager.observeDownloadState(it.id.toString()) }
            combine(flows) { states -> aggregateDownloadStates(states.toList()) }
                .collect { _seriesDownloadState.value = it }
        }
    }

    fun downloadSeason(seriesId: UUID, seasonId: UUID) {
        viewModelScope.launch {
            val mediaCatalogReader = selectedMediaCatalogReader()
            mediaCatalogReader.loadSeasonEpisodes(seriesId, seasonId)
            val episodes = mediaCatalogReader.getSeries(seriesId)
                .first()
                ?.seasons
                ?.firstOrNull { it.id == seasonId }
                ?.episodes
                .orEmpty()
            mediaDownloadManager.downloadEpisodes(episodes.map { it.id })
        }
    }

    fun enableSmartDownload(seriesId: UUID) {
        viewModelScope.launch {
            mediaDownloadManager.enableSmartDownload(seriesId)
        }
    }

    fun deleteSmartDownloads(seriesId: UUID) {
        viewModelScope.launch {
            mediaDownloadManager.deleteSmartDownloads(seriesId)
            if (_series.value?.offline == true) {
                navigationManager.pop()
            }
        }
    }

    fun downloadSeries(seriesData: Series) {
        viewModelScope.launch {
            val mediaCatalogReader = selectedMediaCatalogReader()
            seriesData.seasons.forEach { season ->
                mediaCatalogReader.loadSeasonEpisodes(seriesData.id, season.id)
            }
            val allEpisodeIds = mediaCatalogReader.getSeries(seriesData.id)
                .first()
                ?.seasons
                .orEmpty()
                .flatMap { season -> season.episodes.map { it.id } }
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
        navigationManager.navigate(
            Route.EpisodeRoute(
                EpisodeDto(
                    id = episodeId,
                    seasonId = seasonId,
                    seriesId = seriesId,
                    offline = _series.value?.offline == true,
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

    fun markAsWatched(watched: Boolean) {
        val seriesId = _series.value?.id ?: return
        viewModelScope.launch {
            mediaMetadataUpdater.markAsWatched(seriesId, watched)
        }
    }

    fun markEpisodeAsWatched(episodeId: UUID, watched: Boolean) {
        viewModelScope.launch {
            mediaMetadataUpdater.markAsWatched(episodeId, watched)
        }
    }

    fun selectSeries(series: SeriesDto) {
        _series.value = series
        viewModelScope.launch {
            val mediaCatalogReader = mediaCatalogReader(series.offline)
            launch { mediaCatalogReader.loadSeries(series.id) }
            launch { mediaCatalogReader.loadSeasons(series.id) }
        }
    }

    fun selectSeason(seriesId: UUID, seasonId: UUID) {
        viewModelScope.launch {
            selectedMediaCatalogReader().loadSeasonEpisodes(seriesId, seasonId)
        }
    }

    private fun selectedMediaCatalogReader(): LocalMediaRepository {
        return mediaCatalogReader(_series.value?.offline == true)
    }

    private fun mediaCatalogReader(offline: Boolean): LocalMediaRepository {
        return if (offline) offlineMediaCatalogReader else defaultMediaCatalogReader
    }
}
