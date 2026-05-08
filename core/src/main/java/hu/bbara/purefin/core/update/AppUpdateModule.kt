package hu.bbara.purefin.core.update

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import hu.bbara.purefin.core.feature.update.AppUpdateController
import hu.bbara.purefin.core.feature.update.AppUpdateInstaller
import hu.bbara.purefin.core.feature.update.AppVersionProvider
import hu.bbara.purefin.core.settings.SettingsGroupProvider

@Module
@InstallIn(SingletonComponent::class)
abstract class AppUpdateModule {

    @Binds
    abstract fun bindAppUpdateInstaller(impl: AndroidAppUpdateInstaller): AppUpdateInstaller

    @Binds
    abstract fun bindAppVersionProvider(impl: AndroidAppVersionProvider): AppVersionProvider

    @Binds
    @IntoSet
    abstract fun bindAppUpdateSettingsProvider(impl: AppUpdateController): SettingsGroupProvider
}
