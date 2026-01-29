package hu.bbara.purefin.data

import hu.bbara.purefin.data.model.Episode
import hu.bbara.purefin.data.model.Season
import hu.bbara.purefin.data.model.Series
import java.util.UUID

interface MediaRepository {

    fun getSeries(seriesId: UUID, includeContent: Boolean) : Series

    fun getSeason(seriesId: UUID, seasonId: UUID, includeContent: Boolean) : Season

    fun getSeasons(seriesId: UUID, includeContent: Boolean) : List<Season>

    fun getEpisode(seriesId: UUID, seasonId: UUID, episodeId: UUID) : Episode

    fun getEpisodes(seriesId: UUID, seasonId: UUID) : List<Episode>

    fun getEpisodes(seriesId: UUID) : List<Episode>

}