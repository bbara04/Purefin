package hu.bbara.purefin.core.download

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bbara.purefin.core.download.MediaDownloadController
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DownloadControllerModule {

    @Binds
    @Singleton
    abstract fun bindMediaDownloadController(
        impl: MediaDownloadManager
    ): MediaDownloadController
}
