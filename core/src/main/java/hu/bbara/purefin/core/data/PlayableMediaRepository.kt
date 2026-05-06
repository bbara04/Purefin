package hu.bbara.purefin.core.data

import hu.bbara.purefin.model.PlayableMedia
import java.util.UUID

interface PlayableMediaRepository {
    suspend fun getPlayableMedia(mediaId: UUID): PlayableMedia?
    suspend fun getNextUpPlayableMedias(
        episodeId: UUID,
        existingIds: Set<UUID>,
        count: Int,
    ): List<PlayableMedia>
}
