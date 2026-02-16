package hu.bbara.purefin.data.local.room

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import hu.bbara.purefin.data.local.room.dao.CastMemberDao
import hu.bbara.purefin.data.local.room.dao.EpisodeDao
import hu.bbara.purefin.data.local.room.dao.MovieDao
import hu.bbara.purefin.data.local.room.dao.SeasonDao
import hu.bbara.purefin.data.local.room.dao.SeriesDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaDatabaseModule {

    // Online Database and DAOs
    @Provides
    @Singleton
    @OnlineDatabase
    fun provideOnlineDatabase(@ApplicationContext context: Context): MediaDatabase =
        Room.inMemoryDatabaseBuilder(context, MediaDatabase::class.java)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @OnlineDatabase
    fun provideOnlineMovieDao(@OnlineDatabase db: MediaDatabase) = db.movieDao()

    @Provides
    @OnlineDatabase
    fun provideOnlineSeriesDao(@OnlineDatabase db: MediaDatabase) = db.seriesDao()

    @Provides
    @OnlineDatabase
    fun provideOnlineSeasonDao(@OnlineDatabase db: MediaDatabase) = db.seasonDao()

    @Provides
    @OnlineDatabase
    fun provideOnlineEpisodeDao(@OnlineDatabase db: MediaDatabase) = db.episodeDao()

    @Provides
    @OnlineDatabase
    fun provideOnlineCastMemberDao(@OnlineDatabase db: MediaDatabase) = db.castMemberDao()

    // Offline Database and DAOs
    @Provides
    @Singleton
    @OfflineDatabase
    fun provideOfflineDatabase(@ApplicationContext context: Context): OfflineMediaDatabase =
        Room.databaseBuilder(context, OfflineMediaDatabase::class.java, "offline_media_database")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @OfflineDatabase
    fun provideOfflineMovieDao(@OfflineDatabase db: OfflineMediaDatabase) = db.movieDao()

    @Provides
    @OfflineDatabase
    fun provideOfflineSeriesDao(@OfflineDatabase db: OfflineMediaDatabase) = db.seriesDao()

    @Provides
    @OfflineDatabase
    fun provideOfflineSeasonDao(@OfflineDatabase db: OfflineMediaDatabase) = db.seasonDao()

    @Provides
    @OfflineDatabase
    fun provideOfflineEpisodeDao(@OfflineDatabase db: OfflineMediaDatabase) = db.episodeDao()

    @Provides
    @OfflineDatabase
    fun provideOfflineCastMemberDao(@OfflineDatabase db: OfflineMediaDatabase) = db.castMemberDao()

    // Data Sources
    @Provides
    @Singleton
    @OnlineDatabase
    fun provideOnlineDataSource(
        @OnlineDatabase database: MediaDatabase,
        @OnlineDatabase movieDao: MovieDao,
        @OnlineDatabase seriesDao: SeriesDao,
        @OnlineDatabase seasonDao: SeasonDao,
        @OnlineDatabase episodeDao: EpisodeDao,
        @OnlineDatabase castMemberDao: CastMemberDao
    ): RoomMediaLocalDataSource = RoomMediaLocalDataSource(
        database, movieDao, seriesDao, seasonDao, episodeDao, castMemberDao
    )

    @Provides
    @Singleton
    @OfflineDatabase
    fun provideOfflineDataSource(
        @OfflineDatabase database: OfflineMediaDatabase,
        @OfflineDatabase movieDao: MovieDao,
        @OfflineDatabase seriesDao: SeriesDao,
        @OfflineDatabase seasonDao: SeasonDao,
        @OfflineDatabase episodeDao: EpisodeDao,
        @OfflineDatabase castMemberDao: CastMemberDao
    ): OfflineRoomMediaLocalDataSource = OfflineRoomMediaLocalDataSource(
        database, movieDao, seriesDao, seasonDao, episodeDao, castMemberDao
    )

    // Default (unqualified) data source for backward compatibility
    @Provides
    @Singleton
    fun provideDefaultDataSource(
        @OnlineDatabase dataSource: RoomMediaLocalDataSource
    ): RoomMediaLocalDataSource = dataSource
}
