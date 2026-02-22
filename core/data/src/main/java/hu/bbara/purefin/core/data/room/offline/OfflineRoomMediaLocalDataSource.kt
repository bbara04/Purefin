package hu.bbara.purefin.core.data.room.offline

import androidx.room.withTransaction
import hu.bbara.purefin.core.data.room.dao.EpisodeDao
import hu.bbara.purefin.core.data.room.dao.MovieDao
import hu.bbara.purefin.core.data.room.dao.SeasonDao
import hu.bbara.purefin.core.data.room.dao.SeriesDao
import hu.bbara.purefin.core.data.room.entity.EpisodeEntity
import hu.bbara.purefin.core.data.room.entity.MovieEntity
import hu.bbara.purefin.core.data.room.entity.SeasonEntity
import hu.bbara.purefin.core.data.room.entity.SeriesEntity
import hu.bbara.purefin.core.model.Episode
import hu.bbara.purefin.core.model.Movie
import hu.bbara.purefin.core.model.Season
import hu.bbara.purefin.core.model.Series
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Singleton

@Singleton
class OfflineRoomMediaLocalDataSource(
    private val database: OfflineMediaDatabase,
    private val movieDao: MovieDao,
    private val seriesDao: SeriesDao,
    private val seasonDao: SeasonDao,
    private val episodeDao: EpisodeDao,
) {

    val moviesFlow: Flow<Map<UUID, Movie>> = movieDao.observeAll()
        .map { entities -> entities.associate { it.id to it.toDomain() } }

    val seriesFlow: Flow<Map<UUID, Series>> = seriesDao.observeAll()
        .map { entities ->
            entities.associate { it.id to it.toDomain(seasons = emptyList()) }
        }

    val episodesFlow: Flow<Map<UUID, Episode>> = episodeDao.observeAll()
        .map { entities -> entities.associate { it.id to it.toDomain() } }

    // Full content Flow for series detail screen (scoped to one series)
    fun observeSeriesWithContent(seriesId: UUID): Flow<Series?> =
        seriesDao.observeWithContent(seriesId).map { relation ->
            relation?.let {
                it.series.toDomain(
                    seasons = it.seasons.map { swe ->
                        swe.season.toDomain(
                            episodes = swe.episodes.map { ep -> ep.toDomain() }
                        )
                    }                )
            }
        }

    suspend fun saveMovies(movies: List<Movie>) {
        database.withTransaction {
            movieDao.upsertAll(movies.map { it.toEntity() })
        }
    }

    suspend fun saveSeries(seriesList: List<Series>) {
        database.withTransaction {
            seriesDao.upsertAll(seriesList.map { it.toEntity() })
        }
    }

    suspend fun saveSeriesContent(series: Series) {
        database.withTransaction {
            // First ensure the series exists before adding seasons/episodes/cast
            seriesDao.upsert(series.toEntity())

            episodeDao.deleteBySeriesId(series.id)
            seasonDao.deleteBySeriesId(series.id)

            series.seasons.forEach { season ->
                seasonDao.upsert(season.toEntity())
                season.episodes.forEach { episode ->
                    episodeDao.upsert(episode.toEntity())
                }
            }
        }
    }

    suspend fun saveEpisode(episode: Episode) {
        database.withTransaction {
            seriesDao.getById(episode.seriesId)
                ?: throw RuntimeException("Cannot add episode without series. Episode: $episode")

            episodeDao.upsert(episode.toEntity())
        }
    }

    suspend fun getMovies(): List<Movie> {
        val movies = movieDao.getAll()
        return movies.map { entity ->
            entity.toDomain()
        }
    }

    suspend fun getMovie(id: UUID): Movie? {
        val entity = movieDao.getById(id) ?: return null
        return entity.toDomain()
    }

    suspend fun getSeries(): List<Series> {
        return seriesDao.getAll().mapNotNull { entity -> getSeriesInternal(entity.id, includeContent = false) }
    }

    suspend fun getSeriesBasic(id: UUID): Series? = getSeriesInternal(id, includeContent = false)

    suspend fun getSeriesWithContent(id: UUID): Series? = getSeriesInternal(id, includeContent = true)

    private suspend fun getSeriesInternal(id: UUID, includeContent: Boolean): Series? {
        val entity = seriesDao.getById(id) ?: return null
        val seasons = if (includeContent) {
            seasonDao.getBySeriesId(id).map { seasonEntity ->
                val episodes = episodeDao.getBySeasonId(seasonEntity.id).map { episodeEntity ->
                    episodeEntity.toDomain()
                }
                seasonEntity.toDomain(episodes)
            }
        } else emptyList()
        return entity.toDomain(seasons)
    }

    suspend fun getSeason(seriesId: UUID, seasonId: UUID): Season? {
        val seasonEntity = seasonDao.getById(seasonId) ?: return null
        val episodes = episodeDao.getBySeasonId(seasonId).map { episodeEntity ->
            episodeEntity.toDomain()
        }
        return seasonEntity.toDomain(episodes)
    }

    suspend fun getSeasons(seriesId: UUID): List<Season> {
        return seasonDao.getBySeriesId(seriesId).map { seasonEntity ->
            val episodes = episodeDao.getBySeasonId(seasonEntity.id).map { episodeEntity ->
                episodeEntity.toDomain()
            }
            seasonEntity.toDomain(episodes)
        }
    }

    suspend fun getEpisode(seriesId: UUID, seasonId: UUID, episodeId: UUID): Episode? {
        val episodeEntity = episodeDao.getById(episodeId) ?: return null
        return episodeEntity.toDomain()
    }

    suspend fun getEpisodeById(episodeId: UUID): Episode? {
        val episodeEntity = episodeDao.getById(episodeId) ?: return null
        return episodeEntity.toDomain()
    }

    suspend fun updateWatchProgress(mediaId: UUID, progress: Double?, watched: Boolean) {
        movieDao.getById(mediaId)?.let {
            movieDao.updateProgress(mediaId, progress, watched)
            return
        }

        episodeDao.getById(mediaId)?.let { episode ->
            database.withTransaction {
                episodeDao.updateProgress(mediaId, progress, watched)
                val seasonUnwatched = episodeDao.countUnwatchedBySeason(episode.seasonId)
                seasonDao.updateUnwatchedCount(episode.seasonId, seasonUnwatched)
                val seriesUnwatched = episodeDao.countUnwatchedBySeries(episode.seriesId)
                seriesDao.updateUnwatchedCount(episode.seriesId, seriesUnwatched)
            }
        }
    }

    suspend fun getEpisodesBySeries(seriesId: UUID): List<Episode> {
        return episodeDao.getBySeriesId(seriesId).map { episodeEntity ->
            episodeEntity.toDomain()
        }
    }

    private fun Movie.toEntity() = MovieEntity(
        id = id,
        libraryId = libraryId,
        title = title,
        progress = progress,
        watched = watched,
        year = year,
        rating = rating,
        runtime = runtime,
        format = format,
        synopsis = synopsis,
        heroImageUrl = heroImageUrl,
        audioTrack = audioTrack,
        subtitles = subtitles
    )

    private fun Series.toEntity() = SeriesEntity(
        id = id,
        libraryId = libraryId,
        name = name,
        synopsis = synopsis,
        year = year,
        heroImageUrl = heroImageUrl,
        unwatchedEpisodeCount = unwatchedEpisodeCount,
        seasonCount = seasonCount
    )

    private fun Season.toEntity() = SeasonEntity(
        id = id,
        seriesId = seriesId,
        name = name,
        index = index,
        unwatchedEpisodeCount = unwatchedEpisodeCount,
        episodeCount = episodeCount
    )

    private fun Episode.toEntity() = EpisodeEntity(
        id = id,
        seriesId = seriesId,
        seasonId = seasonId,
        index = index,
        title = title,
        synopsis = synopsis,
        releaseDate = releaseDate,
        rating = rating,
        runtime = runtime,
        progress = progress,
        watched = watched,
        format = format,
        heroImageUrl = heroImageUrl
    )

    private fun MovieEntity.toDomain() = Movie(
        id = id,
        libraryId = libraryId,
        title = title,
        progress = progress,
        watched = watched,
        year = year,
        rating = rating,
        runtime = runtime,
        format = format,
        synopsis = synopsis,
        heroImageUrl = heroImageUrl,
        audioTrack = audioTrack,
        subtitles = subtitles,
        cast = emptyList()
    )

    private fun SeriesEntity.toDomain(seasons: List<Season>) = Series(
        id = id,
        libraryId = libraryId,
        name = name,
        synopsis = synopsis,
        year = year,
        heroImageUrl = heroImageUrl,
        unwatchedEpisodeCount = unwatchedEpisodeCount,
        seasonCount = seasonCount,
        seasons = seasons,
        cast = emptyList()
    )

    private fun SeasonEntity.toDomain(episodes: List<Episode>) = Season(
        id = id,
        seriesId = seriesId,
        name = name,
        index = index,
        unwatchedEpisodeCount = unwatchedEpisodeCount,
        episodeCount = episodeCount,
        episodes = episodes
    )

    private fun EpisodeEntity.toDomain() = Episode(
        id = id,
        seriesId = seriesId,
        seasonId = seasonId,
        index = index,
        title = title,
        synopsis = synopsis,
        releaseDate = releaseDate,
        rating = rating,
        runtime = runtime,
        progress = progress,
        watched = watched,
        format = format,
        heroImageUrl = heroImageUrl,
        cast = emptyList()
    )
}