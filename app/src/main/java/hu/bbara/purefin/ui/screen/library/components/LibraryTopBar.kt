package hu.bbara.purefin.ui.screen.library.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.ui.screen.home.components.DefaultTopBar

@Composable
fun LibraryTopBar(
    onSearchClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme

    DefaultTopBar {
        IconButton(
            onClick = onSearchClick,
            colors = IconButtonColors(
                containerColor = scheme.surface,
                contentColor = scheme.onSurface,
                disabledContainerColor = scheme.surface,
                disabledContentColor = scheme.onSurface
            ),
            modifier = Modifier.size(50.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Search",
                modifier = Modifier.size(30.dp),
            )
        }
    }
}