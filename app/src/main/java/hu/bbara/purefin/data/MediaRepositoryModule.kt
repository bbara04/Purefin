package hu.bbara.purefin.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bbara.purefin.data.local.room.OnlineRepository
import hu.bbara.purefin.data.local.room.OfflineRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class MediaRepositoryModule {

    @Binds
    @OnlineRepository
    abstract fun bindOnlineMediaRepository(impl: InMemoryMediaRepository): MediaRepository

    @Binds
    @OfflineRepository
    abstract fun bindOfflineMediaRepository(impl: OfflineMediaRepository): MediaRepository

    // Default binding delegates to online/offline based on user preference
    @Binds
    abstract fun bindDefaultMediaRepository(impl: ActiveMediaRepository): MediaRepository
}
