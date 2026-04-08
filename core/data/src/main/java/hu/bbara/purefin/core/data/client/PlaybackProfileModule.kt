package hu.bbara.purefin.core.data.client

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlaybackProfileModule {

    @Provides
    @Singleton
    fun provideDeviceProfileCapabilities(): DeviceProfileCapabilities = AndroidDeviceProfileCapabilities()

    @Provides
    @Singleton
    fun providePlaybackProfilePolicy(
        family: PlaybackProfileFamily,
        capabilities: DeviceProfileCapabilities,
    ): PlaybackProfilePolicy = when (family) {
        PlaybackProfileFamily.MOBILE -> MobilePlaybackProfilePolicy(capabilities)
        PlaybackProfileFamily.TV -> TvPlaybackProfilePolicy(capabilities)
    }
}
