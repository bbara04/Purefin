package hu.bbara.purefin.ui.screen.episode

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.core.download.DownloadState
import hu.bbara.purefin.core.feature.content.episode.EpisodeScreenViewModel
import hu.bbara.purefin.core.image.ArtworkKind
import hu.bbara.purefin.core.image.ImageUrlBuilder
import hu.bbara.purefin.core.navigation.EpisodeDto
import hu.bbara.purefin.core.navigation.Route
import hu.bbara.purefin.model.CastMember
import hu.bbara.purefin.model.Episode
import hu.bbara.purefin.navigation.LocalNavigationBackStack
import hu.bbara.purefin.ui.common.media.MediaDetailScaffold
import hu.bbara.purefin.ui.common.media.MediaMetadataFlowRow
import hu.bbara.purefin.ui.screen.episode.components.EpisodeDetails
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

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Proceed with download regardless — notification is nice-to-have
        viewModel.onDownloadClick()
    }

    val onDownloadClick = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            && downloadState.value is DownloadState.NotDownloaded
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
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

@Composable
private fun EpisodeScreenInternal(
    episode: Episode,
    topBarShortcut: EpisodeTopBarShortcut?,
    downloadState: DownloadState,
    onBack: () -> Unit,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MediaDetailScaffold(
        imageUrl = ImageUrlBuilder.finishImageUrl(episode.imageUrlPrefix, ArtworkKind.PRIMARY),
        modifier = modifier,
        topBar = {
            EpisodeTopBar(
                shortcut = topBarShortcut,
                onBack = onBack,
            )
        },
        heroContent = {
            EpisodeHeroContent(episode = episode)
        }
    ) {
        EpisodeDetails(
            episode = episode,
            downloadState = downloadState,
            onDownloadClick = onDownloadClick,
        )
    }
}

@Composable
private fun EpisodeHeroContent(
    episode: Episode,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme

    Text(
        text = episode.title,
        color = scheme.onBackground,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 38.sp
    )
    Text(
        text = "Episode ${episode.index}",
        color = scheme.onBackground,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium
    )
    EpisodeMetaChips(episode = episode)
}

@Composable
private fun EpisodeMetaChips(episode: Episode) {
    MediaMetadataFlowRow(
        items = listOf(episode.releaseDate, episode.rating, episode.runtime, episode.format),
        highlightedItem = episode.format
    )
}

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
