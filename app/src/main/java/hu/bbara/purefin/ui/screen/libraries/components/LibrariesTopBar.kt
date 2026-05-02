package hu.bbara.purefin.ui.screen.libraries.components

import androidx.compose.runtime.Composable
import hu.bbara.purefin.ui.screen.home.components.DefaultTopBar
import hu.bbara.purefin.ui.screen.home.components.DefaultTopBarSearchButton

@Composable
fun LibrariesTopBar(
    onSearchClick: () -> Unit,
) {
    DefaultTopBar(
        rightActions = {
            DefaultTopBarSearchButton(onClick = onSearchClick)
        }
    )
}
