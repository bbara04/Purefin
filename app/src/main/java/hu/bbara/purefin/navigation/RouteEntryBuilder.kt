package hu.bbara.purefin.navigation

import androidx.navigation3.runtime.EntryProviderScope
import hu.bbara.purefin.app.content.movie.MovieScreen
import hu.bbara.purefin.app.home.HomePage

fun EntryProviderScope<Route>.appRouteEntryBuilder() {
    entry<Route.Home> {
        HomePage()
    }
    entry<Route.Movie> {
        MovieScreen(movieId = it.movieId)
    }
}