package hu.bbara.purefin.data.jellyfin

import hu.bbara.purefin.data.jellyfin.client.JellyfinApiClient
import hu.bbara.purefin.jellyfin.JellyfinMediaMetadataUpdater
import java.util.UUID
import javax.inject.Inject

class JellyfinMediaMetadataUpdaterImpl @Inject constructor(
    private val jellyfinApiClient: JellyfinApiClient
) : JellyfinMediaMetadataUpdater {

    override suspend fun markAsWatched(mediaId: UUID, watched: Boolean) {
        if (watched) {
            jellyfinApiClient.markAsWatched(mediaId)
        } else {
            jellyfinApiClient.markAsUnwatched(mediaId)
        }
    }
}