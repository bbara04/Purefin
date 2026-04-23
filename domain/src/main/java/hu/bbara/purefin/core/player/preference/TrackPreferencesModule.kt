package hu.bbara.purefin.core.player.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class TrackPreferencesModule {

    @Provides
    @Singleton
    fun provideTrackPreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<TrackPreferences> {
        return DataStoreFactory.create(
            serializer = TrackPreferencesSerializer,
            produceFile = { context.dataStoreFile("track_preferences.json") },
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { TrackPreferencesSerializer.defaultValue }
            )
        )
    }

    @Provides
    @Singleton
    fun provideTrackPreferencesRepository(
        trackPreferencesDataStore: DataStore<TrackPreferences>
    ): TrackPreferencesRepository {
        return TrackPreferencesRepository(trackPreferencesDataStore)
    }
}
