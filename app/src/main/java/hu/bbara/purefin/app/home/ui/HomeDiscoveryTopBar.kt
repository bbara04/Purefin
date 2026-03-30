package hu.bbara.purefin.app.home.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDiscoveryTopBar(
    title: String,
    subtitle: String,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    var isProfileMenuExpanded by remember { mutableStateOf(false) }

    LargeTopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search"
                )
            }
            IconButton(
                onClick = { isProfileMenuExpanded = true },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
            ) {
                HomeAvatar(
                    size = 40.dp,
                    borderWidth = 1.dp,
                    borderColor = scheme.outlineVariant,
                    backgroundColor = scheme.secondaryContainer,
                    icon = Icons.Outlined.Person,
                    iconTint = scheme.onSecondaryContainer
                )
            }
            DropdownMenu(
                expanded = isProfileMenuExpanded,
                onDismissRequest = { isProfileMenuExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Profile") },
                    onClick = {
                        isProfileMenuExpanded = false
                        onProfileClick()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Settings") },
                    onClick = {
                        isProfileMenuExpanded = false
                        onSettingsClick()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Logout") },
                    onClick = {
                        isProfileMenuExpanded = false
                        onLogoutClick()
                    }
                )
            }
        },
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = scheme.background,
            scrolledContainerColor = scheme.surface.copy(alpha = 0.96f),
            navigationIconContentColor = scheme.onSurface,
            actionIconContentColor = scheme.onSurface,
            titleContentColor = scheme.onSurface
        ),
        scrollBehavior = scrollBehavior,
        modifier = modifier
    )
}
