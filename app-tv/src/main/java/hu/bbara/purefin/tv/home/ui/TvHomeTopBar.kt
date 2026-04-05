package hu.bbara.purefin.tv.home.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

internal const val TvHomeTabTagPrefix = "tv-home-tab-"
private val TvHomeTopBarPillShape = RoundedCornerShape(18.dp)

@Composable
fun TvHomeTopBar(
    tabs: List<TvHomeTabItem>,
    selectedTabIndex: Int,
    onTabSelected: (Int, TvHomeTabItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val safeSelectedTabIndex = selectedTabIndex.coerceIn(0, tabs.lastIndex.coerceAtLeast(0))
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(safeSelectedTabIndex) {
        if (tabs.isNotEmpty()) {
            listState.animateScrollToItem(safeSelectedTabIndex)
        }
    }

    LazyRow(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .background(scheme.background),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(items = tabs) { index, tab ->
            var isFocused by remember { mutableStateOf(false) }
            val isSelected = index == safeSelectedTabIndex
            val containerColor by animateColorAsState(
                targetValue = when {
                    isSelected -> scheme.surfaceContainerHigh
                    isFocused -> scheme.surfaceContainerHigh.copy(alpha = 0.92f)
                    else -> scheme.surfaceContainer.copy(alpha = 0.72f)
                },
                label = "tv-home-tab-container"
            )
            val contentColor by animateColorAsState(
                targetValue = if (isSelected || isFocused) {
                    scheme.onBackground
                } else {
                    scheme.onSurfaceVariant
                },
                label = "tv-home-tab-content"
            )
            val borderColor by animateColorAsState(
                targetValue = when {
                    isFocused -> scheme.primary
                    isSelected -> scheme.outline
                    else -> scheme.outlineVariant.copy(alpha = 0.6f)
                },
                label = "tv-home-tab-border"
            )
            val scale by animateFloatAsState(
                targetValue = if (isFocused) 1.03f else 1f,
                label = "tv-home-tab-scale"
            )

            OutlinedButton(
                onClick = { onTabSelected(index, tab) },
                modifier = Modifier
                    .testTag("$TvHomeTabTagPrefix$index")
                    .semantics { selected = isSelected }
                    .heightIn(min = 46.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .onFocusChanged {
                        isFocused = it.isFocused
                        if (it.isFocused) {
                            scope.launch {
                                listState.animateScrollToItem(index)
                            }
                        }
                    },
                shape = TvHomeTopBarPillShape,
                border = BorderStroke(1.dp, borderColor),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = containerColor,
                    contentColor = contentColor
                ),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = tab.label,
                    fontSize = 15.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                )
            }
        }
    }
}
