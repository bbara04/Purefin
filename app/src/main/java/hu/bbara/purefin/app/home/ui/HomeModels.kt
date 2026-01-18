package hu.bbara.purefin.app.home.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import org.jellyfin.sdk.model.UUID
import org.jellyfin.sdk.model.api.BaseItemKind

data class ContinueWatchingItem(
    val id: UUID,
    val type: BaseItemKind,
    val primaryText: String,
    val secondaryText: String,
    val progress: Double,
    val colors: List<Color>
)

data class LibraryItem(
    val name: String,
    val id: UUID,
    val isEmpty: Boolean
)

data class PosterItem(
    val id: UUID,
    val title: String,
    val type: BaseItemKind
)

data class HomeNavItem(
    val label: String,
    val icon: ImageVector,
    val selected: Boolean = false
)

data class HomeUser(
    val name: String,
    val plan: String
)
