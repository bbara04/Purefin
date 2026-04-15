package hu.bbara.purefin.data.offline.room

import androidx.room.Embedded
import androidx.room.Relation
import hu.bbara.purefin.data.offline.room.entity.EpisodeEntity
import hu.bbara.purefin.data.offline.room.entity.LibraryEntity
import hu.bbara.purefin.data.offline.room.entity.MovieEntity
import hu.bbara.purefin.data.offline.room.entity.SeasonEntity
import hu.bbara.purefin.data.offline.room.entity.SeriesEntity

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
