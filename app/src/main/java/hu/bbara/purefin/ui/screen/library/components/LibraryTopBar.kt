package hu.bbara.purefin.ui.screen.library.components

import androidx.compose.runtime.Composable
import hu.bbara.purefin.ui.screen.home.components.DefaultTopBar
import hu.bbara.purefin.ui.screen.home.components.DefaultTopBarSearchButton

@Composable
fun LibraryTopBar(
    onSearchClick: () -> Unit,
) {
    DefaultTopBar {
        DefaultTopBarSearchButton(onClick = onSearchClick)
    }
}
