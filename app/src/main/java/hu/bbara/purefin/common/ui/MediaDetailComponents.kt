package hu.bbara.purefin.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import hu.bbara.purefin.app.content.episode.EpisodeColors
import hu.bbara.purefin.app.content.movie.MovieColors
import hu.bbara.purefin.app.content.series.SeriesColors

data class MediaDetailColors(
    val primary: Color,
    val onPrimary: Color,
    val background: Color,
    val surface: Color,
    val surfaceAlt: Color,
    val surfaceBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textMutedStrong: Color
)

internal fun MovieColors.toMediaDetailColors() = MediaDetailColors(
    primary = primary,
    onPrimary = onPrimary,
    background = background,
    surface = surface,
    surfaceAlt = surfaceAlt,
    surfaceBorder = surfaceBorder,
    textPrimary = textPrimary,
    textSecondary = textSecondary,
    textMuted = textMuted,
    textMutedStrong = textMutedStrong
)

internal fun EpisodeColors.toMediaDetailColors() = MediaDetailColors(
    primary = primary,
    onPrimary = onPrimary,
    background = background,
    surface = surface,
    surfaceAlt = surfaceAlt,
    surfaceBorder = surfaceBorder,
    textPrimary = textPrimary,
    textSecondary = textSecondary,
    textMuted = textMuted,
    textMutedStrong = textMutedStrong
)

internal fun SeriesColors.toMediaDetailColors() = MediaDetailColors(
    primary = primary,
    onPrimary = onPrimary,
    background = background,
    surface = surface,
    surfaceAlt = surfaceAlt,
    surfaceBorder = surfaceBorder,
    textPrimary = textPrimary,
    textSecondary = textSecondary,
    textMuted = textMuted,
    textMutedStrong = textMutedStrong
)

@Composable
fun MediaGhostIconButton(
    colors: MediaDetailColors,
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(colors.background.copy(alpha = 0.4f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = colors.textPrimary
        )
    }
}

@Composable
fun MediaMetaChip(
    colors: MediaDetailColors,
    text: String,
    background: Color = colors.surfaceAlt,
    border: Color = Color.Transparent,
    textColor: Color = colors.textSecondary,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
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
fun MediaActionButtons(
    colors: MediaDetailColors,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    textSize: TextUnit = 14.sp,
    watchlistIcon: ImageVector = Icons.Outlined.Add,
    downloadIcon: ImageVector = Icons.Outlined.Download
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MediaActionButton(
            colors = colors,
            text = "Watchlist",
            icon = watchlistIcon,
            modifier = Modifier.weight(1f),
            height = height,
            textSize = textSize
        )
        MediaActionButton(
            colors = colors,
            text = "Download",
            icon = downloadIcon,
            modifier = Modifier.weight(1f),
            height = height,
            textSize = textSize
        )
    }
}

@Composable
private fun MediaActionButton(
    colors: MediaDetailColors,
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    height: Dp,
    textSize: TextUnit
) {
    Row(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceAlt.copy(alpha = 0.6f))
            .border(1.dp, colors.surfaceBorder, RoundedCornerShape(12.dp))
            .clickable { },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = colors.textPrimary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, color = colors.textPrimary, fontSize = textSize, fontWeight = FontWeight.Bold)
    }
}

data class MediaCastMember(
    val name: String,
    val role: String,
    val imageUrl: String?
)

@Composable
fun MediaCastRow(
    colors: MediaDetailColors,
    cast: List<MediaCastMember>,
    modifier: Modifier = Modifier,
    cardWidth: Dp = 96.dp,
    nameSize: TextUnit = 12.sp,
    roleSize: TextUnit = 10.sp
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(cast) { member ->
            Column(modifier = Modifier.width(cardWidth)) {
                Box(
                    modifier = Modifier
                        .aspectRatio(4f / 5f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceAlt)
                ) {
                    if (member.imageUrl == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(colors.surfaceAlt.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = colors.textMutedStrong
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
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = member.name,
                    color = colors.textPrimary,
                    fontSize = nameSize,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = member.role,
                    color = colors.textMutedStrong,
                    fontSize = roleSize,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun MediaPlayButton(
    colors: MediaDetailColors,
    size: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(24.dp, CircleShape)
            .clip(CircleShape)
            .background(colors.primary)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = "Play",
            tint = colors.onPrimary,
            modifier = Modifier.size(42.dp)
        )
    }
}

@Composable
fun MediaFloatingPlayButton(
    containerColor: Color,
    onContainerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .shadow(20.dp, CircleShape)
            .clip(CircleShape)
            .background(containerColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = "Play",
            tint = onContainerColor
        )
    }
}
