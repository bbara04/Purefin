package hu.bbara.purefin.ui.screen.libraries

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import hu.bbara.purefin.core.model.LibraryUiModel
import hu.bbara.purefin.ui.screen.AppBottomBar
import hu.bbara.purefin.ui.screen.home.components.rememberDefaultTopBarScrollBehavior
import hu.bbara.purefin.ui.screen.libraries.components.LibrariesContent
import hu.bbara.purefin.ui.screen.libraries.components.LibrariesTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrariesScreen(
    items: List<LibraryUiModel>,
    onLibrarySelected: (LibraryUiModel) -> Unit,
    onSearchClick: () -> Unit,
    selectedTab: Int,
    isOnline: Boolean,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val topBarScrollBehavior = rememberDefaultTopBarScrollBehavior()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            LibrariesTopBar(
                onSearchClick = onSearchClick,
                scrollBehavior = topBarScrollBehavior
            )
        },
        bottomBar = {
            AppBottomBar(
                selectedTab = selectedTab,
                isOnline = isOnline,
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
