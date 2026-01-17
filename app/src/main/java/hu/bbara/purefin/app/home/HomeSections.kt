package hu.bbara.purefin.app.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ContinueWatchingSection(
    items: List<ContinueWatchingItem>,
    colors: HomeColors,
    modifier: Modifier = Modifier
) {
    SectionHeader(
        title = "Continue Watching",
        action = null,
        colors = colors
    )
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items) { item ->
            ContinueWatchingCard(
                item = item,
                colors = colors
            )
        }
    }
}

@Composable
fun ContinueWatchingCard(
    item: ContinueWatchingItem,
    colors: HomeColors,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(280.dp)
            .wrapContentHeight()
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(16f / 9f)
                .shadow(12.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(colors.card)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Brush.linearGradient(item.colors))
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.2f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(item.progress)
                        .background(colors.primary)
                )
            }
        }
        Column(modifier = Modifier.padding(top = 12.dp)) {
            Text(
                text = item.primaryText,
                color = colors.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.secondaryText,
                color = colors.textSecondary,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun LibraryPosterSection(
    title: String,
    items: List<PosterItem>,
    action: String?,
    colors: HomeColors,
    modifier: Modifier = Modifier
) {
    SectionHeader(
        title = title,
        action = action,
        colors = colors
    )
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items) { item ->
            PosterCard(
                item = item,
                colors = colors
            )
        }
    }
}

@Composable
fun PosterCard(
    item: PosterItem,
    colors: HomeColors,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(144.dp)
            .aspectRatio(2f / 3f)
            .shadow(10.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(colors.card)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Brush.linearGradient(item.colors))
        )
        if (item.isLatest) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.primary)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "LATEST",
                    color = colors.onPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
        )
        Text(
            text = item.title,
            color = colors.textPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    action: String?,
    colors: HomeColors,
    modifier: Modifier = Modifier,
    onActionClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = colors.textPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        if (action != null) {
            Text(
                text = action,
                color = colors.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onActionClick() }
            )
        }
    }
}
