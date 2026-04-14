package hu.bbara.purefin.core.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class MediaRepositoryModule {

    @Binds
    abstract fun bindHomeRepository(impl: InMemoryAppContentRepository): HomeRepository

    @Binds
    abstract fun bindMediaCatalogReader(impl: CompositeMediaRepository): MediaCatalogReader

    @Binds
    abstract fun bindMediaProgressWriter(impl: CompositeMediaRepository): MediaProgressWriter

    @Binds
    abstract fun bindOfflineCatalogReader(impl: OfflineMediaRepository): OfflineCatalogReader

    @Binds
    abstract fun bindOfflineCatalogStore(impl: RoomOfflineCatalogStore): OfflineCatalogStore

    @Binds
    abstract fun bindSmartDownloadStore(impl: RoomSmartDownloadStore): SmartDownloadStore

    @Binds
    abstract fun bindPlayableMediaRepository(impl: DefaultPlayableMediaRepository): PlayableMediaRepository

    @Binds
    abstract fun bindEpisodeSeriesLookup(impl: DefaultEpisodeSeriesLookup): EpisodeSeriesLookup

    @Binds
    abstract fun bindPlaybackProgressReporter(impl: JellyfinPlaybackProgressReporter): PlaybackProgressReporter

}
