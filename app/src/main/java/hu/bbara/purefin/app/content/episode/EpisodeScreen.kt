package hu.bbara.purefin.app.content.episode

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.common.ui.PurefinWaitingScreen
import hu.bbara.purefin.common.ui.components.MediaHero
import hu.bbara.purefin.core.data.navigation.EpisodeDto
import hu.bbara.purefin.core.model.Episode
import hu.bbara.purefin.feature.download.DownloadState
import hu.bbara.purefin.feature.shared.content.episode.EpisodeScreenViewModel

@Composable
fun EpisodeScreen(
    episode: EpisodeDto,
    viewModel: EpisodeScreenViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {

    LaunchedEffect(episode) {
        viewModel.selectEpisode(
            seriesId = episode.seriesId,
            seasonId = episode.seasonId,
            episodeId = episode.id
        )
    }

    val episode = viewModel.episode.collectAsState()
    val seriesTitle = viewModel.seriesTitle.collectAsState()
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
        seriesTitle = seriesTitle.value,
        downloadState = downloadState.value,
        onBack = viewModel::onBack,
        onSeriesClick = viewModel::onSeriesClick,
        onPlaybackStarted = viewModel::onPlaybackStarted,
        onDownloadClick = onDownloadClick,
        modifier = modifier
    )
}

@Composable
private fun EpisodeScreenInternal(
    episode: Episode,
    seriesTitle: String?,
    downloadState: DownloadState,
    onBack: () -> Unit,
    onSeriesClick: () -> Unit,
    onPlaybackStarted: () -> Unit,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier,
) {

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            EpisodeTopBar(
                seriesTitle = seriesTitle,
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
                onPlaybackStarted = onPlaybackStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = innerPadding.calculateBottomPadding())
            )
        }
    }
}
