package hu.bbara.purefin.core.model

import org.jellyfin.sdk.model.api.BaseItemKind
import java.util.UUID

sealed class Media(
    val id: UUID,
    val type: BaseItemKind
) {
    class MovieMedia(val movieId: UUID) : Media(movieId, BaseItemKind.MOVIE)
    class SeriesMedia(val seriesId: UUID) : Media(seriesId, BaseItemKind.SERIES)
    class SeasonMedia(val seasonId: UUID, val seriesId: UUID) : Media(seasonId, BaseItemKind.SEASON)
    class EpisodeMedia(val episodeId: UUID, val seriesId: UUID) : Media(episodeId, BaseItemKind.EPISODE)
}
