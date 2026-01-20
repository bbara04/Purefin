package hu.bbara.purefin.app.home.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import org.jellyfin.sdk.model.UUID
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.CollectionType

data class ContinueWatchingItem(
    val id: UUID,
    val type: BaseItemKind,
    val primaryText: String,
    val secondaryText: String,
    val progress: Double,
    val colors: List<Color>
)

data class LibraryItem(
    val id: UUID,
    val name: String,
    val type: CollectionType,
    val isEmpty: Boolean
)

data class PosterItem(
    val id: UUID,
    val title: String,
    val type: BaseItemKind,
    val parentId: UUID? = null
) {
    val imageItemId: UUID get() = parentId ?: id
}

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
