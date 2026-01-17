package hu.bbara.purefin.app.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.app.HomePageViewModel

@Composable
fun HomeContent(
    viewModel: HomePageViewModel = hiltViewModel(),
    colors: HomeColors,
    continueWatching: List<ContinueWatchingItem>,
    modifier: Modifier = Modifier
) {

    val libraries by viewModel.libraries.collectAsState()
    val libraryContent by viewModel.latestLibraryContent.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
        item {
            ContinueWatchingSection(
                items = continueWatching,
                colors = colors
            )
        }
        items(
            items = libraries.filter { libraryContent[it.id]?.isEmpty() != true },
            key = { it.id }
        ) { item ->
            LibraryPosterSection(
                title = item.name,
                items = libraryContent[item.id] ?: emptyList(),
                action = "See All",
                colors = colors
            )
        }
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
