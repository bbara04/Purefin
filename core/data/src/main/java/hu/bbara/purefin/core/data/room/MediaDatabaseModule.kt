package hu.bbara.purefin.core.data.room

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import hu.bbara.purefin.core.data.room.dao.EpisodeDao
import hu.bbara.purefin.core.data.room.dao.MovieDao
import hu.bbara.purefin.core.data.room.dao.SeasonDao
import hu.bbara.purefin.core.data.room.dao.SeriesDao
import hu.bbara.purefin.core.data.room.offline.OfflineMediaDatabase
import hu.bbara.purefin.core.data.room.offline.OfflineRoomMediaLocalDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaDatabaseModule {

    // Offline Database and DAOs
    @Provides
    @Singleton
    fun provideOfflineDatabase(@ApplicationContext context: Context): OfflineMediaDatabase =
        Room.databaseBuilder(context, OfflineMediaDatabase::class.java, "offline_media_database")
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
    @Singleton
    fun provideOfflineDataSource(
        database: OfflineMediaDatabase,
        movieDao: MovieDao,
        seriesDao: SeriesDao,
        seasonDao: SeasonDao,
        episodeDao: EpisodeDao
    ): OfflineRoomMediaLocalDataSource = OfflineRoomMediaLocalDataSource(
        database, movieDao, seriesDao, seasonDao, episodeDao
    )
}