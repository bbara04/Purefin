package hu.bbara.purefin.ui.screen.episode

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.feature.content.episode.EpisodeScreenViewModel
import hu.bbara.purefin.image.ArtworkKind
import hu.bbara.purefin.image.ImageUrlBuilder
import hu.bbara.purefin.model.Episode
import hu.bbara.purefin.navigation.EpisodeDto
import hu.bbara.purefin.navigation.LocalNavigationManager
import hu.bbara.purefin.navigation.Route
import hu.bbara.purefin.ui.common.media.MediaDetailOverviewSection
import hu.bbara.purefin.ui.common.media.MediaDetailPlaybackSection
import hu.bbara.purefin.ui.common.media.MediaDetailSectionTitle
import hu.bbara.purefin.ui.common.media.TvMediaDetailBodyBox
import hu.bbara.purefin.ui.common.media.TvMediaDetailScaffold
import hu.bbara.purefin.ui.screen.episode.components.TvEpisodeHeroSection
import hu.bbara.purefin.ui.screen.waiting.PurefinWaitingScreen

@Composable
fun TvEpisodeScreen(
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
    val seriesTitle = viewModel.seriesTitle.collectAsState()
    val selectedEpisode = episode.value

    if (selectedEpisode == null) {
        PurefinWaitingScreen()
        return
    }

    TvEpisodeScreenContent(
        episode = selectedEpisode,
        seriesTitle = seriesTitle.value,
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

@Composable
internal fun TvEpisodeScreenContent(
    episode: Episode,
    seriesTitle: String?,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val playFocusRequester = remember { FocusRequester() }

    LaunchedEffect(episode.id) {
        withFrameNanos { }
        playFocusRequester.requestFocus()
    }

    TvMediaDetailScaffold(
        resetScrollKey = episode.id,
        modifier = modifier
    ) {
        item(key = "episode-hero") {
            TvMediaDetailBodyBox(
                backgroundImageUrl = ImageUrlBuilder.finishImageUrl(episode.imageUrlPrefix, ArtworkKind.PRIMARY),
                modifier = it
            ) {
                TvEpisodeHeroSection(
                    episode = episode,
                    seriesTitle = seriesTitle,
                    onPlay = onPlay,
                    playFocusRequester = playFocusRequester,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
        item(key = "episode-overview") {
            Column(modifier = it.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(16.dp))
                MediaDetailOverviewSection(
                    synopsis = episode.synopsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
        item(key = "episode-playback") {
            Column(modifier = it.fillMaxWidth()) {
                MediaDetailPlaybackSection(
                    audioTrack = "ENG",
                    subtitles = "ENG",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
        if (episode.cast.isNotEmpty()) {
            item(key = "episode-cast") {
                Column(modifier = it.fillMaxWidth()) {
                    MediaDetailSectionTitle(text = "Cast")
                    Spacer(modifier = Modifier.height(14.dp))
//                    MediaCastRow(cast = episode.cast)
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
