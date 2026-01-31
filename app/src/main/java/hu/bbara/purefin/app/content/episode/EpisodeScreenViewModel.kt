package hu.bbara.purefin.app.content.episode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.data.InMemoryMediaRepository
import hu.bbara.purefin.data.model.Episode
import hu.bbara.purefin.navigation.NavigationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jellyfin.sdk.model.UUID
import javax.inject.Inject

@HiltViewModel
class EpisodeScreenViewModel @Inject constructor(
    private val mediaRepository: InMemoryMediaRepository,
    private val navigationManager: NavigationManager,
): ViewModel() {

    private val _episode = MutableStateFlow<Episode?>(null)
    val episode = _episode.asStateFlow()

    init {
        viewModelScope.launch { mediaRepository.ensureReady() }
    }

    fun onBack() {
        navigationManager.pop()
    }

    fun selectEpisode(seriesId: UUID, seasonId: UUID, episodeId: UUID) {
        viewModelScope.launch {
            _episode.value = mediaRepository.getEpisode(
                seriesId = seriesId,
                seasonId = seasonId,
                episodeId = episodeId,
            )
        }
    }

}
