package hu.bbara.purefin.core.model

import java.util.UUID

sealed class Media(
    val id: UUID,
    val type: MediaKind,
) {
    class MovieMedia(val movieId: UUID) : Media(movieId, MediaKind.MOVIE)
    class SeriesMedia(val seriesId: UUID) : Media(seriesId, MediaKind.SERIES)
    class SeasonMedia(val seasonId: UUID, val seriesId: UUID) : Media(seasonId, MediaKind.SEASON)
    class EpisodeMedia(val episodeId: UUID, val seriesId: UUID) : Media(episodeId, MediaKind.EPISODE)
}
