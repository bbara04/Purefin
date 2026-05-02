package hu.bbara.purefin.ui.screen.home.components.search

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import hu.bbara.purefin.feature.search.SearchResult
import hu.bbara.purefin.model.MediaKind
import hu.bbara.purefin.ui.common.card.MediaImageCard

@Composable
internal fun SearchResultCard(
    item: SearchResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    MediaImageCard(
        imageUrl = item.posterUrl,
        title = item.title,
        subtitle = when (item.type) {
            MediaKind.MOVIE -> "Movie"
            MediaKind.SERIES -> "Series"
            else -> "Title"
        },
        onClick = onClick,
        modifier = modifier
    )
}
