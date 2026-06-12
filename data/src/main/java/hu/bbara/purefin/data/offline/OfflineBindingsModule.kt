package hu.bbara.purefin.data.offline

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bbara.purefin.core.data.OfflineMediaManager
import hu.bbara.purefin.core.data.SmartDownloadStore
import hu.bbara.purefin.data.catalog.OfflineLocalMediaRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class OfflineBindingsModule {

    @Binds
    abstract fun bindOfflineMediaManager(impl: OfflineLocalMediaRepository): OfflineMediaManager

    @Binds
    abstract fun bindSmartDownloadStore(impl: RoomSmartDownloadStore): SmartDownloadStore
}
