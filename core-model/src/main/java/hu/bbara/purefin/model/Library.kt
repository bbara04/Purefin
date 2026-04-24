package hu.bbara.purefin.model

import java.util.UUID

data class Library(
    val id: UUID,
    val name: String,
    val type: LibraryKind,
    val posterUrl: String,
    val series: List<Series>? = null,
    val movies: List<Movie>? = null,
) {
    init {
        require(series != null || movies != null) { "Either series or movie must be provided" }
        require(series == null || movies == null) { "Only one of series or movie can be provided" }
        require(type == LibraryKind.SERIES || type == LibraryKind.MOVIES) { "Invalid type: $type" }
    }
}
