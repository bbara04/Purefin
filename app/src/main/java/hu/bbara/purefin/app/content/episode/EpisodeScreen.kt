package hu.bbara.purefin.app.content.episode

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.common.ui.PurefinWaitingScreen
import hu.bbara.purefin.common.ui.components.MediaHero
import hu.bbara.purefin.core.data.navigation.EpisodeDto
import hu.bbara.purefin.core.data.navigation.LocalNavigationBackStack
import hu.bbara.purefin.core.data.navigation.LocalNavigationManager
import hu.bbara.purefin.core.data.navigation.Route
import hu.bbara.purefin.core.model.Episode
import hu.bbara.purefin.core.model.CastMember
import hu.bbara.purefin.feature.download.DownloadState
import hu.bbara.purefin.feature.shared.content.episode.EpisodeScreenViewModel
import hu.bbara.purefin.ui.theme.AppTheme
import java.util.UUID

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
            MediaHero(
                imageUrl = episode.heroImageUrl,
                backgroundColor = MaterialTheme.colorScheme.background,
                heightFraction = 0.30f,
                modifier = Modifier.fillMaxWidth()
            )
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
        heroImageUrl = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee",
        cast = listOf(
            CastMember("Adam Scott", "Mark Scout", null),
            CastMember("Britt Lower", "Helly R.", null),
            CastMember("John Turturro", "Irving B.", null)
        )
    )
}
