package hu.bbara.purefin.data.catalog

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bbara.purefin.core.data.EpisodeSeriesLookup
import hu.bbara.purefin.core.data.HomeRepository
import hu.bbara.purefin.core.data.MediaCatalogReader
import hu.bbara.purefin.core.data.MediaProgressWriter
import hu.bbara.purefin.core.data.OfflineCatalogReader

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
    abstract fun bindEpisodeSeriesLookup(impl: DefaultEpisodeSeriesLookup): EpisodeSeriesLookup
}
