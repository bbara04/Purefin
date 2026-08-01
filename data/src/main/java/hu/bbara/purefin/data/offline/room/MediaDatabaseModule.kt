package hu.bbara.purefin.data.offline.room

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import hu.bbara.purefin.data.offline.room.dao.EpisodeDao
import hu.bbara.purefin.data.offline.room.dao.MovieDao
import hu.bbara.purefin.data.offline.room.dao.SeasonDao
import hu.bbara.purefin.data.offline.room.dao.SeriesDao
import hu.bbara.purefin.data.offline.room.dao.SmartDownloadDao
import hu.bbara.purefin.data.offline.room.dao.SubtitleDao
import hu.bbara.purefin.data.offline.room.offline.OfflineMediaDatabase
import hu.bbara.purefin.data.offline.room.offline.OfflineRoomMediaLocalDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaDatabaseModule {

    private val MIGRATION_12_13 = object : Migration(12, 13) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE smart_downloads ADD COLUMN count INTEGER NOT NULL DEFAULT 5"
            )
        }
    }

    // Offline Database and DAOs
    @Provides
    @Singleton
    fun provideOfflineDatabase(@ApplicationContext context: Context): OfflineMediaDatabase =
        Room.databaseBuilder(context, OfflineMediaDatabase::class.java, "offline_media_database")
            .addMigrations(MIGRATION_12_13)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideOfflineMovieDao(db: OfflineMediaDatabase) = db.movieDao()

    @Provides
    fun provideOfflineSeriesDao(db: OfflineMediaDatabase) = db.seriesDao()

    @Provides
    fun provideOfflineSeasonDao(db: OfflineMediaDatabase) = db.seasonDao()

    @Provides
    fun provideOfflineEpisodeDao(db: OfflineMediaDatabase) = db.episodeDao()

    @Provides
    fun provideSmartDownloadDao(db: OfflineMediaDatabase): SmartDownloadDao = db.smartDownloadDao()

    @Provides
    fun provideSubtitleDao(db: OfflineMediaDatabase): SubtitleDao = db.subtitleDao()

    @Provides
    @Singleton
    fun provideOfflineDataSource(
        database: OfflineMediaDatabase,
        movieDao: MovieDao,
        seriesDao: SeriesDao,
        seasonDao: SeasonDao,
        episodeDao: EpisodeDao,
        subtitleDao: SubtitleDao,
    ): OfflineRoomMediaLocalDataSource = OfflineRoomMediaLocalDataSource(
        database, movieDao, seriesDao, seasonDao, episodeDao, subtitleDao
    )
}