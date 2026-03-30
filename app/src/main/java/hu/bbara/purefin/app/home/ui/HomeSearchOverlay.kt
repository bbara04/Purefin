package hu.bbara.purefin.app.home.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.common.ui.components.PurefinAsyncImage
import hu.bbara.purefin.core.model.SearchResult
import hu.bbara.purefin.feature.shared.search.SearchViewModel
import org.jellyfin.sdk.model.UUID
import org.jellyfin.sdk.model.api.BaseItemKind

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSearchOverlay(
    visible: Boolean,
    topPadding: Dp,
    onDismiss: () -> Unit,
    onMovieSelected: (UUID) -> Unit,
    onSeriesSelected: (UUID) -> Unit,
    modifier: Modifier = Modifier,
    searchViewModel: SearchViewModel = hiltViewModel(),
) {
    if (!visible) return

    BackHandler(onBack = onDismiss)

    var query by rememberSaveable { mutableStateOf("") }
    val searchResults by searchViewModel.searchResult.collectAsState()
    val dismissInteractionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.36f))
                .clickable(
                    interactionSource = dismissInteractionSource,
                    indication = null,
                    onClick = onDismiss
                )
        )
        SearchBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = topPadding + 8.dp, start = 16.dp, end = 16.dp),
            inputField = {
                SearchBarDefaults.InputField(
                    query = query,
                    onQueryChange = {
                        query = it
                        searchViewModel.search(it)
                    },
                    onSearch = { searchViewModel.search(query) },
                    expanded = true,
                    onExpandedChange = { expanded ->
                        if (!expanded) {
                            onDismiss()
                        }
                    },
                    placeholder = { Text("Search movies and shows") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Close search"
                            )
                        }
                    }
                )
            },
            expanded = true,
            onExpandedChange = { expanded ->
                if (!expanded) {
                    onDismiss()
                }
            }
        ) {
            when {
                query.isBlank() -> {
                    SearchMessage(
                        title = "Search your library",
                        body = "Find movies and shows by title."
                    )
                }

                searchResults.isEmpty() -> {
                    SearchMessage(
                        title = "No matches",
                        body = "Try a different title or browse your libraries."
                    )
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 132.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        items(searchResults, key = { result -> "${result.type}:${result.id}" }) { item ->
                            HomeSearchResultCard(
                                item = item,
                                onClick = {
                                    when (item.type) {
                                        BaseItemKind.MOVIE -> onMovieSelected(item.id)
                                        BaseItemKind.SERIES -> onSeriesSelected(item.id)
                                        else -> Unit
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchMessage(
    title: String,
    body: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                androidx.compose.foundation.layout.Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeSearchResultCard(
    item: SearchResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = scheme.surfaceContainer,
        tonalElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
    ) {
        androidx.compose.foundation.layout.Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(12.dp)
        ) {
            PurefinAsyncImage(
                model = item.posterUrl,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(18.dp))
            )
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = when (item.type) {
                    BaseItemKind.MOVIE -> "Movie"
                    BaseItemKind.SERIES -> "Series"
                    else -> "Title"
                },
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant
            )
        }
    }
}
