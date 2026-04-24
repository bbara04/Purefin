package hu.bbara.purefin.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bbara.purefin.data.CompositeMediaRepository
import hu.bbara.purefin.data.HomeRepository
import hu.bbara.purefin.data.MediaCatalogReader
import hu.bbara.purefin.data.MediaProgressWriter
import hu.bbara.purefin.data.catalog.InMemoryAppContentRepository


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindHomeRepository(impl: InMemoryAppContentRepository): HomeRepository

    @Binds
    abstract fun bindMediaCatalogReader(impl: CompositeMediaRepository): MediaCatalogReader

    @Binds
    abstract fun bindMediaProgressWrite(impl: CompositeMediaRepository): MediaProgressWriter
}