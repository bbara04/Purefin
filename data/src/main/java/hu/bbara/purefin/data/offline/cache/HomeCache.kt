package hu.bbara.purefin.data.offline.cache

import hu.bbara.purefin.model.CastMember
import hu.bbara.purefin.model.Episode
import hu.bbara.purefin.model.Library
import hu.bbara.purefin.model.LibraryKind
import hu.bbara.purefin.model.Media
import hu.bbara.purefin.model.Movie
import hu.bbara.purefin.model.Series
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class CachedCastMember(
    val name: String,
    val role: String,
    val imageUrl: String? = null
)

@Serializable
data class CachedMovie(
    val id: String,
    val libraryId: String,
    val title: String,
    val progress: Double? = null,
    val watched: Boolean,
    val year: String,
    val rating: String,
    val runtime: String,
    val format: String,
    val synopsis: String,
    val imageUrlPrefix: String,
    val cast: List<CachedCastMember> = emptyList()
)

@Serializable
data class CachedSeries(
    val id: String,
    val libraryId: String,
    val name: String,
    val synopsis: String,
    val year: String,
    val imageUrlPrefix: String,
    val unwatchedEpisodeCount: Int,
    val seasonCount: Int,
    val cast: List<CachedCastMember> = emptyList()
)

@Serializable
data class CachedEpisode(
    val id: String,
    val seriesId: String,
    val seriesName: String,
    val seasonId: String,
    val seasonIndex: Int,
    val index: Int,
    val title: String,
    val synopsis: String,
    val releaseDate: String,
    val rating: String,
    val runtime: String,
    val progress: Double? = null,
    val watched: Boolean,
    val format: String,
    val imageUrlPrefix: String,
    val cast: List<CachedCastMember> = emptyList()
)

@Serializable
data class CachedMediaItem(
    val type: String,
    val id: String,
    val seriesId: String? = null
)

@Serializable
data class CachedLibrary(
    val id: String,
    val name: String,
    val type: String,
    val posterUrl: String,
    val size: Int = 0,
    val series: List<CachedSeries>? = null,
    val movies: List<CachedMovie>? = null,
)

@Serializable
data class HomeCache(
    val suggestions: List<CachedMediaItem> = emptyList(),
    val continueWatching: List<CachedMediaItem> = emptyList(),
    val nextUp: List<CachedMediaItem> = emptyList(),
    val latestLibraryContent: Map<String, List<CachedMediaItem>> = emptyMap(),
    val libraries: List<CachedLibrary> = emptyList(),
    val movies: List<CachedMovie> = emptyList(),
    val series: List<CachedSeries> = emptyList(),
    val episodes: List<CachedEpisode> = emptyList()
)

fun Library.toCachedLibrary() = CachedLibrary(
    id = id.toString(),
    name = name,
    type = type.name,
    posterUrl = posterUrl,
    size = size,
    series = series?.map { it.toCachedSeries() },
    movies = movies?.map { it.toCachedMovie() },
)

fun CachedLibrary.toLibrary(): Library? {
    val libraryId = id.toUuidOrNull() ?: return null
    return when (type) {
        "MOVIES" -> Library(
            id = libraryId,
            name = name,
            type = LibraryKind.MOVIES,
            posterUrl = posterUrl,
            size = size,
            movies = movies?.mapNotNull { it.toMovie() } ?: emptyList(),
        )
        "SERIES" -> Library(
            id = libraryId,
            name = name,
            type = LibraryKind.SERIES,
            posterUrl = posterUrl,
            size = size,
            series = series?.mapNotNull { it.toSeries() } ?: emptyList(),
        )
        else -> null
    }
}

fun Media.toCachedItem(): CachedMediaItem = when (this) {
    is Media.MovieMedia -> CachedMediaItem(type = "MOVIE", id = movieId.toString())
    is Media.SeriesMedia -> CachedMediaItem(type = "SERIES", id = seriesId.toString())
    is Media.SeasonMedia -> CachedMediaItem(type = "SEASON", id = seasonId.toString(), seriesId = seriesId.toString())
    is Media.EpisodeMedia -> CachedMediaItem(type = "EPISODE", id = episodeId.toString(), seriesId = seriesId.toString())
}

