package hu.bbara.purefin.app.home.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tv

object HomeMockData {
    val user = HomeUser(name = "Alex User", plan = "Premium Account")

    val primaryNavItems = listOf(
        HomeNavItem(label = "Home", icon = Icons.Outlined.Home, selected = true),
        HomeNavItem(label = "Movies", icon = Icons.Outlined.Movie),
        HomeNavItem(label = "TV Shows", icon = Icons.Outlined.Tv),
        HomeNavItem(label = "Search", icon = Icons.Outlined.Search)
    )

    val secondaryNavItems = listOf(
        HomeNavItem(label = "Settings", icon = Icons.Outlined.Settings)
    )

}
