package hu.bbara.purefin.ui.screen.movie

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.bbara.purefin.core.download.DownloadState
import hu.bbara.purefin.core.feature.content.movie.MovieScreenViewModel
import hu.bbara.purefin.core.image.ArtworkKind
import hu.bbara.purefin.core.image.ImageUrlBuilder
import hu.bbara.purefin.core.navigation.MovieDto
import hu.bbara.purefin.model.CastMember
import hu.bbara.purefin.model.Movie
import hu.bbara.purefin.ui.common.media.MediaDetailScaffold
import hu.bbara.purefin.ui.common.media.MediaMetadataFlowRow
import hu.bbara.purefin.ui.common.permission.rememberNotificationPermissionGate
import hu.bbara.purefin.ui.screen.movie.components.MovieDetails
import hu.bbara.purefin.ui.screen.movie.components.MovieTopBar
import hu.bbara.purefin.ui.screen.waiting.PurefinWaitingScreen
import hu.bbara.purefin.ui.theme.AppTheme
import java.util.UUID

@Composable
fun MovieScreen(
    movie: MovieDto,
    viewModel: MovieScreenViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    LaunchedEffect(movie) {
        viewModel.selectMovie(movie)
    }

    val movieItem = viewModel.movie.collectAsStateWithLifecycle()
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
    MediaDetailScaffold(
        imageUrl = ImageUrlBuilder.finishImageUrl(movie.imageUrlPrefix, ArtworkKind.PRIMARY),
        modifier = modifier,
        topBar = {
            MovieTopBar(onBack = onBack)
        },
        heroContent = {
            MovieHeroContent(movie = movie)
        }
    ) {
        MovieDetails(
            movie = movie,
            downloadState = downloadState,
            onDownloadClick = onDownloadClick,
        )
    }
}

@Composable
private fun MovieHeroContent(
    movie: Movie,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Text(
        text = movie.title,
        color = scheme.onBackground,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 38.sp
    )
//    Spacer(modifier = Modifier.height(8.dp))
    MediaMetadataFlowRow(
        items = listOf(movie.year, movie.rating, movie.runtime, movie.format),
        highlightedItem = movie.format
    )
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
        imageUrlPrefix = "https://images.unsplash.com/photo-1519608487953-e999c86e7455",
        cast = listOf(
            CastMember("Ryan Gosling", "K", null),
            CastMember("Ana de Armas", "Joi", null),
            CastMember("Harrison Ford", "Rick Deckard", null)
        )
    )
