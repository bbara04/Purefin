package hu.bbara.purefin.common.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.core.model.SearchResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurefinSearchBar(
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    searchResults: List<SearchResult>,
    onExpandedChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var expanded by rememberSaveable { mutableStateOf(false) }
    val setExpanded: (Boolean) -> Unit = {
        expanded = it
        onExpandedChange(it)
    }

    Box(
        modifier
            .fillMaxWidth()
            .semantics { isTraversalGroup = true }
    ) {
        SearchBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .semantics { traversalIndex = 0f },
            inputField = {
                SearchBarDefaults.InputField(
                    query = query,
                    onQueryChange = {
                        query = it
                        onQueryChange(it)
                    },
                    onSearch = {
                        onSearch(query)
                        setExpanded(false)
                    },
                    expanded = expanded,
                    onExpandedChange = setExpanded,
                    placeholder = { Text("Search") }
                )
            },
            expanded = expanded,
            onExpandedChange = setExpanded,
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.background(MaterialTheme.colorScheme.background)
            ) {
                items(searchResults) { item ->
                    SearchResultCard(item)
                }
            }
        }
    }
}

@Composable
private fun SearchResultCard(
    item: SearchResult,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        PurefinAsyncImage(
            model = item.posterUrl,
            contentDescription = item.title,
            modifier = Modifier.height(150.dp),
            contentScale = ContentScale.Fit
        )
        Text(
            text = item.title,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}
