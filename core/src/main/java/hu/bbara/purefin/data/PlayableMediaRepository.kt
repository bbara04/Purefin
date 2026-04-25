package hu.bbara.purefin.data

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
