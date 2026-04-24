package hu.bbara.purefin.data.jellyfin.download

import hu.bbara.purefin.image.ImageUrlBuilder
import hu.bbara.purefin.model.Episode
import hu.bbara.purefin.model.Movie
import hu.bbara.purefin.model.Season
import hu.bbara.purefin.model.Series
import java.util.UUID
import java.util.concurrent.TimeUnit
import org.jellyfin.sdk.model.api.BaseItemDto

internal fun BaseItemDto.toMovie(serverUrl: String): Movie {
    return Movie(
        id = id,
        libraryId = parentId ?: UUID.randomUUID(),
        title = name ?: "Unknown title",
        progress = userData?.playedPercentage,
        watched = userData?.played ?: false,
        year = productionYear?.toString() ?: premiereDate?.year?.toString().orEmpty(),
        rating = officialRating ?: "NR",
        runtime = formatRuntime(runTimeTicks),
        format = container?.uppercase() ?: "VIDEO",
        synopsis = overview ?: "No synopsis available",
        imageUrlPrefix = ImageUrlBuilder.toPrefixImageUrl(serverUrl, id),
        audioTrack = "ENG",
        subtitles = "ENG",
        cast = emptyList(),
    )
}

internal fun BaseItemDto.toEpisode(serverUrl: String): Episode {
    return Episode(
        id = id,
        seriesId = seriesId ?: UUID.randomUUID(),
        seasonId = parentId ?: UUID.randomUUID(),
        title = name ?: "Unknown title",
        index = indexNumber ?: 0,
        synopsis = overview ?: "No synopsis available.",
        releaseDate = productionYear?.toString() ?: "—",
        rating = officialRating ?: "NR",
        runtime = formatRuntime(runTimeTicks),
        progress = userData?.playedPercentage,
        watched = userData?.played ?: false,
        format = container?.uppercase() ?: "VIDEO",
        imageUrlPrefix = ImageUrlBuilder.toPrefixImageUrl(serverUrl, id),
        cast = emptyList(),
    )
}

internal fun BaseItemDto.toSeries(serverUrl: String): Series {
    return Series(
        id = id,
        libraryId = parentId ?: UUID.randomUUID(),
        name = name ?: "Unknown",
        synopsis = overview ?: "No synopsis available",
        year = productionYear?.toString() ?: premiereDate?.year?.toString().orEmpty(),
        imageUrlPrefix = ImageUrlBuilder.toPrefixImageUrl(serverUrl, id),
        unwatchedEpisodeCount = userData?.unplayedItemCount ?: 0,
        seasonCount = childCount ?: 0,
        seasons = emptyList(),
        cast = emptyList(),
    )
}

internal fun BaseItemDto.toSeason(seriesId: UUID): Season {
    return Season(
        id = id,
        seriesId = this.seriesId ?: seriesId,
        name = name ?: "Unknown",
        index = indexNumber ?: 0,
        unwatchedEpisodeCount = userData?.unplayedItemCount ?: 0,
        episodeCount = childCount ?: 0,
        episodes = emptyList(),
    )
}

private fun formatRuntime(ticks: Long?): String {
    if (ticks == null || ticks <= 0) return "—"
    val totalSeconds = ticks / 10_000_000
    val hours = TimeUnit.SECONDS.toHours(totalSeconds)
    val minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}
