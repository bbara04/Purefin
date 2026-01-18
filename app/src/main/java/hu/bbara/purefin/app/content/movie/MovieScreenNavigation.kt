package hu.bbara.purefin.app.content.movie

import androidx.navigation3.runtime.EntryProviderScope
import hu.bbara.purefin.navigation.Route

/**
 * Navigation 3 entry definition for the Home section.
 */
fun EntryProviderScope<Route>.homeSection() {
    entry<Route.Movie> {
        MovieScreen(movieId = it.movieId)
    }
}
