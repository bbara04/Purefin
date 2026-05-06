package hu.bbara.purefin.core.jellyfin

import java.util.UUID

interface JellyfinMediaMetadataUpdater {
    suspend fun markAsWatched(mediaId: UUID, watched: Boolean)
}