package hu.bbara.purefin.navigation

import androidx.navigation3.runtime.EntryProviderScope
import hu.bbara.purefin.feature.shared.navigation.Route
import hu.bbara.purefin.ui.screen.library.LibraryScreen
import hu.bbara.purefin.ui.screen.login.LoginScreen
import hu.bbara.purefin.ui.screen.AppScreen
import hu.bbara.purefin.ui.screen.episode.EpisodeScreen
import hu.bbara.purefin.ui.screen.movie.MovieScreen
import hu.bbara.purefin.ui.screen.series.SeriesScreen

fun EntryProviderScope<Route>.appRouteEntryBuilder() {
    entry<Route.Home> {
        AppScreen()
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
