package hu.bbara.purefin.ui.screen.home.components

import androidx.compose.ui.graphics.vector.ImageVector

enum class TvDrawerDestination {
    HOME,
    LIBRARIES
}

data class TvDrawerDestinationItem(
    val destination: TvDrawerDestination,
    val label: String,
    val icon: ImageVector,
    val selected: Boolean = false
)
