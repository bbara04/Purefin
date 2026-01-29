package hu.bbara.purefin.data.local.relations

import androidx.room.Embedded
import androidx.room.Relation
import hu.bbara.purefin.data.local.entity.EpisodeEntity
import hu.bbara.purefin.data.local.entity.SeasonEntity

data class SeasonWithEpisodes(
    @Embedded val season: SeasonEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "seasonId"
    )
    val episodes: List<EpisodeEntity>
)
