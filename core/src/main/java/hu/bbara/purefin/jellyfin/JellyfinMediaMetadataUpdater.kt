package hu.bbara.purefin.jellyfin

import java.util.UUID

interface JellyfinMediaMetadataUpdater {
    suspend fun markAsWatched(mediaId: UUID, watched: Boolean)
}