package hu.bbara.purefin.data.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bbara.purefin.Offline
import hu.bbara.purefin.Online
import hu.bbara.purefin.data.HomeRepository
import hu.bbara.purefin.data.LocalMediaRepository
import hu.bbara.purefin.data.SearchManager
import hu.bbara.purefin.data.catalog.InMemoryAppContentRepository
import hu.bbara.purefin.data.catalog.InMemoryLocalMediaRepository
import hu.bbara.purefin.data.catalog.OfflineLocalMediaRepository
import hu.bbara.purefin.data.jellyfin.JellyfinMediaMetadataUpdaterImpl
import hu.bbara.purefin.data.jellyfin.SearchManagerImpl
import hu.bbara.purefin.jellyfin.JellyfinMediaMetadataUpdater

@Module
@InstallIn(SingletonComponent::class)
abstract class MediaRepositoryModule {

    @Binds
    @Online
    abstract fun bindOnlineRepository(impl: InMemoryLocalMediaRepository): LocalMediaRepository

    @Binds
    @Offline
    abstract fun bindOfflineRepository(impl: OfflineLocalMediaRepository): LocalMediaRepository

    @Binds
    abstract fun bindHomeRepository(impl: InMemoryAppContentRepository): HomeRepository

    @Binds
    abstract fun bindJellyfinMediaMetadataUpdater(impl: JellyfinMediaMetadataUpdaterImpl): JellyfinMediaMetadataUpdater

    @Binds
    abstract fun bindSearchManager(impl: SearchManagerImpl): SearchManager
}