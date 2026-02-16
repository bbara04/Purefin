package hu.bbara.purefin.app.content.series

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.data.MediaRepository
import hu.bbara.purefin.data.model.Series
import hu.bbara.purefin.navigation.EpisodeDto
import hu.bbara.purefin.navigation.NavigationManager
import hu.bbara.purefin.navigation.Route
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jellyfin.sdk.model.UUID
import javax.inject.Inject

@HiltViewModel
class SeriesViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val navigationManager: NavigationManager,
) : ViewModel() {

    private val _seriesId = MutableStateFlow<UUID?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val series: StateFlow<Series?> = _seriesId
        .flatMapLatest { id ->
            if (id != null) mediaRepository.observeSeriesWithContent(id) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch { mediaRepository.ensureReady() }
    }

    fun onSelectEpisode(seriesId: UUID, seasonId:UUID, episodeId: UUID) {
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

    fun selectSeries(seriesId: UUID) {
        _seriesId.value = seriesId
    }
}
