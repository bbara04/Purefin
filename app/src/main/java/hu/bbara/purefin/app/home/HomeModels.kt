package hu.bbara.purefin.app.home

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import org.jellyfin.sdk.model.UUID

data class ContinueWatchingItem(
    val id: UUID,
    val primaryText: String,
    val secondaryText: String,
    val progress: Float,
    val colors: List<Color>
)

data class LibraryItem(
    val name: String,
    val id: UUID
)

data class PosterItem(
    val id: UUID,
    val title: String,
    val isLatest: Boolean,
    val colors: List<Color>
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
