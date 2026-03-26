package hu.bbara.purefin.app.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.common.ui.components.PurefinSearchBar
import hu.bbara.purefin.feature.shared.search.SearchViewModel

@Composable
fun HomeTopBar(
    modifier: Modifier = Modifier,
    searchViewModel: SearchViewModel = hiltViewModel()
) {
    val scheme = MaterialTheme.colorScheme
    val searchResult = searchViewModel.searchResult.collectAsState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(scheme.background.copy(alpha = 0.95f))
            .zIndex(1f)
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
        ) {
            PurefinSearchBar(
                onQueryChange = {
                    searchViewModel.search(it)
                },
                onSearch = {
                    searchViewModel.search(it)
                },
                searchResults = searchResult.value,
                modifier = Modifier.weight(1.0f, true),
            )
        }
    }
}
