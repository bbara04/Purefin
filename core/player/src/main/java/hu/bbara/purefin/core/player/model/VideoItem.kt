package hu.bbara.purefin.core.player.model

import android.net.Uri
import androidx.media3.common.MediaItem

data class VideoItem(
    val title: String,
    val mediaItem: MediaItem,
    val uri: Uri
)
