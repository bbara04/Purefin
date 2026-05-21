package hu.bbara.purefin.ui.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun AppBottomBar(
    selectedTab: Int,
    isOnline: Boolean,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        modifier = Modifier.testTag(BottomNavigationTag)
    ) {
        if (isOnline) {
            NavigationBarItem(
                selected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                modifier = Modifier.testTag(BottomNavigationHomeTag),
                icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
                label = { Text("Home") }
            )
            NavigationBarItem(
                selected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                modifier = Modifier.testTag(BottomNavigationLibrariesTag),
                icon = { Icon(Icons.Outlined.Collections, contentDescription = "Libraries") },
                label = { Text("Libraries") }
            )
        }
        NavigationBarItem(
            selected = !isOnline || selectedTab == 2,
            onClick = { onTabSelected(2) },
            modifier = Modifier.testTag(BottomNavigationDownloadsTag),
            icon = { Icon(Icons.Outlined.Download, contentDescription = "Downloads") },
            label = { Text("Downloads") }
        )
    }
}

internal const val BottomNavigationTag = "bottom-navigation"
internal const val BottomNavigationHomeTag = "bottom-navigation-home"
internal const val BottomNavigationLibrariesTag = "bottom-navigation-libraries"
internal const val BottomNavigationDownloadsTag = "bottom-navigation-downloads"
