package hu.bbara.purefin.update

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bbara.purefin.feature.update.AppUpdateInstaller
import hu.bbara.purefin.feature.update.AppVersionProvider

@Module
@InstallIn(SingletonComponent::class)
abstract class AppUpdateModule {

    @Binds
    abstract fun bindAppUpdateInstaller(impl: AndroidAppUpdateInstaller): AppUpdateInstaller

    @Binds
    abstract fun bindAppVersionProvider(impl: BuildConfigAppVersionProvider): AppVersionProvider
}
