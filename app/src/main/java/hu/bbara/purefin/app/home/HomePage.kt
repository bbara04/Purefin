package hu.bbara.purefin.app.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerValue
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
import hu.bbara.purefin.app.home.ui.HomeTopBar
import hu.bbara.purefin.app.home.ui.rememberHomeColors
import kotlinx.coroutines.launch

@Composable
fun HomePage(
    viewModel: HomePageViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val colors = rememberHomeColors()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val continueWatching = viewModel.continueWatching.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxSize(),
                drawerContainerColor = colors.drawerBackground,
                drawerContentColor = colors.textPrimary
            ) {
                HomeDrawerContent(
                    title = "Jellyfin",
                    subtitle = "Library Dashboard",
                    colors = colors,
                    primaryNavItems = HomeMockData.primaryNavItems,
                    secondaryNavItems = HomeMockData.secondaryNavItems,
                    user = HomeMockData.user
                )
            }
        }
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = colors.background,
            contentColor = colors.textPrimary,
            topBar = {
                HomeTopBar(
                    title = "Home",
                    colors = colors,
                    onMenuClick = { coroutineScope.launch { drawerState.open() } }
                )
            }
        ) { innerPadding ->
            HomeContent(
                colors = colors,
                continueWatching = continueWatching.value,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
