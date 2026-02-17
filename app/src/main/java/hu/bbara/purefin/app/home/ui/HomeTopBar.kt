package hu.bbara.purefin.app.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import hu.bbara.purefin.common.ui.components.PurefinIconButton
import hu.bbara.purefin.common.ui.components.SearchField

@Composable
fun HomeTopBar(
    onMenuClick: () -> Unit,
    isOfflineMode: Boolean,
    onToggleOfflineMode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme

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
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        ) {
            PurefinIconButton(
                icon = Icons.Outlined.Menu,
                contentDescription = "Menu",
                onClick = onMenuClick,
            )
            SearchField(
                value = "",
                onValueChange = {},
                placeholder = "Search",
                backgroundColor = scheme.surface,
                textColor = scheme.onSurface,
                cursorColor = scheme.secondary,
                modifier = Modifier.weight(1.0f, true),
            )
            PurefinIconButton(
                icon = if (isOfflineMode) Icons.Outlined.CloudOff else Icons.Outlined.Cloud,
                contentDescription = if (isOfflineMode) "Switch to Online" else "Switch to Offline",
                onClick = onToggleOfflineMode
            )
        }
    }
}


