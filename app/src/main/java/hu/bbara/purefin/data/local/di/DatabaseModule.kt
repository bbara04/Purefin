package hu.bbara.purefin.data.local.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import hu.bbara.purefin.data.local.dao.EpisodeDao
import hu.bbara.purefin.data.local.dao.SeasonDao
import hu.bbara.purefin.data.local.dao.SeriesDao
import hu.bbara.purefin.data.local.db.PurefinDatabase
import hu.bbara.purefin.data.local.repository.LocalMediaRepository
import hu.bbara.purefin.data.local.repository.RoomLocalMediaRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): PurefinDatabase {
        return Room.databaseBuilder(
            context,
            PurefinDatabase::class.java,
            "purefin.db"
        ).build()
    }

    @Provides
    fun provideSeriesDao(database: PurefinDatabase): SeriesDao = database.seriesDao()

    @Provides
    fun provideSeasonDao(database: PurefinDatabase): SeasonDao = database.seasonDao()

    @Provides
    fun provideEpisodeDao(database: PurefinDatabase): EpisodeDao = database.episodeDao()

    @Provides
    @Singleton
    fun provideLocalMediaRepository(
        seriesDao: SeriesDao,
        seasonDao: SeasonDao,
        episodeDao: EpisodeDao
    ): LocalMediaRepository = RoomLocalMediaRepository(seriesDao, seasonDao, episodeDao)
}
