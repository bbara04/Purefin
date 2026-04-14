package hu.bbara.purefin.ui.screen.home.components

import java.util.UUID

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
