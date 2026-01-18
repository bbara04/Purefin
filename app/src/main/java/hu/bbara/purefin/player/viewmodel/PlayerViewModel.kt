package hu.bbara.purefin.player.viewmodel

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.client.JellyfinApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.jellyfin.sdk.model.UUID
import org.jellyfin.sdk.model.api.MediaSourceInfo
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    val player: Player,
    val jellyfinApiClient: JellyfinApiClient
) : ViewModel() {

    val mediaId: String? = savedStateHandle["MEDIA_ID"]
    private val videoUris = savedStateHandle.getStateFlow("videoUris", emptyList<Uri>())
    private val _contentUri = MutableStateFlow<Uri?>(null)

    init {
        player.prepare()
        loadMedia()
    }

    fun loadMedia() {
        viewModelScope.launch {
            val mediaSources: List<MediaSourceInfo> = jellyfinApiClient.getMediaSources(UUID.fromString(mediaId!!))
            val contentUriString =
                jellyfinApiClient.getMediaPlaybackInfo(mediaId = UUID.fromString(mediaId), mediaSourceId = mediaSources.first().id)
            contentUriString?.toUri()?.let {
                _contentUri.value = it
                playVideo(it)
            }
        }
    }

    fun addVideoUri(contentUri: Uri) {
        savedStateHandle["videoUris"] = videoUris.value + contentUri
        player.addMediaItem(MediaItem.fromUri(contentUri))
    }

    fun playVideo(uri: Uri) {
        player.setMediaItem(
            MediaItem.fromUri(uri)
        )
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}