package hu.bbara.purefin.ui.screen.episode

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.ui.screen.waiting.PurefinWaitingScreen
import hu.bbara.purefin.ui.common.media.MediaHero
import hu.bbara.purefin.ui.common.media.MediaMetadataFlowRow
import hu.bbara.purefin.core.download.DownloadState
import hu.bbara.purefin.core.image.ImageUrlBuilder
import hu.bbara.purefin.core.navigation.EpisodeDto
import hu.bbara.purefin.navigation.LocalNavigationBackStack
import hu.bbara.purefin.core.navigation.Route
import hu.bbara.purefin.core.model.CastMember
import hu.bbara.purefin.core.model.Episode
import hu.bbara.purefin.feature.content.episode.EpisodeScreenViewModel
import hu.bbara.purefin.ui.screen.episode.components.EpisodeDetails
import hu.bbara.purefin.ui.screen.episode.components.EpisodeTopBar
import hu.bbara.purefin.ui.screen.episode.components.EpisodeTopBarShortcut
import hu.bbara.purefin.ui.theme.AppTheme
import hu.bbara.purefin.core.image.ArtworkKind
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
        viewModel.selectEpisode(
            seriesId = episode.seriesId,
            seasonId = episode.seasonId,
            episodeId = episode.id
        )
    }

    val episode = viewModel.episode.collectAsState()
    val downloadState = viewModel.downloadState.collectAsState()

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
        onSeriesClick = viewModel::onSeriesClick,
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
    onSeriesClick: () -> Unit,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier,
) {

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            EpisodeTopBar(
                shortcut = topBarShortcut,
                onBack = onBack,
                onSeriesClick = onSeriesClick,
                modifier = Modifier
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            EpisodeHeroSection(episode = episode)
            EpisodeDetails(
                episode = episode,
                downloadState = downloadState,
                onDownloadClick = onDownloadClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = innerPadding.calculateBottomPadding())
            )
        }
    }
}

@Composable
private fun EpisodeHeroSection(
    episode: Episode,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val sectionHeight = screenHeight * 0.4f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(sectionHeight)
    ) {
        MediaHero(
            imageUrl = ImageUrlBuilder.finishImageUrl(episode.imageUrlPrefix, ArtworkKind.PRIMARY),
            backgroundColor = scheme.background,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            scheme.background.copy(alpha = 0.5f),
                            scheme.background
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
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
            EpisodeMetaChips(episode = episode)
        }
    }
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
            onSeriesClick = {},
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
        seasonId = seasonId,
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
