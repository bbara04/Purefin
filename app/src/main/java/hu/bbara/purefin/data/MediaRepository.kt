package hu.bbara.purefin.data

import hu.bbara.purefin.data.model.Episode
import hu.bbara.purefin.data.model.Season
import hu.bbara.purefin.data.model.Series
import java.util.UUID

interface MediaRepository {

    suspend fun getSeries(seriesId: UUID, includeContent: Boolean) : Series

    suspend fun getSeason(seriesId: UUID, seasonId: UUID, includeContent: Boolean) : Season

    suspend fun getSeasons(seriesId: UUID, includeContent: Boolean) : List<Season>

    suspend fun getEpisode(seriesId: UUID, seasonId: UUID, episodeId: UUID) : Episode

    suspend fun getEpisodes(seriesId: UUID, seasonId: UUID) : List<Episode>

    suspend fun getEpisodes(seriesId: UUID) : List<Episode>

}