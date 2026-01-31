package hu.bbara.purefin.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Route : NavKey {
    @Serializable
    data object Home: Route

    @Serializable
    data class MovieRoute(val item : MovieDto) : Route

    @Serializable
    data class SeriesRoute(val item : SeriesDto) : Route

    @Serializable
    data class EpisodeRoute(val item : EpisodeDto) : Route

    @Serializable
    data class LibraryRoute(val library : LibraryDto) : Route

    @Serializable
    data object LoginRoute : Route
}
