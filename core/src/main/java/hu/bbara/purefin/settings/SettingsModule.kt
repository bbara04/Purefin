package hu.bbara.purefin.settings

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
import hu.bbara.purefin.model.Settings
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SettingsModule {

    @Provides
    @Singleton
    fun provideSettingsDataStore(
        @ApplicationContext context: Context
    ): DataStore<Settings> {
        return DataStoreFactory.create(
            serializer = SettingsSerializer,
            produceFile = { context.dataStoreFile("settings.json") },
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { SettingsSerializer.defaultValue }
            )
        )
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        settingsDataStore: DataStore<Settings>
    ): SettingsRepository {
        return SettingsRepository(settingsDataStore)
    }
}
