package hu.bbara.purefin.tv.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvHomeTopBar(
    tabs: List<TvHomeTabItem>,
    selectedTabIndex: Int,
    onTabSelected: (Int, TvHomeTabItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val safeSelectedTabIndex = selectedTabIndex.coerceIn(0, tabs.lastIndex.coerceAtLeast(0))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(scheme.background.copy(alpha = 0.95f))
            .zIndex(1f)
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PrimaryScrollableTabRow(
                selectedTabIndex = safeSelectedTabIndex,
                modifier = Modifier.weight(1f),
                containerColor = scheme.surface,
                contentColor = scheme.onSurface
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = index == safeSelectedTabIndex,
                        onClick = { onTabSelected(index, tab) },
                        text = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = null
                                )
                                Text(text = tab.label)
                            }
                        }
                    )
                }
            }
        }
    }
}
