package hu.bbara.purefin.session

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
class UserSessionModule {

    @Provides
    @Singleton
    fun provideUserProfileDataStore(
        @ApplicationContext context: Context
    ): DataStore<UserSession> {
        return DataStoreFactory.create(
            serializer = UserSessionSerializer,
            produceFile = { context.dataStoreFile("user_session.json") },
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { UserSessionSerializer.defaultValue }
            )
        )
    }

    @Provides
    @Singleton
    fun provideUserSessionRepository(
        userSessionDataStore: DataStore<UserSession>
    ): UserSessionRepository {
        return UserSessionRepository(userSessionDataStore)
    }
}