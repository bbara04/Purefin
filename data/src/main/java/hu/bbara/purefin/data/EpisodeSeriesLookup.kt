package hu.bbara.purefin.data

import java.util.UUID

interface EpisodeSeriesLookup {
    suspend fun preferenceKeyFor(mediaId: UUID): String
}
