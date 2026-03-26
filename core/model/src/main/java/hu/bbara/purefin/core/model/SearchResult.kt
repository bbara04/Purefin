package hu.bbara.purefin.core.model

import org.jellyfin.sdk.model.UUID
import org.jellyfin.sdk.model.api.BaseItemKind

data class SearchResult(
    val id: UUID,
    val title: String,
    val posterUrl: String,
    val type: BaseItemKind,
) {
    companion object {
        fun create(movie: Movie, imageUrl: String) : SearchResult {
            return SearchResult(
                id = movie.id,
                title = movie.title,
                posterUrl = imageUrl,
                type = BaseItemKind.MOVIE
            )
        }

        fun create(series: Series, imageUrl: String) : SearchResult {
            return SearchResult(
                id = series.id,
                title = series.name,
                posterUrl = imageUrl,
                type = BaseItemKind.MOVIE
            )
        }
    }
}
