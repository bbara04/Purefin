package hu.bbara.purefin.ui.screen.library.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.runtime.Composable
import hu.bbara.purefin.ui.screen.home.components.DefaultTopBar
import hu.bbara.purefin.ui.screen.home.components.DefaultTopBarIconButton

@Composable
fun LibraryTopBar(
    onBack: () -> Unit,
) {
    DefaultTopBar(
        leftActions = {
            DefaultTopBarIconButton(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                onClick = onBack,
            )
        },
        rightActions = {
            DefaultTopBarIconButton(
                imageVector = Icons.Outlined.FilterAlt,
                contentDescription = "Search",
                onClick = {},
            )
        }
    )
}
