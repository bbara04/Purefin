package hu.bbara.purefin.ui.screen.home.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.navigation.HOME_SEARCH_SHARED_BOUNDS_KEY
import hu.bbara.purefin.navigation.LocalNavSharedAnimatedVisibilityScope
import hu.bbara.purefin.navigation.LocalSharedTransitionScope

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeTopBar(
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavSharedAnimatedVisibilityScope.current

    var isProfileMenuExpanded by remember { mutableStateOf(false) }

    DefaultTopBar()
        {
            val searchButtonModifier = if (
                sharedTransitionScope != null &&
                animatedVisibilityScope != null
            ) {
                with(sharedTransitionScope) {
                    Modifier
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(
                                key = HOME_SEARCH_SHARED_BOUNDS_KEY
                            ),
                            animatedVisibilityScope = animatedVisibilityScope,
                            enter = fadeIn(),
                            exit = fadeOut(),
                            resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds()
                        )
                        .size(50.dp)
                }
            } else {
                Modifier.size(50.dp)
            }

            IconButton(
                onClick = onSearchClick,
                colors = IconButtonColors(
                    containerColor = scheme.surface,
                    contentColor = scheme.onSurface,
                    disabledContainerColor = scheme.surface,
                    disabledContentColor = scheme.onSurface
                ),
                modifier = searchButtonModifier,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    modifier = Modifier.size(30.dp),
                )
            }
            Spacer(modifier = Modifier.size(12.dp))
            IconButton(
                onClick = { isProfileMenuExpanded = true },
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
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.size(30.dp),
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
