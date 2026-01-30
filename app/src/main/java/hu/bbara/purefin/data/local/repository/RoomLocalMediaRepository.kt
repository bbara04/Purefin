package hu.bbara.purefin.data.local.repository

import hu.bbara.purefin.app.content.episode.CastMember
import hu.bbara.purefin.data.local.dao.EpisodeDao
import hu.bbara.purefin.data.local.dao.SeasonDao
import hu.bbara.purefin.data.local.dao.SeriesDao
import hu.bbara.purefin.data.local.entity.EpisodeCastMemberEntity
import hu.bbara.purefin.data.local.entity.EpisodeEntity
import hu.bbara.purefin.data.local.entity.SeasonEntity
import hu.bbara.purefin.data.local.entity.SeriesEntity
import hu.bbara.purefin.data.local.relations.SeasonWithEpisodes
import hu.bbara.purefin.data.local.relations.SeriesWithSeasonsAndEpisodes
import hu.bbara.purefin.data.model.Episode
import hu.bbara.purefin.data.model.Season
import hu.bbara.purefin.data.model.Series
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class RoomLocalMediaRepository @Inject constructor(
    private val seriesDao: SeriesDao,
    private val seasonDao: SeasonDao,
    private val episodeDao: EpisodeDao
) : LocalMediaRepository {

    override suspend fun upsertSeries(series: Series) {
        withContext(Dispatchers.IO) {
            seriesDao.upsertSeries(series.toEntity())
            upsertSeasonsInternal(series.seasons)
        }
    }

    override suspend fun upsertSeries(seriesList: List<Series>) {
        if (seriesList.isEmpty()) return
        withContext(Dispatchers.IO) {
            seriesDao.upsertSeries(seriesList.map { it.toEntity() })
            val seasons = seriesList.flatMap { it.seasons }
            upsertSeasonsInternal(seasons)
        }
    }

    override suspend fun getSeries(seriesId: UUID, includeContent: Boolean): Series? {
        return withContext(Dispatchers.IO) {
            if (includeContent) {
                seriesDao.getSeriesWithContent(seriesId)?.toDomain()
            } else {
                seriesDao.getSeriesById(seriesId)?.toDomain(emptyList())
            }
        }
    }

    override fun observeSeries(seriesId: UUID): Flow<Series?> {
        return seriesDao.observeSeriesWithContent(seriesId).map { relation ->
            relation?.toDomain()
        }
    }

    override fun observeAllSeries(): Flow<List<Series>> {
        return seriesDao.observeAllSeriesWithContent().map { relations ->
            relations.map { it.toDomain() }
        }
    }

    override suspend fun deleteSeries(seriesId: UUID) {
        withContext(Dispatchers.IO) {
            seriesDao.deleteSeries(seriesId)
        }
    }

    override suspend fun upsertSeason(season: Season) {
        withContext(Dispatchers.IO) {
            upsertSeasonsInternal(listOf(season))
        }
    }

    override suspend fun upsertSeasons(seasons: List<Season>) {
        if (seasons.isEmpty()) return
        withContext(Dispatchers.IO) {
            upsertSeasonsInternal(seasons)
        }
    }

    override suspend fun getSeason(seasonId: UUID, includeEpisodes: Boolean): Season? {
        return withContext(Dispatchers.IO) {
            if (includeEpisodes) {
                seasonDao.getSeasonWithEpisodes(seasonId)?.toDomain()
            } else {
                seasonDao.getSeasonById(seasonId)?.toDomain(emptyList())
            }
        }
    }

    override fun observeSeasons(seriesId: UUID): Flow<List<Season>> {
        return seasonDao.observeSeasonsWithEpisodes(seriesId).map { relations ->
            relations.map { it.toDomain() }
        }
    }

    override suspend fun deleteSeason(seasonId: UUID) {
        withContext(Dispatchers.IO) {
            seasonDao.deleteSeason(seasonId)
        }
    }

    override suspend fun upsertEpisode(episode: Episode) {
        withContext(Dispatchers.IO) {
            episodeDao.upsertEpisode(episode.toEntity())
        }
    }

    override suspend fun upsertEpisodes(episodes: List<Episode>) {
        if (episodes.isEmpty()) return
        withContext(Dispatchers.IO) {
            episodeDao.upsertEpisodes(episodes.map { it.toEntity() })
        }
    }

    override suspend fun getEpisode(episodeId: UUID): Episode? {
        return withContext(Dispatchers.IO) {
            episodeDao.getEpisodeById(episodeId)?.toDomain()
        }
    }

    override suspend fun deleteEpisode(episodeId: UUID) {
        withContext(Dispatchers.IO) {
            episodeDao.deleteEpisode(episodeId)
        }
    }

    private suspend fun upsertSeasonsInternal(seasons: List<Season>) {
        if (seasons.isEmpty()) return
        seasonDao.upsertSeasons(seasons.map { it.toEntity() })
        val episodes = seasons.flatMap { it.episodes }
        if (episodes.isNotEmpty()) {
            episodeDao.upsertEpisodes(episodes.map { it.toEntity() })
        }
    }
}

private fun SeriesEntity.toDomain(seasons: List<Season>): Series = Series(
    id = id,
    name = name,
    synopsis = synopsis,
    year = year,
    heroImageUrl = heroImageUrl,
    seasonCount = seasonCount,
    seasons = seasons,
    //TODO check if it is needed
    cast = emptyList()
)

private fun SeasonEntity.toDomain(episodes: List<Episode>): Season = Season(
    id = id,
    seriesId = seriesId,
    name = name,
    index = index,
    episodeCount = episodeCount,
    episodes = episodes
)

private fun EpisodeEntity.toDomain(): Episode = Episode(
    id = id,
    seriesId = seriesId,
    seasonId = seasonId,
    title = title,
    index = index,
    releaseDate = releaseDate,
    rating = rating,
    runtime = runtime,
    format = format,
    synopsis = synopsis,
    heroImageUrl = heroImageUrl,
    progress = 13.0,
    watched = false,
    cast = emptyList()
)

private fun SeriesWithSeasonsAndEpisodes.toDomain(): Series =
    series.toDomain(seasons.map { it.toDomain() })

private fun SeasonWithEpisodes.toDomain(): Season =
    season.toDomain(episodes.map { it.toDomain() })

private fun Series.toEntity(): SeriesEntity = SeriesEntity(
    id = id,
    name = name,
    synopsis = synopsis,
    year = year,
    heroImageUrl = heroImageUrl,
    seasonCount = seasonCount
)

private fun Season.toEntity(): SeasonEntity = SeasonEntity(
    id = id,
    seriesId = seriesId,
    name = name,
    index = index,
    episodeCount = episodeCount
)

private fun Episode.toEntity(): EpisodeEntity = EpisodeEntity(
    id = id,
    seriesId = seriesId,
    seasonId = seasonId,
    title = title,
    index = index,
    releaseDate = releaseDate,
    rating = rating,
    runtime = runtime,
    format = format,
    synopsis = synopsis,
    heroImageUrl = heroImageUrl,
    cast = emptyList()
)

private fun EpisodeCastMemberEntity.toDomain(): CastMember = CastMember(
    name = name,
    role = role,
    imageUrl = imageUrl
)

private fun CastMember.toEntity(): EpisodeCastMemberEntity = EpisodeCastMemberEntity(
    name = name,
    role = role,
    imageUrl = imageUrl
)
