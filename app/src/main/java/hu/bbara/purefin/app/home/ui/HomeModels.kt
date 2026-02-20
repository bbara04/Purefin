package hu.bbara.purefin.app.home.ui

import androidx.compose.ui.graphics.vector.ImageVector
import org.jellyfin.sdk.model.UUID

data class HomeNavItem(
    val id: UUID,
    val label: String,
    val icon: ImageVector,
    val selected: Boolean = false
)

data class HomeUser(
    val name: String,
    val plan: String
)
