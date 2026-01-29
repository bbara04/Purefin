package hu.bbara.purefin.data.local.relations

import androidx.room.Embedded
import androidx.room.Relation
import hu.bbara.purefin.data.local.entity.SeasonEntity
import hu.bbara.purefin.data.local.entity.SeriesEntity

data class SeriesWithSeasonsAndEpisodes(
    @Embedded val series: SeriesEntity,
    @Relation(
        entity = SeasonEntity::class,
        parentColumn = "id",
        entityColumn = "seriesId"
    )
    val seasons: List<SeasonWithEpisodes>
)
