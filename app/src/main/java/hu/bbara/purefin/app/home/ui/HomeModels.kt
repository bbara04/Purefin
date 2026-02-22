package hu.bbara.purefin.app.home.ui

import org.jellyfin.sdk.model.UUID

data class HomeNavItem(
    val id: UUID,
    val label: String,
    val posterUrl: String,
    val selected: Boolean = false
)

data class HomeUser(
    val name: String,
    val plan: String
)
