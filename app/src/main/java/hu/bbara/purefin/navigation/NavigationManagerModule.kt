package hu.bbara.purefin.navigation

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NavigationManagerModule {
    @Provides
    @Singleton
    fun provideNavigationManager(): NavigationManager = DefaultNavigationManager()
}
