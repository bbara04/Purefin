package hu.bbara.purefin.core.data

import androidx.media3.common.MediaItem
import java.util.UUID

interface PlayableMediaRepository {
    suspend fun getMediaItem(mediaId: UUID): Pair<MediaItem, Long?>?
    suspend fun getNextUpMediaItems(
        episodeId: UUID,
        existingIds: Set<String>,
        count: Int = 9,
    ): List<MediaItem>
}
