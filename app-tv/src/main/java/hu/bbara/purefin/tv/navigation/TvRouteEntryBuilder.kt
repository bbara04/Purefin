package hu.bbara.purefin.tv.navigation

import androidx.navigation3.runtime.EntryProviderScope
import hu.bbara.purefin.app.content.episode.EpisodeScreen
import hu.bbara.purefin.app.content.movie.MovieScreen
import hu.bbara.purefin.app.content.series.SeriesScreen
import hu.bbara.purefin.core.data.navigation.Route
import hu.bbara.purefin.login.ui.LoginScreen
import hu.bbara.purefin.tv.home.TvHomePage

fun EntryProviderScope<Route>.tvHomeSection() {
    entry<Route.Home> {
        TvHomePage()
    }
}

fun EntryProviderScope<Route>.tvLoginSection() {
    entry<Route.LoginRoute> {
        LoginScreen()
    }
}

fun EntryProviderScope<Route>.tvMovieSection() {
    entry<Route.MovieRoute> { route ->
        MovieScreen(movie = route.item)
    }
}

fun EntryProviderScope<Route>.tvSeriesSection() {
    entry<Route.SeriesRoute> { route ->
        SeriesScreen(series = route.item)
    }
}

fun EntryProviderScope<Route>.tvEpisodeSection() {
    entry<Route.EpisodeRoute> { route ->
        EpisodeScreen(episode = route.item)
    }
}
