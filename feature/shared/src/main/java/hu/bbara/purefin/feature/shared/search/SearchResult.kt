package hu.bbara.purefin.feature.shared.search

import hu.bbara.purefin.core.model.MediaKind
import hu.bbara.purefin.core.model.Movie
import hu.bbara.purefin.core.model.Series
import java.util.UUID

data class SearchResult(
    val id: UUID,
    val title: String,
    val posterUrl: String,
    val type: MediaKind,
) {
    companion object {
        fun create(movie: Movie, imageUrl: String): SearchResult {
            return SearchResult(
                id = movie.id,
                title = movie.title,
                posterUrl = imageUrl,
                type = MediaKind.MOVIE,
            )
        }

        fun create(series: Series, imageUrl: String): SearchResult {
            return SearchResult(
                id = series.id,
                title = series.name,
                posterUrl = imageUrl,
                type = MediaKind.SERIES,
            )
        }
    }
}
