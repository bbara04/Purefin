package hu.bbara.purefin.navigation

import androidx.navigation3.runtime.EntryProviderScope
import hu.bbara.purefin.app.content.episode.EpisodeScreen
import hu.bbara.purefin.app.content.movie.MovieScreen
import hu.bbara.purefin.app.content.series.SeriesScreen
import hu.bbara.purefin.app.home.HomePage
import hu.bbara.purefin.app.library.ui.LibraryScreen
import hu.bbara.purefin.core.data.navigation.Route
import hu.bbara.purefin.login.ui.LoginScreen

fun EntryProviderScope<Route>.appRouteEntryBuilder() {
    entry<Route.Home> {
        HomePage()
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
