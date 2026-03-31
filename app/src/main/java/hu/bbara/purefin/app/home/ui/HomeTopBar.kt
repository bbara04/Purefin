package hu.bbara.purefin.app.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.bbara.purefin.common.ui.components.PurefinLogo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    var isProfileMenuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Row(
                modifier = Modifier,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PurefinLogo(
                    contentDescription = "Purefin",
                    modifier = Modifier.size(48.dp),
                    contentScale = ContentScale.Fit,
                )
                Text(
                    text = "PureFin",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    color = scheme.onSecondary
                )
            }
        },
        actions = {
            IconButton(
                onClick = onSearchClick,
                colors = IconButtonColors(
                    containerColor = scheme.secondary,
                    contentColor = scheme.onSecondary,
                    disabledContainerColor = scheme.secondary,
                    disabledContentColor = scheme.onSecondary)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
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
        modifier = modifier
    )
}
