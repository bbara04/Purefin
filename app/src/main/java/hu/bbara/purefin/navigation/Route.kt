package hu.bbara.purefin.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Route : NavKey {
    @Serializable
    data object Home: Route

    @Serializable
    data class Movie(val item : ItemDto) : Route

    @Serializable
    data class Series(val item : ItemDto) : Route

    @Serializable
    data class Episode(val item : ItemDto) : Route
}
