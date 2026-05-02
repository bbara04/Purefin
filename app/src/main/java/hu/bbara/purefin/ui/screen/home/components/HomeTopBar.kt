package hu.bbara.purefin.ui.screen.home.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeTopBar(
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    var isProfileMenuExpanded by remember { mutableStateOf(false) }

    DefaultTopBar(
        rightActions = {
            DefaultTopBarSearchButton(onClick = onSearchClick)
            Spacer(modifier = Modifier.size(12.dp))
            DefaultTopBarIconButton(
                imageVector = Icons.Outlined.Person,
                contentDescription = "Profile",
                onClick = { isProfileMenuExpanded = true },
            )
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
        }
    )
}
