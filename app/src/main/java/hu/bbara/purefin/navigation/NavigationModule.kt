package hu.bbara.purefin.navigation

import androidx.navigation3.runtime.EntryProviderScope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet
import hu.bbara.purefin.core.data.navigation.Route

@Module
@InstallIn(ActivityRetainedComponent::class)
object NavigationModule {

    @IntoSet
    @Provides
    fun provideAppEntryBuilder(): EntryProviderScope<Route>.() -> Unit = {
        appRouteEntryBuilder()
    }
}