fun CachedMediaItem.toMedia(): Media? {
    val uuid = runCatching { UUID.fromString(id) }.getOrNull() ?: return null
    val seriesUuid = seriesId?.let { runCatching { UUID.fromString(it) }.getOrNull() }
    return when (type) {
        "MOVIE" -> Media.MovieMedia(movieId = uuid)
        "SERIES" -> Media.SeriesMedia(seriesId = uuid)
        "SEASON" -> Media.SeasonMedia(seasonId = uuid, seriesId = seriesUuid ?: return null)
        "EPISODE" -> Media.EpisodeMedia(episodeId = uuid, seriesId = seriesUuid ?: return null)
        else -> null
    }
}

fun Movie.toCachedMovie() = CachedMovie(
    id = id.toString(),
    libraryId = libraryId.toString(),
    title = title,
    progress = progress,
    watched = watched,
    year = year,
    rating = rating,
    runtime = runtime,
    format = format,
    synopsis = synopsis,
    imageUrlPrefix = imageUrlPrefix,
    cast = cast.map { it.toCachedCastMember() },
)

fun CachedMovie.toMovie(): Movie? {
    val movieId = id.toUuidOrNull() ?: return null
    val libraryUuid = libraryId.toUuidOrNull() ?: return null
    return Movie(
        id = movieId,
        libraryId = libraryUuid,
        title = title,
        progress = progress,
        watched = watched,
        year = year,
        rating = rating,
        runtime = runtime,
        format = format,
        synopsis = synopsis,
        imageUrlPrefix = imageUrlPrefix,
        cast = cast.map { it.toCastMember() },
    )
}

fun Series.toCachedSeries() = CachedSeries(
    id = id.toString(),
    libraryId = libraryId.toString(),
    name = name,
    synopsis = synopsis,
    year = year,
    imageUrlPrefix = imageUrlPrefix,
    unwatchedEpisodeCount = unwatchedEpisodeCount,
    seasonCount = seasonCount,
    cast = cast.map { it.toCachedCastMember() },
)

fun CachedSeries.toSeries(): Series? {
    val seriesId = id.toUuidOrNull() ?: return null
    val libraryUuid = libraryId.toUuidOrNull() ?: return null
    return Series(
        id = seriesId,
        libraryId = libraryUuid,
        name = name,
        synopsis = synopsis,
        year = year,
        imageUrlPrefix = imageUrlPrefix,
        unwatchedEpisodeCount = unwatchedEpisodeCount,
        seasonCount = seasonCount,
        seasons = emptyList(),
        cast = cast.map { it.toCastMember() },
    )
}

fun Episode.toCachedEpisode() = CachedEpisode(
    id = id.toString(),
    seriesId = seriesId.toString(),
    seriesName = seriesName,
    seasonId = seasonId.toString(),
    seasonIndex = seasonIndex,
    index = index,
    title = title,
    synopsis = synopsis,
    releaseDate = releaseDate,
    rating = rating,
    runtime = runtime,
    progress = progress,
    watched = watched,
    format = format,
    imageUrlPrefix = imageUrlPrefix,
    cast = cast.map { it.toCachedCastMember() },
)

fun CachedEpisode.toEpisode(): Episode? {
    val episodeId = id.toUuidOrNull() ?: return null
    val seriesUuid = seriesId.toUuidOrNull() ?: return null
    val seasonUuid = seasonId.toUuidOrNull() ?: return null
    return Episode(
        id = episodeId,
        seriesId = seriesUuid,
        seriesName = seriesName,
        seasonId = seasonUuid,
        seasonIndex = seasonIndex,
        index = index,
        title = title,
        synopsis = synopsis,
        releaseDate = releaseDate,
        rating = rating,
        runtime = runtime,
        progress = progress,
        watched = watched,
        format = format,
        imageUrlPrefix = imageUrlPrefix,
        cast = cast.map { it.toCastMember() },
    )
}

private fun CastMember.toCachedCastMember() = CachedCastMember(
    name = name,
    role = role,
    imageUrl = imageUrl,
)

private fun CachedCastMember.toCastMember() = CastMember(
    name = name,
    role = role,
    imageUrl = imageUrl,
)

private fun String.toUuidOrNull(): UUID? = runCatching { UUID.fromString(this) }.getOrNull()
