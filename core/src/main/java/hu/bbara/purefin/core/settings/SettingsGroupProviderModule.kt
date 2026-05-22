package hu.bbara.purefin.core.settings

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import hu.bbara.purefin.core.feature.settings.HomeLibrarySettingsProvider
import hu.bbara.purefin.core.feature.settings.LogoutSettingsProvider
import hu.bbara.purefin.core.feature.update.AppUpdateController

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsGroupProviderModule {

    @Binds
    @IntoSet
    abstract fun bindAppUpdateSettingsProvider(impl: AppUpdateController): SettingsGroupProvider

    @Binds
    @IntoSet
    abstract fun bindHomeLibrarySettingsProvider(impl: HomeLibrarySettingsProvider): SettingsGroupProvider

    @Binds
    @IntoSet
    abstract fun bindLogoutSettingsProvider(impl: LogoutSettingsProvider): SettingsGroupProvider
}
