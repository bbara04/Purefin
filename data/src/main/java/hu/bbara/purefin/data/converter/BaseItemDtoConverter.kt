package hu.bbara.purefin.data.converter

import hu.bbara.purefin.image.ArtworkKind
import hu.bbara.purefin.image.ImageUrlBuilder
import hu.bbara.purefin.model.Episode
import hu.bbara.purefin.model.Library
import hu.bbara.purefin.model.LibraryKind
import hu.bbara.purefin.model.Movie
import hu.bbara.purefin.model.Season
import hu.bbara.purefin.model.Series
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.CollectionType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit

fun BaseItemDto.toLibrary(serverUrl: String): Library {
    return when (collectionType) {
        CollectionType.MOVIES -> Library(
            id = id,
            name = name!!,
            posterUrl = ImageUrlBuilder.toImageUrl(
                url = serverUrl,
                itemId = id,
                artworkKind = ArtworkKind.PRIMARY
            ),
            type = LibraryKind.MOVIES,
            movies = emptyList(),
        )
        CollectionType.TVSHOWS -> Library(
            id = id,
            name = name!!,
            posterUrl = ImageUrlBuilder.toImageUrl(
                url = serverUrl,
                itemId = id,
                artworkKind = ArtworkKind.PRIMARY
            ),
            type = LibraryKind.SERIES,
            series = emptyList(),
        )
        else -> throw UnsupportedOperationException("Unsupported library type: $collectionType")
    }
}

fun BaseItemDto.toMovie(serverUrl: String): Movie {
    return Movie(
        id = id,
        libraryId = parentId!!,
        title = name ?: "Unknown title",
        progress = userData!!.playedPercentage,
        watched = userData!!.played,
        year = productionYear?.toString() ?: premiereDate?.year?.toString().orEmpty(),
        rating = officialRating ?: "NR",
        runtime = formatRuntime(runTimeTicks),
        synopsis = overview ?: "No synopsis available",
        format = container?.uppercase() ?: "VIDEO",
        imageUrlPrefix = ImageUrlBuilder.toPrefixImageUrl(url = serverUrl, itemId = id),
        cast = emptyList(),
    )
}

fun BaseItemDto.toSeries(serverUrl: String): Series {
    return Series(
        id = id,
        libraryId = parentId!!,
        name = name ?: "Unknown",
        synopsis = overview ?: "No synopsis available",
        year = productionYear?.toString() ?: premiereDate?.year?.toString().orEmpty(),
        imageUrlPrefix = ImageUrlBuilder.toPrefixImageUrl(url = serverUrl, itemId = id),
        unwatchedEpisodeCount = userData!!.unplayedItemCount!!,
        seasonCount = childCount!!,
        seasons = emptyList(),
        cast = emptyList(),
    )
}

fun BaseItemDto.toSeason(): Season {
    return Season(
        id = id,
        seriesId = seriesId!!,
        name = name ?: "Unknown",
        index = indexNumber ?: 0,
        unwatchedEpisodeCount = userData!!.unplayedItemCount!!,
        episodeCount = childCount!!,
        episodes = emptyList(),
    )
}

fun BaseItemDto.toEpisode(serverUrl: String): Episode {
    val releaseDate = formatReleaseDate(premiereDate, productionYear)
    val imageUrlPrefix = id?.let { itemId ->
        ImageUrlBuilder.toPrefixImageUrl(url = serverUrl, itemId = itemId)
    } ?: ""
    return Episode(
        id = id,
        seriesId = seriesId!!,
        seriesName = seriesName!!,
        seasonId = parentId!!,
        seasonIndex = parentIndexNumber!!,
        title = name ?: "Unknown title",
        index = indexNumber!!,
        releaseDate = releaseDate,
        rating = officialRating ?: "NR",
        runtime = formatRuntime(runTimeTicks),
        progress = userData!!.playedPercentage,
        watched = userData!!.played,
        format = container?.uppercase() ?: "VIDEO",
        synopsis = overview ?: "No synopsis available.",
        imageUrlPrefix = imageUrlPrefix,
        cast = emptyList(),
    )
}

fun formatReleaseDate(date: LocalDateTime?, fallbackYear: Int?): String {
    if (date == null) {
        return fallbackYear?.toString() ?: "—"
    }
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
    return date.toLocalDate().format(formatter)
}

private fun formatRuntime(ticks: Long?): String {
    if (ticks == null || ticks <= 0) return "—"
    val totalSeconds = ticks / 10_000_000
    val hours = TimeUnit.SECONDS.toHours(totalSeconds)
    val minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}