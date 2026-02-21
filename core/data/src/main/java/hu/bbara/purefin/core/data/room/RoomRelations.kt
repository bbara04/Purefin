package hu.bbara.purefin.core.data.room

import androidx.room.Embedded
import androidx.room.Relation
import hu.bbara.purefin.core.data.room.entity.EpisodeEntity
import hu.bbara.purefin.core.data.room.entity.LibraryEntity
import hu.bbara.purefin.core.data.room.entity.MovieEntity
import hu.bbara.purefin.core.data.room.entity.SeasonEntity
import hu.bbara.purefin.core.data.room.entity.SeriesEntity

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

data class LibraryWithContent(
    @Embedded val library: LibraryEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "libraryId"
    )
    val series: List<SeriesEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "libraryId"
    )
    val movies: List<MovieEntity>
)
