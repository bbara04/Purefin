package hu.bbara.purefin.data.local.room

import androidx.room.Embedded
import androidx.room.Relation

data class SeasonWithEpisodes(
    @Embedded val season: SeasonEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "seasonId"
    )
    val episodes: List<EpisodeEntity>
)

data class SeriesWithSeasonsAndEpisodes(
    @Embedded val series: SeriesEntity,
    @Relation(
        entity = SeasonEntity::class,
        parentColumn = "id",
        entityColumn = "seriesId"
    )
    val seasons: List<SeasonWithEpisodes>
)
