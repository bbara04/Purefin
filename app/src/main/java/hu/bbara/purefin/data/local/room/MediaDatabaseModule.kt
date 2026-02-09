package hu.bbara.purefin.data.local.room

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaDatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MediaDatabase =
        Room.databaseBuilder(context, MediaDatabase::class.java, "media_database")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideMovieDao(db: MediaDatabase) = db.movieDao()

    @Provides
    fun provideSeriesDao(db: MediaDatabase) = db.seriesDao()

    @Provides
    fun provideSeasonDao(db: MediaDatabase) = db.seasonDao()

    @Provides
    fun provideEpisodeDao(db: MediaDatabase) = db.episodeDao()

    @Provides
    fun provideCastMemberDao(db: MediaDatabase) = db.castMemberDao()
}
