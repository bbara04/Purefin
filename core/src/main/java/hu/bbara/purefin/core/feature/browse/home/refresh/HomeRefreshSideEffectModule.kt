package hu.bbara.purefin.core.feature.browse.home.refresh

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class HomeRefreshSideEffectModule {

    @Binds
    @IntoSet
    abstract fun bindSyncSmartDownloadsHomeRefreshSideEffect(
        impl: SyncSmartDownloadsHomeRefreshSideEffect
    ): HomeRefreshSideEffect
}
