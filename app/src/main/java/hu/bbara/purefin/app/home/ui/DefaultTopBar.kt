package hu.bbara.purefin.app.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.common.ui.components.PurefinSearchBar
import hu.bbara.purefin.feature.shared.search.SearchViewModel

@Composable
fun DefaultTopBar(
    modifier: Modifier = Modifier,
    searchViewModel: SearchViewModel = hiltViewModel(),
    onProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme
    val searchResult = searchViewModel.searchResult.collectAsState()
    var isProfileMenuExpanded by remember { mutableStateOf(false) }
    var isSearchExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(scheme.background.copy(alpha = 0.95f))
            .zIndex(1f)
    ) {
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            PurefinSearchBar(
                onQueryChange = {
                    searchViewModel.search(it)
                },
                onSearch = {
                    searchViewModel.search(it)
                },
                onExpandedChange = { expanded ->
                    isSearchExpanded = expanded
                    if (expanded) {
                        isProfileMenuExpanded = false
                    }
                },
                searchResults = searchResult.value,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = if (isSearchExpanded) 0.dp else 72.dp),
            )
            if (!isSearchExpanded) {
                Box {
                    IconButton(
                        onClick = { isProfileMenuExpanded = true },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape),
                    ) {
                        HomeAvatar(
                            size = 56.dp,
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
                }
            }
        }
    }
}
