package hu.bbara.purefin.app.content.series

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Cast
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

@Composable
internal fun SeriesTopBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        GhostIconButton(icon = Icons.Outlined.ArrowBack, contentDescription = "Back")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GhostIconButton(icon = Icons.Outlined.Cast, contentDescription = "Cast")
            GhostIconButton(icon = Icons.Outlined.MoreVert, contentDescription = "More")
        }
    }
}

@Composable
private fun GhostIconButton(
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(SeriesBackgroundDark.copy(alpha = 0.4f))
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White
        )
    }
}

@Composable
internal fun SeriesHero(
    imageUrl: String,
    height: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(height)
            .background(SeriesBackgroundDark)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            SeriesBackgroundDark.copy(alpha = 0.4f),
                            SeriesBackgroundDark
                        )
                    )
                )
        )
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            PlayButton(size = 80.dp)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SeriesMetaChips(series: SeriesUiModel) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetaChip(text = series.year)
        MetaChip(text = series.rating)
        MetaChip(text = series.seasons)
        MetaChip(
            text = series.format,
            background = SeriesPrimary.copy(alpha = 0.2f),
            border = SeriesPrimary.copy(alpha = 0.3f),
            textColor = SeriesPrimary
        )
    }
}

@Composable
private fun MetaChip(
    text: String,
    background: Color = Color.White.copy(alpha = 0.1f),
    border: Color = Color.White.copy(alpha = 0.05f),
    textColor: Color = Color.White
) {
    Box(
        modifier = Modifier
            .height(28.dp)
            .wrapContentHeight(Alignment.CenterVertically)
            .clip(RoundedCornerShape(6.dp))
            .background(background)
            .border(width = 1.dp, color = border, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
internal fun SeriesActionButtons(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ActionButton(
            text = "Watchlist",
            icon = Icons.Outlined.Add,
            modifier = Modifier.weight(1f)
        )
        ActionButton(
            text = "Download",
            icon = Icons.Outlined.Download,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ActionButton(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .border(1.dp, SeriesSurfaceBorder, RoundedCornerShape(12.dp))
            .clickable { },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
internal fun SeasonTabs(seasons: List<SeriesSeasonUiModel>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        seasons.forEach { season ->
            SeasonTab(name = season.name, isSelected = season.isSelected)
        }
    }
}

@Composable
private fun SeasonTab(name: String, isSelected: Boolean) {
    val color = if (isSelected) SeriesPrimary else SeriesMutedStrong
    val borderColor = if (isSelected) SeriesPrimary else Color.Transparent
    Column(
        modifier = Modifier
            .padding(bottom = 8.dp)
            .clickable { }
    ) {
        Text(
            text = name,
            color = color,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .height(2.dp)
                .width(52.dp)
                .background(borderColor)
        )
    }
}

@Composable
internal fun EpisodeCarousel(episodes: List<SeriesEpisodeUiModel>, modifier: Modifier = Modifier) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(episodes) { episode ->
            EpisodeCard(episode = episode)
        }
    }
}

@Composable
private fun EpisodeCard(episode: SeriesEpisodeUiModel) {
    Column(
        modifier = Modifier
            .width(260.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SeriesSurfaceDark.copy(alpha = 0.3f))
            .border(1.dp, SeriesSurfaceBorder, RoundedCornerShape(16.dp))
            .padding(12.dp)
            .clickable { },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(12.dp))
                .background(SeriesSurfaceDark)
                .border(1.dp, SeriesSurfaceBorder, RoundedCornerShape(12.dp))
        ) {
            AsyncImage(
                model = episode.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.2f))
            )
            Icon(
                imageVector = Icons.Outlined.PlayCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(32.dp)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = episode.duration,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = episode.title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = episode.description,
                color = SeriesMutedStrong,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun CastRow(cast: List<SeriesCastMemberUiModel>, modifier: Modifier = Modifier) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(cast) { member ->
            CastCard(member = member)
        }
    }
}

@Composable
private fun CastCard(member: SeriesCastMemberUiModel) {
    Column(
        modifier = Modifier.width(84.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(4f / 5f)
                .clip(RoundedCornerShape(12.dp))
                .background(SeriesSurfaceDark)
                .border(1.dp, SeriesSurfaceBorder, RoundedCornerShape(12.dp))
        ) {
            if (member.imageUrl == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = SeriesMutedStrong
                    )
                }
            } else {
                AsyncImage(
                    model = member.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Text(
            text = member.name,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = member.role,
            color = SeriesMutedStrong,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PlayButton(size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(24.dp, CircleShape)
            .clip(CircleShape)
            .background(SeriesPrimary)
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = "Play",
            tint = SeriesBackgroundDark,
            modifier = Modifier.size(36.dp)
        )
    }
}
