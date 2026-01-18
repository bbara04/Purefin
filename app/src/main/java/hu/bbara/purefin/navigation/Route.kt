package hu.bbara.purefin.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Route : NavKey {
    @Serializable
    data object Home: Route

    @Serializable
    data class Movie(val movieId: String) : Route

    @Serializable
    data class Episode(val seriesId: String) : Route
}
