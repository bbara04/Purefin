package hu.bbara.purefin.tv.home

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
import androidx.lifecycle.compose.LifecycleResumeEffect
import hu.bbara.purefin.feature.shared.home.HomePageViewModel
import hu.bbara.purefin.tv.home.ui.TvHomeContent
import hu.bbara.purefin.tv.home.ui.TvHomeDrawerContent
import hu.bbara.purefin.tv.home.ui.TvHomeMockData
import hu.bbara.purefin.tv.home.ui.TvHomeNavItem
import hu.bbara.purefin.tv.home.ui.TvHomeTopBar
import kotlinx.coroutines.launch
import org.jellyfin.sdk.model.api.CollectionType

@Composable
fun TvHomePage(
    viewModel: HomePageViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val libraries = viewModel.libraries.collectAsState().value
    val isOfflineMode = viewModel.isOfflineMode.collectAsState().value
    val libraryNavItems = libraries.map {
        TvHomeNavItem(
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
    val nextUp = viewModel.nextUp.collectAsState()
    val latestLibraryContent = viewModel.latestLibraryContent.collectAsState()

    LifecycleResumeEffect(Unit) {
        viewModel.onResumed()
        onPauseOrDispose { }
    }

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
                TvHomeDrawerContent(
                    title = "Jellyfin",
                    subtitle = "Library Dashboard",
                    primaryNavItems = libraryNavItems,
                    secondaryNavItems = TvHomeMockData.secondaryNavItems,
                    user = TvHomeMockData.user,
                    onLibrarySelected = { item -> viewModel.onLibrarySelected(item.id, item.label) },
                    onLogout = viewModel::logout
                )
            }
        }
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            topBar = {
                TvHomeTopBar(
                    onMenuClick = { coroutineScope.launch { drawerState.open() } },
                    isOfflineMode = isOfflineMode,
                    onToggleOfflineMode = viewModel::toggleOfflineMode
                )
            }
        ) { innerPadding ->
            TvHomeContent(
                libraries = libraries,
                libraryContent = latestLibraryContent.value,
                continueWatching = continueWatching.value,
                nextUp = nextUp.value,
                onMovieSelected = viewModel::onMovieSelected,
                onSeriesSelected = viewModel::onSeriesSelected,
                onEpisodeSelected = viewModel::onEpisodeSelected,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
