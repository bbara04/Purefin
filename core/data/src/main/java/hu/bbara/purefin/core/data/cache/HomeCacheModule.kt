package hu.bbara.purefin.core.data.cache

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
class HomeCacheModule {

    @Provides
    @Singleton
    fun provideHomeCacheDataStore(
        @ApplicationContext context: Context
    ): DataStore<HomeCache> {
        return DataStoreFactory.create(
            serializer = HomeCacheSerializer,
            produceFile = { context.dataStoreFile("home_cache.json") },
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { HomeCacheSerializer.defaultValue }
            )
        )
    }
}
