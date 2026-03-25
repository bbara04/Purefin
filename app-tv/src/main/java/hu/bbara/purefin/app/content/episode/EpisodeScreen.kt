package hu.bbara.purefin.app.content.episode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.common.ui.MediaCastRow
import hu.bbara.purefin.common.ui.MediaMetaChip
import hu.bbara.purefin.common.ui.MediaSynopsis
import hu.bbara.purefin.common.ui.PurefinWaitingScreen
import hu.bbara.purefin.common.ui.components.MediaActionButton
import hu.bbara.purefin.common.ui.components.MediaHero
import hu.bbara.purefin.common.ui.components.MediaPlaybackSettings
import hu.bbara.purefin.common.ui.components.MediaResumeButton
import hu.bbara.purefin.core.data.navigation.EpisodeDto
import hu.bbara.purefin.core.data.navigation.LocalNavigationManager
import hu.bbara.purefin.core.data.navigation.Route
import hu.bbara.purefin.core.model.Episode
import hu.bbara.purefin.feature.shared.content.episode.EpisodeScreenViewModel

@Composable
fun EpisodeScreen(
    episode: EpisodeDto,
    viewModel: EpisodeScreenViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val navigationManager = LocalNavigationManager.current

    LaunchedEffect(episode) {
        viewModel.selectEpisode(
            seriesId = episode.seriesId,
            seasonId = episode.seasonId,
            episodeId = episode.id
        )
    }

    val episode = viewModel.episode.collectAsState()
    val selectedEpisode = episode.value

    if (selectedEpisode == null) {
        PurefinWaitingScreen()
        return
    }

    EpisodeScreenInternal(
        episode = selectedEpisode,
        onBack = viewModel::onBack,
        onPlay = remember(selectedEpisode.id, navigationManager) {
            {
                navigationManager.navigate(
                    Route.PlayerRoute(mediaId = selectedEpisode.id.toString())
                )
            }
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EpisodeScreenInternal(
    episode: Episode,
    onBack: () -> Unit,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val hPad = Modifier.padding(horizontal = 16.dp)
    val playFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        playFocusRequester.requestFocus()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
    ) {
        item {
            Box {
                MediaHero(
                    imageUrl = episode.heroImageUrl,
                    backgroundColor = scheme.background,
                    heightFraction = 0.30f,
                    modifier = Modifier.fillMaxWidth()
                )
                EpisodeTopBar(onBack = onBack)
            }
        }
        item {
            Column(modifier = hPad) {
                Text(
                    text = episode.title,
                    color = scheme.onBackground,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 38.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Episode ${episode.index}",
                    color = scheme.onBackground,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(16.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MediaMetaChip(text = episode.releaseDate)
                    MediaMetaChip(text = episode.rating)
                    MediaMetaChip(text = episode.runtime)
                    MediaMetaChip(
                        text = episode.format,
                        background = scheme.primary.copy(alpha = 0.2f),
                        border = scheme.primary.copy(alpha = 0.3f),
                        textColor = scheme.primary
                    )
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
            MediaSynopsis(
                synopsis = episode.synopsis,
                modifier = hPad
            )
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = hPad) {
                MediaResumeButton(
                    text = if (episode.progress == null) "Play" else "Resume",
                    progress = episode.progress?.div(100)?.toFloat() ?: 0f,
                    onClick = onPlay,
                    modifier = Modifier.sizeIn(maxWidth = 200.dp).focusRequester(playFocusRequester)
                )
                VerticalDivider(
                    color = scheme.secondary,
                    thickness = 4.dp,
                    modifier = Modifier
                        .height(48.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Row {
                    MediaActionButton(
                        backgroundColor = scheme.secondary,
                        iconColor = scheme.onSecondary,
                        icon = Icons.Outlined.Add,
                        height = 48.dp
                    )
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
            MediaPlaybackSettings(
                backgroundColor = scheme.surface,
                foregroundColor = scheme.onSurface,
                audioTrack = "ENG",
                subtitles = "ENG",
                modifier = hPad
            )
        }
        if (episode.cast.isNotEmpty()) {
            item {
                Column(modifier = hPad) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Cast",
                        color = scheme.onBackground,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    MediaCastRow(cast = episode.cast)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
