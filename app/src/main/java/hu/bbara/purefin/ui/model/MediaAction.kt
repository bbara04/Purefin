package hu.bbara.purefin.ui.model

data class MediaAction(
    val name: String,
    val onClick: () -> Unit
)