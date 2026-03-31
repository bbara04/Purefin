package hu.bbara.purefin.app.home.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.app.home.ui.shared.SectionHeader
import hu.bbara.purefin.feature.shared.home.LibraryItem
import hu.bbara.purefin.feature.shared.home.PosterItem
import org.jellyfin.sdk.model.UUID

@Composable
fun LibraryPosterSection(
    library: LibraryItem,
    items: List<PosterItem>,
    onLibrarySelected: (LibraryItem) -> Unit,
    onMovieSelected: (UUID) -> Unit,
    onSeriesSelected: (UUID) -> Unit,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        SectionHeader(
            title = library.name,
            actionLabel = "See all",
            onActionClick = { onLibrarySelected(library) }
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items = items, key = { item -> item.id }) { item ->
                HomeBrowseCard(
                    item = item,
                    onMovieSelected = onMovieSelected,
                    onSeriesSelected = onSeriesSelected,
                    onEpisodeSelected = onEpisodeSelected
                )
            }
        }
    }
}
