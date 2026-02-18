package hu.bbara.purefin.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import hu.bbara.purefin.data.local.room.dao.CastMemberDao
import hu.bbara.purefin.data.local.room.dao.EpisodeDao
import hu.bbara.purefin.data.local.room.dao.LibraryDao
import hu.bbara.purefin.data.local.room.dao.MovieDao
import hu.bbara.purefin.data.local.room.dao.SeasonDao
import hu.bbara.purefin.data.local.room.dao.SeriesDao

@Database(
    entities = [
        MovieEntity::class,
        SeriesEntity::class,
        SeasonEntity::class,
        EpisodeEntity::class,
        LibraryEntity::class,
        CastMemberEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(UuidConverters::class)
abstract class OfflineMediaDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun seriesDao(): SeriesDao
    abstract fun seasonDao(): SeasonDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun libraryDao(): LibraryDao
    abstract fun castMemberDao(): CastMemberDao
}
