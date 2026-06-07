package hu.bbara.purefin.ui.screen.download

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import hu.bbara.purefin.ui.screen.AppBottomBar
import hu.bbara.purefin.ui.screen.download.components.DownloadsContent
import hu.bbara.purefin.ui.screen.home.components.DefaultTopBar
import hu.bbara.purefin.ui.screen.home.components.DefaultTopBarTextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    selectedTab: Int,
    isOnline: Boolean,
    isCheckingConnection: Boolean,
    onCheckConnection: () -> Unit,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            DefaultTopBar(
                rightActions = {
                    if (!isOnline) {
                        DefaultTopBarTextButton(
                            text = if (isCheckingConnection) "Checking…" else "Check connection",
                            onClick = onCheckConnection,
                            enabled = !isCheckingConnection
                        )
                    }
                }
            )
        },
        bottomBar = {
            AppBottomBar(
                selectedTab = selectedTab,
                isOnline = isOnline,
                onTabSelected = onTabSelected
            )
        }
    ) { innerPadding ->
        DownloadsContent(
            modifier = Modifier.padding(innerPadding)
        )
    }
}
