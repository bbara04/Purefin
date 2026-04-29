package hu.bbara.purefin.ui.model

import hu.bbara.purefin.model.LibraryKind
import java.util.UUID

data class LibraryUiModel(
    val id: UUID,
    val name: String,
    val type: LibraryKind,
    val posterUrl: String,
    val isEmpty: Boolean
)