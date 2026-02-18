package hu.bbara.purefin.data.local.room

import androidx.room.withTransaction
import hu.bbara.purefin.data.local.room.dao.CastMemberDao
import hu.bbara.purefin.data.local.room.dao.EpisodeDao
import hu.bbara.purefin.data.local.room.dao.LibraryDao
import hu.bbara.purefin.data.local.room.dao.MovieDao
import hu.bbara.purefin.data.local.room.dao.SeasonDao
import hu.bbara.purefin.data.local.room.dao.SeriesDao
import hu.bbara.purefin.data.model.CastMember
import hu.bbara.purefin.data.model.Episode
import hu.bbara.purefin.data.model.Library
import hu.bbara.purefin.data.model.Movie
import hu.bbara.purefin.data.model.Season
import hu.bbara.purefin.data.model.Series
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jellyfin.sdk.model.api.CollectionType
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineRoomMediaLocalDataSource(
    private val database: OfflineMediaDatabase,
    private val movieDao: MovieDao,
    private val seriesDao: SeriesDao,
    private val seasonDao: SeasonDao,
    private val episodeDao: EpisodeDao,
    private val castMemberDao: CastMemberDao,
    private val libraryDao: LibraryDao
) {

    // Lightweight Flows for list screens (home, library)
    val librariesFlow: Flow<List<Library>> = libraryDao.observeAllWithContent()
        .map { relation ->
            relation.map {
                it.library.toDomain(
                    movies = it.movies.map { e -> e.toDomain(cast = emptyList()) },
                    series = it.series.map { e -> e.toDomain(seasons = emptyList(), cast = emptyList()) }
                )
            }
        }

    val moviesFlow: Flow<Map<UUID, Movie>> = movieDao.observeAll()
        .map { entities -> entities.associate { it.id to it.toDomain(cast = emptyList()) } }

    val seriesFlow: Flow<Map<UUID, Series>> = seriesDao.observeAll()
        .map { entities ->
            entities.associate { it.id to it.toDomain(seasons = emptyList(), cast = emptyList()) }
        }

    val episodesFlow: Flow<Map<UUID, Episode>> = episodeDao.observeAll()
        .map { entities -> entities.associate { it.id to it.toDomain(cast = emptyList()) } }

    // Full content Flow for series detail screen (scoped to one series)
    fun observeSeriesWithContent(seriesId: UUID): Flow<Series?> =
        seriesDao.observeWithContent(seriesId).map { relation ->
            relation?.let {
                it.series.toDomain(
                    seasons = it.seasons.map { swe ->
                        swe.season.toDomain(
                            episodes = swe.episodes.map { ep -> ep.toDomain(cast = emptyList()) }
                        )
                    },
                    cast = emptyList()
                )
            }
        }

    suspend fun saveLibraries(libraries: List<Library>) {
        database.withTransaction {
            libraryDao.deleteAll()
            libraryDao.upsertAll(libraries.map { it.toEntity() })
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
            val cast = castMemberDao.getByMovieId(entity.id).map { it.toDomain() }
            entity.toDomain(cast)
        }
    }

    suspend fun getMovie(id: UUID): Movie? {
        val entity = movieDao.getById(id) ?: return null
        val cast = castMemberDao.getByMovieId(id).map { it.toDomain() }
        return entity.toDomain(cast)
    }

    suspend fun getSeries(): List<Series> {
        return seriesDao.getAll().mapNotNull { entity -> getSeriesInternal(entity.id, includeContent = false) }
    }

    suspend fun getSeriesBasic(id: UUID): Series? = getSeriesInternal(id, includeContent = false)

    suspend fun getSeriesWithContent(id: UUID): Series? = getSeriesInternal(id, includeContent = true)

    private suspend fun getSeriesInternal(id: UUID, includeContent: Boolean): Series? {
        val entity = seriesDao.getById(id) ?: return null
        val cast = castMemberDao.getBySeriesId(id).map { it.toDomain() }
        val seasons = if (includeContent) {
            seasonDao.getBySeriesId(id).map { seasonEntity ->
                val episodes = episodeDao.getBySeasonId(seasonEntity.id).map { episodeEntity ->
                    val episodeCast = castMemberDao.getByEpisodeId(episodeEntity.id).map { it.toDomain() }
                    episodeEntity.toDomain(episodeCast)
                }
                seasonEntity.toDomain(episodes)
            }
        } else emptyList()
        return entity.toDomain(seasons, cast)
    }

    suspend fun getSeason(seriesId: UUID, seasonId: UUID): Season? {
        val seasonEntity = seasonDao.getById(seasonId) ?: return null
        val episodes = episodeDao.getBySeasonId(seasonId).map { episodeEntity ->
            val episodeCast = castMemberDao.getByEpisodeId(episodeEntity.id).map { it.toDomain() }
            episodeEntity.toDomain(episodeCast)
        }
        return seasonEntity.toDomain(episodes)
    }

    suspend fun getSeasons(seriesId: UUID): List<Season> {
        return seasonDao.getBySeriesId(seriesId).map { seasonEntity ->
            val episodes = episodeDao.getBySeasonId(seasonEntity.id).map { episodeEntity ->
                val episodeCast = castMemberDao.getByEpisodeId(episodeEntity.id).map { it.toDomain() }
                episodeEntity.toDomain(episodeCast)
            }
            seasonEntity.toDomain(episodes)
        }
    }

    suspend fun getEpisode(seriesId: UUID, seasonId: UUID, episodeId: UUID): Episode? {
        val episodeEntity = episodeDao.getById(episodeId) ?: return null
        val cast = castMemberDao.getByEpisodeId(episodeId).map { it.toDomain() }
        return episodeEntity.toDomain(cast)
    }

    suspend fun getEpisodeById(episodeId: UUID): Episode? {
        val episodeEntity = episodeDao.getById(episodeId) ?: return null
        val cast = castMemberDao.getByEpisodeId(episodeId).map { it.toDomain() }
        return episodeEntity.toDomain(cast)
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
            val cast = castMemberDao.getByEpisodeId(episodeEntity.id).map { it.toDomain() }
            episodeEntity.toDomain(cast)
        }
    }

    private fun Library.toEntity() = LibraryEntity(
        id = id,
        name = name,
        type = when (type) {
            CollectionType.MOVIES -> "MOVIES"
            CollectionType.TVSHOWS -> "TVSHOWS"
            else -> throw UnsupportedOperationException("Unsupported library type: $type")
        }
    )

    private fun LibraryEntity.toDomain(series: List<Series>, movies: List<Movie>) = Library(
        id = id,
        name = name,
        type = when (type) {
            "MOVIES" -> CollectionType.MOVIES
            "TVSHOWS" -> CollectionType.TVSHOWS
            else -> throw UnsupportedOperationException("Unsupported library type: $type")
        },
        movies = if (type == "MOVIES") movies else null,
        series = if (type == "TVSHOWS") series else null,
    )

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

    private fun MovieEntity.toDomain(cast: List<CastMember>) = Movie(
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
        cast = cast
    )

    private fun SeriesEntity.toDomain(seasons: List<Season>, cast: List<CastMember>) = Series(
        id = id,
        libraryId = libraryId,
        name = name,
        synopsis = synopsis,
        year = year,
        heroImageUrl = heroImageUrl,
        unwatchedEpisodeCount = unwatchedEpisodeCount,
        seasonCount = seasonCount,
        seasons = seasons,
        cast = cast
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

    private fun EpisodeEntity.toDomain(cast: List<CastMember>) = Episode(
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
        cast = cast
    )

    private fun CastMember.toMovieEntity(movieId: UUID) = CastMemberEntity(
        name = name,
        role = role,
        imageUrl = imageUrl,
        movieId = movieId
    )

    private fun CastMember.toSeriesEntity(seriesId: UUID) = CastMemberEntity(
        name = name,
        role = role,
        imageUrl = imageUrl,
        seriesId = seriesId
    )

    private fun CastMember.toEpisodeEntity(episodeId: UUID) = CastMemberEntity(
        name = name,
        role = role,
        imageUrl = imageUrl,
        episodeId = episodeId
    )

    private fun CastMemberEntity.toDomain() = CastMember(
        name = name,
        role = role,
        imageUrl = imageUrl
    )
}
