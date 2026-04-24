package hu.bbara.purefin.data.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bbara.purefin.data.OfflineCatalogReader
import hu.bbara.purefin.data.catalog.OfflineMediaRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class MediaRepositoryModule {

    @Binds
    abstract fun bindOfflineCatalogReader(impl: OfflineMediaRepository): OfflineCatalogReader
}