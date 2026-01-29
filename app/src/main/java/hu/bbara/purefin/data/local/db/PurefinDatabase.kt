package hu.bbara.purefin.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import hu.bbara.purefin.data.local.dao.EpisodeDao
import hu.bbara.purefin.data.local.dao.SeasonDao
import hu.bbara.purefin.data.local.dao.SeriesDao
import hu.bbara.purefin.data.local.entity.EpisodeEntity
import hu.bbara.purefin.data.local.entity.SeasonEntity
import hu.bbara.purefin.data.local.entity.SeriesEntity

@Database(
    entities = [SeriesEntity::class, SeasonEntity::class, EpisodeEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(PurefinTypeConverters::class)
abstract class PurefinDatabase : RoomDatabase() {

    abstract fun seriesDao(): SeriesDao

    abstract fun seasonDao(): SeasonDao

    abstract fun episodeDao(): EpisodeDao
}
