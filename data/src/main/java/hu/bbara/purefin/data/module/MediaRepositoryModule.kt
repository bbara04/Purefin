package hu.bbara.purefin.data.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bbara.purefin.Offline
import hu.bbara.purefin.Online
import hu.bbara.purefin.data.HomeRepository
import hu.bbara.purefin.data.MediaRepository
import hu.bbara.purefin.data.catalog.InMemoryAppContentRepository
import hu.bbara.purefin.data.catalog.InMemoryMediaRepository
import hu.bbara.purefin.data.catalog.OfflineMediaRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class MediaRepositoryModule {

    @Binds
    @Online
    abstract fun bindOnlineRepository(impl: InMemoryMediaRepository): MediaRepository

    @Binds
    @Offline
    abstract fun bindOfflineRepository(impl: OfflineMediaRepository): MediaRepository

    @Binds
    abstract fun bindHomeRepository(impl: InMemoryAppContentRepository): HomeRepository
}