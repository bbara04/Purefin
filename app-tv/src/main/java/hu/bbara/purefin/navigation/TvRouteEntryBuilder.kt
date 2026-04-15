package hu.bbara.purefin.navigation

import androidx.navigation3.runtime.EntryProviderScope
import hu.bbara.purefin.ui.screen.episode.TvEpisodeScreen
import hu.bbara.purefin.ui.screen.movie.TvMovieScreen
import hu.bbara.purefin.ui.screen.series.TvSeriesScreen
import hu.bbara.purefin.core.navigation.Route
import hu.bbara.purefin.ui.screen.login.LoginScreen
import hu.bbara.purefin.ui.screen.TvAppScreen
import hu.bbara.purefin.ui.screen.library.TvLibraryScreen
import hu.bbara.purefin.ui.screen.player.TvPlayerScreen

fun EntryProviderScope<Route>.tvHomeSection() {
    entry<Route.Home> {
        TvAppScreen()
    }
}

fun EntryProviderScope<Route>.tvLoginSection() {
    entry<Route.LoginRoute> {
        LoginScreen()
    }
}

fun EntryProviderScope<Route>.tvMovieSection() {
    entry<Route.MovieRoute> { route ->
        TvMovieScreen(movie = route.item)
    }
}

fun EntryProviderScope<Route>.tvSeriesSection() {
    entry<Route.SeriesRoute> { route ->
        TvSeriesScreen(series = route.item)
    }
}

fun EntryProviderScope<Route>.tvEpisodeSection() {
    entry<Route.EpisodeRoute> { route ->
        TvEpisodeScreen(episode = route.item)
    }
}

fun EntryProviderScope<Route>.tvPlayerSection() {
    entry<Route.PlayerRoute> { route ->
        val navigationManager = LocalNavigationManager.current
        TvPlayerScreen(
            mediaId = route.mediaId,
            onBack = { navigationManager.pop() }
        )
    }
}

fun EntryProviderScope<Route>.tvLibrarySection() {
    entry<Route.LibraryRoute> { route ->
        TvLibraryScreen(library = route.library)
    }
}
