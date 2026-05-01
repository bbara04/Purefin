package hu.bbara.purefin.data.offline.room.offline

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import hu.bbara.purefin.data.offline.room.UuidConverters
import hu.bbara.purefin.data.offline.room.dao.EpisodeDao
import hu.bbara.purefin.data.offline.room.dao.MovieDao
import hu.bbara.purefin.data.offline.room.dao.SeasonDao
import hu.bbara.purefin.data.offline.room.dao.SeriesDao
import hu.bbara.purefin.data.offline.room.dao.SmartDownloadDao
import hu.bbara.purefin.data.offline.room.entity.EpisodeEntity
import hu.bbara.purefin.data.offline.room.entity.MovieEntity
import hu.bbara.purefin.data.offline.room.entity.SeasonEntity
import hu.bbara.purefin.data.offline.room.entity.SeriesEntity
import hu.bbara.purefin.data.offline.room.entity.SmartDownloadEntity

@Database(
    entities = [
        MovieEntity::class,
        SeriesEntity::class,
        SeasonEntity::class,
        EpisodeEntity::class,
        SmartDownloadEntity::class,
    ],
    version = 11,
    exportSchema = false
)
@TypeConverters(UuidConverters::class)
abstract class OfflineMediaDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun seriesDao(): SeriesDao
    abstract fun seasonDao(): SeasonDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun smartDownloadDao(): SmartDownloadDao
}
