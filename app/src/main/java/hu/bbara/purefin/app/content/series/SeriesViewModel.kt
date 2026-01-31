package hu.bbara.purefin.app.content.series

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.data.InMemoryMediaRepository
import hu.bbara.purefin.data.model.Series
import hu.bbara.purefin.navigation.EpisodeDto
import hu.bbara.purefin.navigation.NavigationManager
import hu.bbara.purefin.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jellyfin.sdk.model.UUID
import javax.inject.Inject

@HiltViewModel
class SeriesViewModel @Inject constructor(
    private val mediaRepository: InMemoryMediaRepository,
    private val navigationManager: NavigationManager,
) : ViewModel() {

    private val _series = MutableStateFlow<Series?>(null)
    val series = _series.asStateFlow()

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
        viewModelScope.launch {
            val series = mediaRepository.getSeriesWithContent(
                seriesId = seriesId
            )
            _series.value = series
        }
    }
}
