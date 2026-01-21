package hu.bbara.purefin.app.content.episode

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Cast
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.common.ui.MediaActionButtons
import hu.bbara.purefin.common.ui.MediaCastMember
import hu.bbara.purefin.common.ui.MediaCastRow
import hu.bbara.purefin.common.ui.MediaFloatingPlayButton
import hu.bbara.purefin.common.ui.MediaGhostIconButton
import hu.bbara.purefin.common.ui.MediaMetaChip
import hu.bbara.purefin.common.ui.MediaPlaybackSettings
import hu.bbara.purefin.common.ui.components.MediaHero
import hu.bbara.purefin.common.ui.toMediaDetailColors
import hu.bbara.purefin.player.PlayerActivity

@Composable
internal fun EpisodeTopBar(
    viewModel: EpisodeScreenViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val colors = rememberEpisodeColors().toMediaDetailColors()
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MediaGhostIconButton(
            colors = colors,
            icon = Icons.Outlined.ArrowBack,
            contentDescription = "Back",
            onClick = { viewModel.onBack() }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MediaGhostIconButton(colors = colors, icon = Icons.Outlined.Cast, contentDescription = "Cast", onClick = { })
            MediaGhostIconButton(colors = colors, icon = Icons.Outlined.MoreVert, contentDescription = "More", onClick = { })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun EpisodeDetails(
    episode: EpisodeUiModel,
    modifier: Modifier = Modifier
) {
    val colors = rememberEpisodeColors().toMediaDetailColors()
    Column(modifier = modifier) {
        Text(
            text = episode.title,
            color = colors.textPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 38.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MediaMetaChip(colors = colors, text = episode.releaseDate)
            MediaMetaChip(colors = colors, text = episode.rating)
            MediaMetaChip(colors = colors, text = episode.runtime)
            MediaMetaChip(
                colors = colors,
                text = episode.format,
                background = colors.primary.copy(alpha = 0.2f),
                border = colors.primary.copy(alpha = 0.3f),
                textColor = colors.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        MediaPlaybackSettings(
            colors = colors,
            audioTrack = episode.audioTrack,
            subtitles = episode.subtitles
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Synopsis",
            color = colors.textPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = episode.synopsis,
            color = colors.textMuted,
            fontSize = 15.sp,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(24.dp))
        MediaActionButtons(colors = colors)

        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "Cast",
            color = colors.textPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        MediaCastRow(
            colors = colors,
            cast = episode.cast.map { it.toMediaCastMember() }
        )
    }
}

@Composable
fun EpisodeCard(
    episode: EpisodeUiModel,
    backGroundColor: Color,
    modifier: Modifier = Modifier,
) {
    val colors = rememberEpisodeColors().toMediaDetailColors()
    val context = LocalContext.current
    val playAction = remember(episode.id) {
        {
            val intent = Intent(context, PlayerActivity::class.java)
            intent.putExtra("MEDIA_ID", episode.id.toString())
            context.startActivity(intent)
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        val isWide = maxWidth >= 900.dp
        val contentPadding = if (isWide) 32.dp else 20.dp

        Box(modifier = Modifier.fillMaxSize()) {
            if (isWide) {
                Row(modifier = Modifier.fillMaxSize()) {
                    MediaHero(
                        imageUrl = episode.heroImageUrl,
                        height = 300.dp,
                        backgroundColor = backGroundColor,
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.5f)
                    )
                    EpisodeDetails(
                        episode = episode,
                        modifier = Modifier
                            .weight(0.5f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(
                                start = contentPadding,
                                end = contentPadding,
                                top = 96.dp,
                                bottom = 32.dp
                            )
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    MediaHero(
                        imageUrl = episode.heroImageUrl,
                        backgroundColor = backGroundColor,
                        height = 400.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    EpisodeDetails(
                        episode = episode,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = contentPadding)
                            .offset(y = (-48).dp)
                            .padding(bottom = 96.dp)
                    )
                }
            }

            EpisodeTopBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            )

            if (!isWide) {
                MediaFloatingPlayButton(
                    containerColor = colors.primary,
                    onContainerColor = colors.onPrimary,
                    onClick = playAction,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(20.dp)
                )
            }
        }
    }

}

private fun CastMember.toMediaCastMember() = MediaCastMember(
    name = name,
    role = role,
    imageUrl = imageUrl
)
