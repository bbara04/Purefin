package hu.bbara.purefin.data.local.repository

import hu.bbara.purefin.data.model.Episode
import hu.bbara.purefin.data.model.Season
import hu.bbara.purefin.data.model.Series
import java.util.UUID
import kotlinx.coroutines.flow.Flow

interface LocalMediaRepository {

    suspend fun upsertSeries(series: Series)

    suspend fun upsertSeries(seriesList: List<Series>)

    suspend fun getSeries(seriesId: UUID, includeContent: Boolean = true): Series?

    fun observeSeries(seriesId: UUID): Flow<Series?>

    fun observeAllSeries(): Flow<List<Series>>

    suspend fun deleteSeries(seriesId: UUID)

    suspend fun upsertSeason(season: Season)

    suspend fun upsertSeasons(seasons: List<Season>)

    suspend fun getSeason(seasonId: UUID, includeEpisodes: Boolean = true): Season?

    fun observeSeasons(seriesId: UUID): Flow<List<Season>>

    suspend fun deleteSeason(seasonId: UUID)

    suspend fun upsertEpisode(episode: Episode)

    suspend fun upsertEpisodes(episodes: List<Episode>)

    suspend fun getEpisode(episodeId: UUID): Episode?

    suspend fun deleteEpisode(episodeId: UUID)
}
