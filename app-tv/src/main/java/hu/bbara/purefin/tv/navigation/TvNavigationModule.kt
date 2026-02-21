package hu.bbara.purefin.tv.navigation

import androidx.navigation3.runtime.EntryProviderScope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet
import hu.bbara.purefin.core.data.navigation.Route

@Module
@InstallIn(ActivityRetainedComponent::class)
object TvNavigationModule {

    @IntoSet
    @Provides
    fun provideTvHomeEntryBuilder(): EntryProviderScope<Route>.() -> Unit = {
        tvHomeSection()
    }

    @IntoSet
    @Provides
    fun provideTvLoginEntryBuilder(): EntryProviderScope<Route>.() -> Unit = {
        tvLoginSection()
    }

    @IntoSet
    @Provides
    fun provideTvMovieEntryBuilder(): EntryProviderScope<Route>.() -> Unit = {
        tvMovieSection()
    }

    @IntoSet
    @Provides
    fun provideTvSeriesEntryBuilder(): EntryProviderScope<Route>.() -> Unit = {
        tvSeriesSection()
    }

    @IntoSet
    @Provides
    fun provideTvEpisodeEntryBuilder(): EntryProviderScope<Route>.() -> Unit = {
        tvEpisodeSection()
    }
}
