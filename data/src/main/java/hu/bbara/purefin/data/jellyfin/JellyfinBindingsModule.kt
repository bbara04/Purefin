package hu.bbara.purefin.data.jellyfin

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bbara.purefin.data.AuthenticationRepository
import hu.bbara.purefin.data.DownloadMediaSourceResolver
import hu.bbara.purefin.data.NetworkMonitor
import hu.bbara.purefin.data.PlayableMediaRepository
import hu.bbara.purefin.data.PlaybackProgressReporter
import hu.bbara.purefin.data.SessionBootstrapper
import hu.bbara.purefin.data.jellyfin.download.JellyfinDownloadMediaSourceResolver
import hu.bbara.purefin.data.jellyfin.session.JellyfinAuthenticationRepository
import hu.bbara.purefin.data.jellyfin.session.JellyfinSessionBootstrapper
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class JellyfinBindingsModule {

    @Binds
    @Singleton
    abstract fun bindSessionBootstrapper(impl: JellyfinSessionBootstrapper): SessionBootstrapper

    @Binds
    @Singleton
    abstract fun bindAuthenticationRepository(impl: JellyfinAuthenticationRepository): AuthenticationRepository

    @Binds
    @Singleton
    abstract fun bindDownloadMediaSourceResolver(impl: JellyfinDownloadMediaSourceResolver): DownloadMediaSourceResolver

    @Binds
    abstract fun bindPlayableMediaRepository(impl: DefaultPlayableMediaRepository): PlayableMediaRepository

    @Binds
    abstract fun bindPlaybackProgressReporter(impl: JellyfinPlaybackProgressReporter): PlaybackProgressReporter

    @Binds
    @Singleton
    abstract fun bindNetworkMonitor(impl: ConnectivityNetworkMonitor): NetworkMonitor
}
