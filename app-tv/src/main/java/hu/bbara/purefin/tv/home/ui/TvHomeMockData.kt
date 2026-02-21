package hu.bbara.purefin.tv.home.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tv
import org.jellyfin.sdk.model.UUID

object TvHomeMockData {
    val user = TvHomeUser(name = "Alex User", plan = "Premium Account")

    val primaryNavItems = listOf(
        TvHomeNavItem(id = UUID.randomUUID(), label = "Home", icon = Icons.Outlined.Home, selected = true),
        TvHomeNavItem(id = UUID.randomUUID(), label = "Movies", icon = Icons.Outlined.Movie),
        TvHomeNavItem(id = UUID.randomUUID(), label = "TV Shows", icon = Icons.Outlined.Tv),
        TvHomeNavItem(id = UUID.randomUUID(), label = "Search", icon = Icons.Outlined.Search)
    )

    val secondaryNavItems = listOf(
        TvHomeNavItem(id = UUID.randomUUID(), label = "Settings", icon = Icons.Outlined.Settings)
    )
}
