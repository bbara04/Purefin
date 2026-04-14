package hu.bbara.purefin.core.data

import java.util.UUID

interface EpisodeSeriesLookup {
    suspend fun preferenceKeyFor(mediaId: UUID): String
}
