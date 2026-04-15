package hu.bbara.purefin.data.offline

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bbara.purefin.core.data.OfflineCatalogStore
import hu.bbara.purefin.core.data.SmartDownloadStore

@Module
@InstallIn(SingletonComponent::class)
abstract class OfflineBindingsModule {

    @Binds
    abstract fun bindOfflineCatalogStore(impl: RoomOfflineCatalogStore): OfflineCatalogStore

    @Binds
    abstract fun bindSmartDownloadStore(impl: RoomSmartDownloadStore): SmartDownloadStore
}
