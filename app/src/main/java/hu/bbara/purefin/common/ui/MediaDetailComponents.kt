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
import androidx.compose.material.icons.outlined.ClosedCaption
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.VolumeUp
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
fun MediaHero(
    imageUrl: String,
    colors: MediaDetailColors,
    height: Dp,
    isWide: Boolean,
    modifier: Modifier = Modifier,
    showPlayButton: Boolean = false,
    playButtonSize: Dp = 80.dp,
    onPlayClick: (() -> Unit)? = null,
    horizontalGradientOnWide: Boolean = true
) {
    Box(
        modifier = modifier
            .height(height)
            .background(colors.background)
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
                            colors.background.copy(alpha = 0.4f),
                            colors.background
                        )
                    )
                )
        )
        if (horizontalGradientOnWide && isWide) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                colors.background.copy(alpha = 0.8f)
                            )
                        )
                    )
            )
        }
        if (showPlayButton && onPlayClick != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                MediaPlayButton(
                    colors = colors,
                    size = playButtonSize,
                    onClick = onPlayClick
                )
            }
        }
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
fun MediaPlaybackSettings(
    colors: MediaDetailColors,
    audioTrack: String,
    subtitles: String,
    headerIcon: ImageVector = Icons.Outlined.Tune,
    audioIcon: ImageVector = Icons.Outlined.VolumeUp,
    subtitleIcon: ImageVector = Icons.Outlined.ClosedCaption,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceAlt)
            .border(1.dp, colors.surfaceBorder, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = headerIcon,
                contentDescription = null,
                tint = colors.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Playback Settings",
                color = colors.textMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MediaSettingDropdown(
                colors = colors,
                label = "Audio Track",
                value = audioTrack,
                icon = audioIcon
            )
            MediaSettingDropdown(
                colors = colors,
                label = "Subtitles",
                value = subtitles,
                icon = subtitleIcon
            )
        }
    }
}

@Composable
private fun MediaSettingDropdown(
    colors: MediaDetailColors,
    label: String,
    value: String,
    icon: ImageVector? = null
) {
    Column {
        Text(
            text = label,
            color = colors.textMutedStrong,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surface)
                .border(1.dp, colors.surfaceBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(imageVector = icon, contentDescription = null, tint = colors.textMutedStrong)
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Text(text = value, color = colors.textPrimary, fontSize = 14.sp)
            }
            Icon(imageVector = Icons.Outlined.ExpandMore, contentDescription = null, tint = colors.textMutedStrong)
        }
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
    colors: MediaDetailColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .shadow(20.dp, CircleShape)
            .clip(CircleShape)
            .background(colors.primary)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = "Play",
            tint = colors.onPrimary
        )
    }
}
