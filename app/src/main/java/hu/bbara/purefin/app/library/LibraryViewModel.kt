package hu.bbara.purefin.app.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.app.home.ui.PosterItem
import hu.bbara.purefin.client.JellyfinApiClient
import hu.bbara.purefin.navigation.NavigationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jellyfin.sdk.model.UUID
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val jellyfinApiClient: JellyfinApiClient,
    private val navigationManager: NavigationManager
) : ViewModel() {
    private val _contents = MutableStateFlow<List<PosterItem>>(emptyList())
    val contents = _contents.asStateFlow()

    fun selectLibrary(libraryId: UUID) {
        viewModelScope.launch {
            val libraryItems = jellyfinApiClient.getLibrary(libraryId)
            _contents.value = libraryItems.map {
                PosterItem(
                    id = it.id,
                    title = it.name ?: "Unknown",
                    type = it.type
                )
            }
        }
    }
}