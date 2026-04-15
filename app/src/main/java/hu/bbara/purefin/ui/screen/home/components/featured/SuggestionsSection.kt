package hu.bbara.purefin.ui.screen.home.components.featured

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.feature.browse.home.SuggestedItem

@Composable
fun SuggestionsSection(
    items: List<SuggestedItem>,
    onItemOpen: (SuggestedItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { items.size })

    LaunchedEffect(Unit) {
        pagerState.scrollToPage(0)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        HorizontalPager(
            state = pagerState,
            pageSize = PageSize.Fixed(320.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            SuggestionCard(
                item = items[page],
                onClick = { onItemOpen(items[page]) }
            )
        }
        if (items.size > 1) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                repeat(items.size) { index ->
                    val selected = pagerState.currentPage == index
                    val indicatorWidth = animateDpAsState(
                        targetValue = if (selected) 22.dp else 8.dp
                    )
                    val indicatorColor = animateColorAsState(
                        targetValue = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        }
                    )
                    Box(
                        modifier = Modifier
                            .width(indicatorWidth.value)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(indicatorColor.value)
                    )
                }
            }
        }
    }
}
