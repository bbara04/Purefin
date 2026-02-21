package hu.bbara.purefin.tv.home.ui

import androidx.compose.ui.graphics.vector.ImageVector
import org.jellyfin.sdk.model.UUID

data class TvHomeNavItem(
    val id: UUID,
    val label: String,
    val icon: ImageVector,
    val selected: Boolean = false
)

data class TvHomeUser(
    val name: String,
    val plan: String
)
