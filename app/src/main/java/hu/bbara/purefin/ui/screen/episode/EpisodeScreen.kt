package hu.bbara.purefin.ui.screen.episode

import android.content.Intent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.bbara.purefin.core.download.DownloadState
import hu.bbara.purefin.core.feature.content.episode.EpisodeScreenViewModel
import hu.bbara.purefin.core.image.ArtworkKind
import hu.bbara.purefin.core.image.ImageUrlBuilder
import hu.bbara.purefin.core.navigation.EpisodeDto
import hu.bbara.purefin.core.navigation.Route
import hu.bbara.purefin.model.CastMember
import hu.bbara.purefin.model.Episode
import hu.bbara.purefin.navigation.LocalNavigationBackStack
import hu.bbara.purefin.player.PlayerActivity
import hu.bbara.purefin.ui.common.media.MediaDetailActionsUiModel
import hu.bbara.purefin.ui.common.media.MediaDetailCastUiModel
import hu.bbara.purefin.ui.common.media.MediaDetailPlaybackSettingsUiModel
import hu.bbara.purefin.ui.common.media.MediaDetailPrimaryActionUiModel
import hu.bbara.purefin.ui.common.media.MediaDetailScaffold
import hu.bbara.purefin.ui.common.media.MediaDetailScaffoldUiModel
import hu.bbara.purefin.ui.common.media.MediaDetailSecondaryActionUiModel
import hu.bbara.purefin.ui.common.media.MediaDetailSynopsisUiModel
import hu.bbara.purefin.ui.common.media.mediaPlayButtonText
import hu.bbara.purefin.ui.common.media.mediaPlaybackProgress
import hu.bbara.purefin.ui.common.permission.rememberNotificationPermissionGate
import hu.bbara.purefin.ui.screen.episode.components.EpisodeDownloadButtonTag
import hu.bbara.purefin.ui.screen.episode.components.EpisodePlayButtonTag
import hu.bbara.purefin.ui.screen.episode.components.EpisodeTopBar
import hu.bbara.purefin.ui.screen.episode.components.EpisodeTopBarShortcut
import hu.bbara.purefin.ui.screen.waiting.PurefinWaitingScreen
import hu.bbara.purefin.ui.theme.AppTheme
import java.util.UUID

@Composable
fun EpisodeScreen(
    episode: EpisodeDto,
    viewModel: EpisodeScreenViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val backStack = LocalNavigationBackStack.current
    val previousRoute = remember(backStack) { backStack.getOrNull(backStack.lastIndex - 1) }

    LaunchedEffect(episode) {
        viewModel.selectEpisode(episode)
    }

    val episode = viewModel.episode.collectAsStateWithLifecycle()
    val downloadState = viewModel.downloadState.collectAsStateWithLifecycle()
    val requestNotificationPermission = rememberNotificationPermissionGate()

    val onDownloadClick = {
        if (downloadState.value is DownloadState.NotDownloaded) {
            requestNotificationPermission {
                viewModel.onDownloadClick()
            }
        } else {
            viewModel.onDownloadClick()
        }
    }

    if (episode.value == null) {
        PurefinWaitingScreen()
        return
    }

    EpisodeScreenInternal(
        episode = episode.value!!,
        topBarShortcut = remember(previousRoute) {
            when (previousRoute) {
                Route.Home -> EpisodeTopBarShortcut.Series(viewModel::onSeriesClick)
                else -> null
            }
        },
        downloadState = downloadState.value,
        onBack = viewModel::onBack,
        onDownloadClick = onDownloadClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EpisodeScreenInternal(
    episode: Episode,
    topBarShortcut: EpisodeTopBarShortcut?,
    downloadState: DownloadState,
    onBack: () -> Unit,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val playAction = remember(episode.id) {
        {
            val intent = Intent(context, PlayerActivity::class.java)
            intent.putExtra("MEDIA_ID", episode.id.toString())
            context.startActivity(intent)
        }
    }

    MediaDetailScaffold(
        uiModel = episode.toMediaDetailScaffoldUiModel(
            onPlayClick = playAction,
            downloadState = downloadState,
            onDownloadClick = onDownloadClick
        ),
        modifier = modifier,
        topBar = { scrollBehavior ->
            EpisodeTopBar(
                shortcut = topBarShortcut,
                onBack = onBack,
                scrollBehavior = scrollBehavior
            )
        },
    )
}

private fun Episode.toMediaDetailScaffoldUiModel(
    onPlayClick: () -> Unit,
    downloadState: DownloadState,
    onDownloadClick: () -> Unit,
): MediaDetailScaffoldUiModel = MediaDetailScaffoldUiModel(
    imageUrl = ImageUrlBuilder.finishImageUrl(imageUrlPrefix, ArtworkKind.PRIMARY),
    title = title,
    subtitle = "Episode $index",
    metadataItems = listOf(releaseDate, rating, runtime, format),
    highlightedMetadataItem = format,
    actions = MediaDetailActionsUiModel(
        primaryAction = MediaDetailPrimaryActionUiModel(
            text = mediaPlayButtonText(progress, watched),
            progress = mediaPlaybackProgress(progress),
            onClick = onPlayClick,
            testTag = EpisodePlayButtonTag
        ),
        secondaryActions = listOf(
            MediaDetailSecondaryActionUiModel.Download(
                downloadState = downloadState,
                onClick = onDownloadClick,
                testTag = EpisodeDownloadButtonTag
            )
        ),
        dividerThickness = 2.dp
    ),
    synopsis = MediaDetailSynopsisUiModel(text = synopsis),
    playbackSettings = MediaDetailPlaybackSettingsUiModel(
        audioTrack = "ENG",
        subtitles = "ENG"
    ),
    cast = if (cast.isNotEmpty()) {
        MediaDetailCastUiModel(members = cast)
    } else {
        null
    }
)

@Preview(showBackground = true)
@Composable
private fun EpisodeScreenPreview() {
    AppTheme {
        EpisodeScreenInternal(
            episode = previewEpisode(),
            topBarShortcut = EpisodeTopBarShortcut.Series(onClick = {}),
            downloadState = DownloadState.Downloading(progressPercent = 0.42f),
            onBack = {},
            onDownloadClick = {}
        )
    }
}

private fun previewEpisode(): Episode {
    val seriesId = UUID.fromString("11111111-1111-1111-1111-111111111111")
    val seasonId = UUID.fromString("22222222-2222-2222-2222-222222222222")
    return Episode(
        id = UUID.fromString("33333333-3333-3333-3333-333333333333"),
        seriesId = seriesId,
        seriesName = "Severance",
        seasonId = seasonId,
        seasonIndex = 2,
        index = 4,
        title = "The You You Are",
        synopsis = "Mark is pulled deeper into Lumon's fractured world as the team chases a clue that reframes everything they thought they understood.",
        releaseDate = "2025",
        rating = "16+",
        runtime = "49m",
        progress = 63.0,
        watched = false,
        format = "4K",
        imageUrlPrefix = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee",
        cast = listOf(
            CastMember("Adam Scott", "Mark Scout", null),
            CastMember("Britt Lower", "Helly R.", null),
            CastMember("John Turturro", "Irving B.", null)
        )
    )
}
