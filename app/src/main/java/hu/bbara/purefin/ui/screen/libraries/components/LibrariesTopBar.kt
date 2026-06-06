package hu.bbara.purefin.ui.screen.libraries.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import hu.bbara.purefin.ui.screen.home.components.DefaultTopBar
import hu.bbara.purefin.ui.screen.home.components.DefaultTopBarSearchButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrariesTopBar(
    onSearchClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    DefaultTopBar(
        scrollBehavior = scrollBehavior,
        rightActions = {
            DefaultTopBarSearchButton(onClick = onSearchClick)
        }
    )
}
