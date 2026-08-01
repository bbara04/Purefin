package hu.bbara.purefin.data.offline.room.entity

import androidx.room.Entity
import java.util.UUID

/**
 * Stores metadata for an external subtitle file that has been downloaded to
 * local storage for offline playback. Rows are keyed by the owning media id
 * (movie or episode) and the subtitle stream index from the server.
 */
@Entity(
    tableName = "subtitles",
    primaryKeys = ["mediaId", "index"],
)
data class SubtitleEntity(
    val mediaId: UUID,
    val index: Int,
    val language: String?,
    val label: String?,
    val mimeType: String,
    val forced: Boolean,
    val defaultTrack: Boolean,
    val localFilePath: String,
)