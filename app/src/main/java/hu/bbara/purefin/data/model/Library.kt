package hu.bbara.purefin.data.model

import org.jellyfin.sdk.model.api.CollectionType
import java.util.UUID

data class Library(
    val id: UUID,
    val name: String,
    val type: CollectionType,
    val series: List<Series>? = null,
    val movies: List<Movie>? = null,
) {
    init {
        require(series != null || movies != null) { "Either series or movie must be provided" }
        require(series == null || movies == null) { "Only one of series or movie can be provided" }
        require(type == CollectionType.TVSHOWS || type == CollectionType.MOVIES) { "Invalid type: $type" }
    }
}
