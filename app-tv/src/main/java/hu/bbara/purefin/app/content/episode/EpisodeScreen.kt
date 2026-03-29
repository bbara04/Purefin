package hu.bbara.purefin.app.content.episode

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.common.ui.MediaCastRow
import hu.bbara.purefin.common.ui.PurefinWaitingScreen
import hu.bbara.purefin.common.ui.components.MediaDetailHeaderRow
import hu.bbara.purefin.common.ui.components.MediaDetailSectionTitle
import hu.bbara.purefin.common.ui.components.TvMediaDetailScaffold
import hu.bbara.purefin.core.data.navigation.EpisodeDto
import hu.bbara.purefin.core.data.navigation.LocalNavigationBackStack
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
    val backStack = LocalNavigationBackStack.current
    val previousRoute = remember(backStack) { backStack.getOrNull(backStack.lastIndex - 1) }

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

    EpisodeScreenContent(
        episode = selectedEpisode,
        seriesTitle = seriesTitle.value,
        topBarShortcut = remember(previousRoute, viewModel) {
            episodeTopBarShortcut(
                previousRoute = previousRoute,
                onSeriesClick = viewModel::onSeriesClick
            )
        },
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

@Composable
internal fun EpisodeScreenContent(
    episode: Episode,
    seriesTitle: String?,
    topBarShortcut: EpisodeTopBarShortcut?,
    onBack: () -> Unit,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val playFocusRequester = remember { FocusRequester() }

    LaunchedEffect(episode.id) {
        playFocusRequester.requestFocus()
    }

    TvMediaDetailScaffold(
        heroImageUrl = episode.heroImageUrl,
        modifier = modifier,
        topBar = {
            EpisodeTopBar(
                onBack = onBack,
                shortcut = topBarShortcut,
                downFocusRequester = playFocusRequester,
                modifier = Modifier.align(Alignment.TopStart)
            )
        },
        heroContent = {
            MediaDetailHeaderRow(
                leftContent = { headerModifier ->
                    EpisodeHeroSection(
                        episode = episode,
                        seriesTitle = seriesTitle,
                        onPlay = onPlay,
                        playFocusRequester = playFocusRequester,
                        modifier = headerModifier
                    )
                },
                rightContent = { panelModifier ->
                    EpisodeOverviewPanel(
                        episode = episode,
                        modifier = panelModifier
                    )
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    ) {
        if (episode.cast.isNotEmpty()) {
            item {
                Column(modifier = it) {
                    Spacer(modifier = Modifier.height(8.dp))
                    MediaDetailSectionTitle(text = "Cast")
                    Spacer(modifier = Modifier.height(14.dp))
                    MediaCastRow(cast = episode.cast)
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
