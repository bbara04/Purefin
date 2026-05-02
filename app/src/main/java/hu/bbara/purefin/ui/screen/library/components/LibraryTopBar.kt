package hu.bbara.purefin.ui.screen.library.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.ui.screen.home.components.DefaultTopBar

@Composable
fun LibraryTopBar(
    onBack: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    DefaultTopBar(
        leftActions = {
            IconButton(
                onClick = onBack,
                colors = IconButtonColors(
                    containerColor = scheme.surface,
                    contentColor = scheme.onSurface,
                    disabledContainerColor = scheme.surface,
                    disabledContentColor = scheme.onSurface
                ),
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Search",
                    modifier = Modifier.size(30.dp),
                )
            }
        }
    )
}