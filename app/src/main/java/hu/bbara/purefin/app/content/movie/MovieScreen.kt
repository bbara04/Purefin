package hu.bbara.purefin.app.content.movie

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.common.ui.PurefinWaitingScreen
import hu.bbara.purefin.common.ui.components.MediaHero
import hu.bbara.purefin.core.data.navigation.MovieDto
import hu.bbara.purefin.core.model.CastMember
import hu.bbara.purefin.core.model.Movie
import hu.bbara.purefin.feature.download.DownloadState
import hu.bbara.purefin.feature.shared.content.movie.MovieScreenViewModel
import hu.bbara.purefin.ui.theme.AppTheme
import java.util.UUID

@Composable
fun MovieScreen(
    movie: MovieDto, viewModel: MovieScreenViewModel = hiltViewModel(), modifier: Modifier = Modifier
) {
    LaunchedEffect(movie.id) {
        viewModel.selectMovie(movie.id)
    }

    val movieItem = viewModel.movie.collectAsState()
    val downloadState = viewModel.downloadState.collectAsState()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
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

    if (movieItem.value != null) {
        MovieScreenInternal(
            movie = movieItem.value!!,
            downloadState = downloadState.value,
            onDownloadClick = onDownloadClick,
            onBack = viewModel::onBack,
            modifier = modifier
        )
    } else {
        PurefinWaitingScreen()
    }
}

@Composable
private fun MovieScreenInternal(
    movie: Movie,
    downloadState: DownloadState = DownloadState.NotDownloaded,
    onDownloadClick: () -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MovieTopBar(
                onBack = onBack,
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
                imageUrl = movie.heroImageUrl,
                backgroundColor = MaterialTheme.colorScheme.background,
                heightFraction = 0.30f,
                modifier = Modifier.fillMaxWidth()
            )
            MovieDetails(
                movie = movie,
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
private fun MovieScreenPreview() {
    AppTheme {
        MovieScreenInternal(
            movie = previewMovie(),
            downloadState = DownloadState.NotDownloaded,
            onDownloadClick = {},
            onBack = {}
        )
    }
}

private fun previewMovie(): Movie =
    Movie(
        id = UUID.fromString("44444444-4444-4444-4444-444444444444"),
        libraryId = UUID.fromString("55555555-5555-5555-5555-555555555555"),
        title = "Blade Runner 2049",
        progress = 18.0,
        watched = false,
        year = "2017",
        rating = "16+",
        runtime = "2h 44m",
        format = "Dolby Vision",
        synopsis = "A new blade runner uncovers a buried secret that forces him to trace the vanished footsteps of Rick Deckard.",
        heroImageUrl = "https://images.unsplash.com/photo-1519608487953-e999c86e7455",
        audioTrack = "English 5.1",
        subtitles = "English CC",
        cast = listOf(
            CastMember("Ryan Gosling", "K", null),
            CastMember("Ana de Armas", "Joi", null),
            CastMember("Harrison Ford", "Rick Deckard", null)
        )
    )
