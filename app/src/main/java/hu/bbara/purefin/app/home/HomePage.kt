package hu.bbara.purefin.app.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.app.home.ui.HomeContent
import hu.bbara.purefin.app.home.ui.HomeDrawerContent
import hu.bbara.purefin.app.home.ui.HomeMockData
import hu.bbara.purefin.app.home.ui.HomeNavItem
import hu.bbara.purefin.app.home.ui.HomeTopBar
import kotlinx.coroutines.launch
import org.jellyfin.sdk.model.api.CollectionType

@Composable
fun HomePage(
    viewModel: HomePageViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val libraries = viewModel.libraries.collectAsState().value.map {
        HomeNavItem(
            id = it.id,
            label = it.name,
            icon = when (it.type) {
                CollectionType.MOVIES -> Icons.Outlined.Movie
                CollectionType.TVSHOWS -> Icons.Outlined.Tv
                else -> Icons.Outlined.Collections
            },
        )
    }
    val continueWatching = viewModel.continueWatching.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxSize(),
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerContentColor = MaterialTheme.colorScheme.onBackground
            ) {
                HomeDrawerContent(
                    title = "Jellyfin",
                    subtitle = "Library Dashboard",
                    primaryNavItems = libraries,
                    secondaryNavItems = HomeMockData.secondaryNavItems,
                    user = HomeMockData.user,
                )
            }
        }
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            topBar = {
                HomeTopBar(
                    title = "Home",
                    onMenuClick = { coroutineScope.launch { drawerState.open() } }
                )
            }
        ) { innerPadding ->
            HomeContent(
                continueWatching = continueWatching.value,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
