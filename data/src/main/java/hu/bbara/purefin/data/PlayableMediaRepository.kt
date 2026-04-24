package hu.bbara.purefin.data

import androidx.media3.common.MediaItem
import hu.bbara.purefin.model.MediaSegment
import java.util.UUID

interface PlayableMediaRepository {
    suspend fun getMediaItem(mediaId: UUID): Pair<MediaItem, Long?>?
    suspend fun getMediaSegments(mediaId: UUID): List<MediaSegment>
    suspend fun getNextUpMediaItems(
        episodeId: UUID,
        existingIds: Set<String>,
        count: Int = 9,
    ): List<MediaItem>
}
