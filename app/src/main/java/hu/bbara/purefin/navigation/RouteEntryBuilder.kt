package hu.bbara.purefin.navigation

import androidx.navigation3.runtime.EntryProviderScope
import hu.bbara.purefin.app.content.episode.EpisodeScreen
import hu.bbara.purefin.app.content.movie.MovieScreen
import hu.bbara.purefin.app.content.series.SeriesScreen
import hu.bbara.purefin.app.home.HomePage
import hu.bbara.purefin.app.library.ui.LibraryScreen
import hu.bbara.purefin.login.ui.LoginScreen

fun EntryProviderScope<Route>.appRouteEntryBuilder() {
    entry<Route.Home> {
        HomePage()
    }
    entry<Route.Movie> {
        MovieScreen(movie = it.item)
    }
    entry<Route.Series> {
        SeriesScreen(series = it.item)
    }
    entry<Route.Episode> {
        EpisodeScreen(episode = it.item)
    }
    entry<Route.Library> {
        LibraryScreen(library = it.library)
    }
    entry<Route.Login> {
        LoginScreen()
    }
}
