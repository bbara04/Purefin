package hu.bbara.purefin.ui.screen.libraries

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import hu.bbara.purefin.ui.model.LibraryUiModel
import hu.bbara.purefin.ui.screen.AppBottomBar
import hu.bbara.purefin.ui.screen.libraries.components.LibrariesContent
import hu.bbara.purefin.ui.screen.libraries.components.LibrariesTopBar

@Composable
fun LibrariesScreen(
    items: List<LibraryUiModel>,
    onLibrarySelected: (LibraryUiModel) -> Unit,
    onSearchClick: () -> Unit,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            LibrariesTopBar(
                onSearchClick = onSearchClick,
            )
        },
        bottomBar = {
            AppBottomBar(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected
            )
        }
    ) { innerPadding ->
        LibrariesContent(
            items = items,
            onLibrarySelected = onLibrarySelected,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
