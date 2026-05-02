package hu.bbara.purefin.navigation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import hu.bbara.purefin.ui.screen.library.LibraryScreen
import hu.bbara.purefin.ui.screen.login.LoginScreen
import hu.bbara.purefin.ui.screen.AppScreen
import hu.bbara.purefin.ui.screen.episode.EpisodeScreen
import hu.bbara.purefin.ui.screen.home.components.search.HomeSearchFullScreen
import hu.bbara.purefin.ui.screen.movie.MovieScreen
import hu.bbara.purefin.ui.screen.series.SeriesScreen

fun EntryProviderScope<Route>.appRouteEntryBuilder() {
    entry<Route.Home> {
        CompositionLocalProvider(
            LocalNavSharedAnimatedVisibilityScope provides LocalNavAnimatedContentScope.current
        ) {
            AppScreen()
        }
    }
    entry<Route.HomeSearchRoute> {
        CompositionLocalProvider(
            LocalNavSharedAnimatedVisibilityScope provides LocalNavAnimatedContentScope.current
        ) {
            HomeSearchFullScreen()
        }
    }
    entry<Route.MovieRoute> {
        MovieScreen(movie = it.item)
    }
    entry<Route.SeriesRoute> {
        SeriesScreen(series = it.item)
    }
    entry<Route.EpisodeRoute> {
        EpisodeScreen(episode = it.item)
    }
    entry<Route.LibraryRoute> {
        LibraryScreen(library = it.library)
    }
    entry<Route.LoginRoute> {
        LoginScreen()
    }
}
