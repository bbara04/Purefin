package hu.bbara.purefin.feature.shared.content.episode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.core.data.MediaRepository
import hu.bbara.purefin.core.data.navigation.NavigationManager
import hu.bbara.purefin.core.model.Episode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jellyfin.sdk.model.UUID
import javax.inject.Inject

@HiltViewModel
class EpisodeScreenViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val navigationManager: NavigationManager,
): ViewModel() {

    private val _episodeId = MutableStateFlow<UUID?>(null)

    val episode: StateFlow<Episode?> = combine(
        _episodeId,
        mediaRepository.episodes
    ) { id, episodesMap ->
        id?.let { episodesMap[it] }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch { mediaRepository.ensureReady() }
    }

    fun onBack() {
        navigationManager.pop()
    }

    fun selectEpisode(seriesId: UUID, seasonId: UUID, episodeId: UUID) {
        _episodeId.value = episodeId
    }

}
