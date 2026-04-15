package hu.bbara.purefin.data.jellyfin

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bbara.purefin.core.data.AuthenticationRepository
import hu.bbara.purefin.core.data.DownloadMediaSourceResolver
import hu.bbara.purefin.core.data.NetworkMonitor
import hu.bbara.purefin.core.data.PlayableMediaRepository
import hu.bbara.purefin.core.data.PlaybackProgressReporter
import hu.bbara.purefin.core.data.SessionBootstrapper
import hu.bbara.purefin.data.jellyfin.client.JellyfinApiClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class JellyfinBindingsModule {

    @Binds
    @Singleton
    abstract fun bindSessionBootstrapper(impl: JellyfinApiClient): SessionBootstrapper

    @Binds
    @Singleton
    abstract fun bindAuthenticationRepository(impl: JellyfinApiClient): AuthenticationRepository

    @Binds
    @Singleton
    abstract fun bindDownloadMediaSourceResolver(impl: JellyfinApiClient): DownloadMediaSourceResolver

    @Binds
    abstract fun bindPlayableMediaRepository(impl: DefaultPlayableMediaRepository): PlayableMediaRepository

    @Binds
    abstract fun bindPlaybackProgressReporter(impl: JellyfinPlaybackProgressReporter): PlaybackProgressReporter

    @Binds
    @Singleton
    abstract fun bindNetworkMonitor(impl: ConnectivityNetworkMonitor): NetworkMonitor
}
