package hu.bbara.purefin.data.logging

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import hu.bbara.purefin.core.settings.SettingsGroupProvider

@Module
@InstallIn(SingletonComponent::class)
abstract class LogUploadSettingsModule {

    @Binds
    @IntoSet
    abstract fun bindLogUploadSettingsProvider(impl: LogUploadSettingsProvider): SettingsGroupProvider
}
