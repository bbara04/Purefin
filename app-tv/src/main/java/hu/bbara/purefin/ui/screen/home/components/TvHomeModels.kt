package hu.bbara.purefin.ui.screen.home.components

import androidx.compose.ui.graphics.vector.ImageVector
import hu.bbara.purefin.navigation.Route

data class TvDrawerDestinationItem(
    val destination: Route,
    val label: String,
    val icon: ImageVector
)
