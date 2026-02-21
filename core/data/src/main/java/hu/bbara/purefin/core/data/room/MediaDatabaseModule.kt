package hu.bbara.purefin.core.data.room

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import hu.bbara.purefin.core.data.room.dao.CastMemberDao
import hu.bbara.purefin.core.data.room.dao.EpisodeDao
import hu.bbara.purefin.core.data.room.dao.LibraryDao
import hu.bbara.purefin.core.data.room.dao.MovieDao
import hu.bbara.purefin.core.data.room.dao.SeasonDao
import hu.bbara.purefin.core.data.room.dao.SeriesDao
import hu.bbara.purefin.core.data.room.local.MediaDatabase
import hu.bbara.purefin.core.data.room.local.RoomMediaLocalDataSource
import hu.bbara.purefin.core.data.room.offline.OfflineMediaDatabase
import hu.bbara.purefin.core.data.room.offline.OfflineRoomMediaLocalDataSource
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

    @Provides
    @OnlineDatabase
    fun provideOnlineLibraryDao(@OnlineDatabase db: MediaDatabase) = db.libraryDao()

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
        @OnlineDatabase castMemberDao: CastMemberDao,
        @OnlineDatabase libraryDao: LibraryDao
    ): RoomMediaLocalDataSource = RoomMediaLocalDataSource(
        database, movieDao, seriesDao, seasonDao, episodeDao, castMemberDao, libraryDao
    )

    @Provides
    @Singleton
    @OfflineDatabase
    fun provideOfflineDataSource(
        @OfflineDatabase database: OfflineMediaDatabase,
        @OfflineDatabase movieDao: MovieDao,
        @OfflineDatabase seriesDao: SeriesDao,
        @OfflineDatabase seasonDao: SeasonDao,
        @OfflineDatabase episodeDao: EpisodeDao
    ): OfflineRoomMediaLocalDataSource = OfflineRoomMediaLocalDataSource(
        database, movieDao, seriesDao, seasonDao, episodeDao
    )

    // Default (unqualified) data source for backward compatibility
    @Provides
    @Singleton
    fun provideDefaultDataSource(
        @OnlineDatabase dataSource: RoomMediaLocalDataSource
    ): RoomMediaLocalDataSource = dataSource
}