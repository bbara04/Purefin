package hu.bbara.purefin.core.data.room.offline

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import hu.bbara.purefin.core.data.room.UuidConverters
import hu.bbara.purefin.core.data.room.dao.EpisodeDao
import hu.bbara.purefin.core.data.room.dao.MovieDao
import hu.bbara.purefin.core.data.room.dao.SeasonDao
import hu.bbara.purefin.core.data.room.dao.SeriesDao
import hu.bbara.purefin.core.data.room.entity.EpisodeEntity
import hu.bbara.purefin.core.data.room.entity.MovieEntity
import hu.bbara.purefin.core.data.room.entity.SeasonEntity
import hu.bbara.purefin.core.data.room.entity.SeriesEntity

@Database(
    entities = [
        MovieEntity::class,
        SeriesEntity::class,
        SeasonEntity::class,
        EpisodeEntity::class,
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(UuidConverters::class)
abstract class OfflineMediaDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun seriesDao(): SeriesDao
    abstract fun seasonDao(): SeasonDao
    abstract fun episodeDao(): EpisodeDao
}