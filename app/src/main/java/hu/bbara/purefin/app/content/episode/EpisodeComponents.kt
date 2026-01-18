package hu.bbara.purefin.app.content.episode

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Cast
import androidx.compose.material.icons.outlined.ClosedCaption
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import hu.bbara.purefin.player.PlayerActivity

@Composable
internal fun EpisodeTopBar(modifier: Modifier = Modifier) {
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
            .background(EpisodeBackgroundDark.copy(alpha = 0.4f))
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
internal fun EpisodeHero(
    episode: EpisodeUiModel,
    height: Dp,
    isWide: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(height)
            .background(EpisodeBackgroundDark)
    ) {
        AsyncImage(
            model = episode.heroImageUrl,
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
                            EpisodeBackgroundDark.copy(alpha = 0.4f),
                            EpisodeBackgroundDark
                        )
                    )
                )
        )
        if (isWide) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                EpisodeBackgroundDark.copy(alpha = 0.8f)
                            )
                        )
                    )
            )
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            PlayButton(size = if (isWide) 96.dp else 80.dp)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun EpisodeDetails(
    episode: EpisodeUiModel,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = episode.title,
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 38.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetaChip(text = episode.releaseDate)
            MetaChip(text = episode.rating)
            MetaChip(text = episode.runtime)
            MetaChip(
                text = episode.format,
                background = EpisodePrimary.copy(alpha = 0.2f),
                border = EpisodePrimary.copy(alpha = 0.3f),
                textColor = EpisodePrimary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        PlaybackSettings(episode = episode)

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Synopsis",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = episode.synopsis,
            color = EpisodeMuted,
            fontSize = 15.sp,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(24.dp))
        ActionButtons()

        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "Cast",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        CastRow(cast = episode.cast)
    }
}

@Composable
private fun MetaChip(
    text: String,
    background: Color = Color.White.copy(alpha = 0.1f),
    border: Color = Color.Transparent,
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
private fun PlaybackSettings(episode: EpisodeUiModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(EpisodeSurfaceDark)
            .border(1.dp, EpisodeSurfaceBorder, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Tune,
                contentDescription = null,
                tint = EpisodePrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Playback Settings",
                color = EpisodeMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SettingDropdown(
                label = "Audio Track",
                icon = Icons.Outlined.VolumeUp,
                value = episode.audioTrack
            )
            SettingDropdown(
                label = "Subtitles",
                icon = Icons.Outlined.ClosedCaption,
                value = episode.subtitles
            )
        }
    }
}

@Composable
private fun SettingDropdown(
    label: String,
    icon: ImageVector,
    value: String
) {
    Column {
        Text(
            text = label,
            color = EpisodeMutedStrong,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(EpisodeBackgroundDark)
                .border(1.dp, EpisodeSurfaceBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = EpisodeMutedStrong)
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = value, color = Color.White, fontSize = 14.sp)
            }
            Icon(imageVector = Icons.Outlined.ExpandMore, contentDescription = null, tint = EpisodeMutedStrong)
        }
    }
}

@Composable
private fun ActionButtons() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
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
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, EpisodeSurfaceBorder, RoundedCornerShape(12.dp))
            .clickable { },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CastRow(cast: List<CastMember>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(cast) { member ->
            Column(modifier = Modifier.width(96.dp)) {
                Box(
                    modifier = Modifier
                        .aspectRatio(4f / 5f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(EpisodeSurfaceDark)
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
                                tint = EpisodeMutedStrong
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
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = member.role,
                    color = EpisodeMutedStrong,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PlayButton(
    size: Dp,
    modifier: Modifier = Modifier,
    viewModel: EpisodeScreenViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val episodeItem = viewModel.episode.collectAsState()

    Box(
        modifier = modifier
            .size(size)
            .shadow(24.dp, CircleShape)
            .clip(CircleShape)
            .background(EpisodePrimary)
            .clickable {
                val intent = Intent(context, PlayerActivity::class.java)
                intent.putExtra("MEDIA_ID", episodeItem.value!!.id.toString())
                context.startActivity(intent)
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = "Play",
            tint = EpisodeBackgroundDark,
            modifier = Modifier.size(42.dp)
        )
    }
}

@Composable
internal fun FloatingPlayButton(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(56.dp)
            .shadow(20.dp, CircleShape)
            .clip(CircleShape)
            .background(EpisodePrimary),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = "Play",
            tint = EpisodeBackgroundDark
        )
    }
}
