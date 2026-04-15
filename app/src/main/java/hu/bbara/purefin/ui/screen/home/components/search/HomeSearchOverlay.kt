package hu.bbara.purefin.ui.screen.home.components.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.feature.search.SearchViewModel
import java.util.UUID
import hu.bbara.purefin.core.model.MediaKind

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
                                        MediaKind.MOVIE -> onMovieSelected(item.id)
                                        MediaKind.SERIES -> onSeriesSelected(item.id)
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
