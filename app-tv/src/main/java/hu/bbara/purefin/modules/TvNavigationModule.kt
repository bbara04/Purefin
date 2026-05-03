package hu.bbara.purefin.modules

import androidx.navigation3.runtime.EntryProviderScope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet
import hu.bbara.purefin.navigation.Route
import hu.bbara.purefin.navigation.tvEpisodeSection
import hu.bbara.purefin.navigation.tvHomeSection
import hu.bbara.purefin.navigation.tvLibrarySection
import hu.bbara.purefin.navigation.tvLoginSection
import hu.bbara.purefin.navigation.tvMovieSection
import hu.bbara.purefin.navigation.tvPlayerSection
import hu.bbara.purefin.navigation.tvSettingsSection
import hu.bbara.purefin.navigation.tvSeriesSection

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

    @IntoSet
    @Provides
    fun provideTvPlayerEntryBuilder(): EntryProviderScope<Route>.() -> Unit = {
        tvPlayerSection()
    }

    @IntoSet
    @Provides
    fun provideTvLibraryEntryBuilder(): EntryProviderScope<Route>.() -> Unit = {
        tvLibrarySection()
    }

    @IntoSet
    @Provides
    fun provideTvSettingsEntryBuilder(): EntryProviderScope<Route>.() -> Unit = {
        tvSettingsSection()
    }
}
