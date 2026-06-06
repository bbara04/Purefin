package hu.bbara.purefin.ui.screen.movie

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
import hu.bbara.purefin.core.feature.content.movie.MovieScreenViewModel
import hu.bbara.purefin.core.image.ArtworkKind
import hu.bbara.purefin.core.image.ImageUrlBuilder
import hu.bbara.purefin.core.navigation.MovieDto
import hu.bbara.purefin.model.CastMember
import hu.bbara.purefin.model.Movie
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
import hu.bbara.purefin.ui.screen.movie.components.MovieDownloadButtonTag
import hu.bbara.purefin.ui.screen.movie.components.MoviePlayButtonTag
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovieScreenInternal(
    movie: Movie,
    downloadState: DownloadState = DownloadState.NotDownloaded,
    onDownloadClick: () -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val playAction = remember(movie.id) {
        {
            val intent = Intent(context, PlayerActivity::class.java)
            intent.putExtra("MEDIA_ID", movie.id.toString())
            context.startActivity(intent)
        }
    }

    MediaDetailScaffold(
        uiModel = movie.toMediaDetailScaffoldUiModel(
            onPlayClick = playAction,
            downloadState = downloadState,
            onDownloadClick = onDownloadClick
        ),
        modifier = modifier,
        topBar = { scrollBehavior ->
            MovieTopBar(
                onBack = onBack,
                scrollBehavior = scrollBehavior
            )
        },
    )
}

private fun Movie.toMediaDetailScaffoldUiModel(
    onPlayClick: () -> Unit,
    downloadState: DownloadState,
    onDownloadClick: () -> Unit,
): MediaDetailScaffoldUiModel = MediaDetailScaffoldUiModel(
    imageUrl = ImageUrlBuilder.finishImageUrl(imageUrlPrefix, ArtworkKind.PRIMARY),
    title = title,
    metadataItems = listOf(year, rating, runtime, format),
    highlightedMetadataItem = format,
    actions = MediaDetailActionsUiModel(
        primaryAction = MediaDetailPrimaryActionUiModel(
            text = mediaPlayButtonText(progress, watched),
            progress = mediaPlaybackProgress(progress),
            onClick = onPlayClick,
            testTag = MoviePlayButtonTag
        ),
        secondaryActions = listOf(
            MediaDetailSecondaryActionUiModel.Download(
                downloadState = downloadState,
                onClick = onDownloadClick,
                testTag = MovieDownloadButtonTag
            )
        ),
        dividerThickness = 4.dp
    ),
    synopsis = MediaDetailSynopsisUiModel(text = synopsis),
    playbackSettings = MediaDetailPlaybackSettingsUiModel(
        audioTrack = "ENG",
        subtitles = "ENG"
    ),
    cast = if (cast.isNotEmpty()) {
        MediaDetailCastUiModel(
            members = cast,
            topSpacing = 24.dp
        )
    } else {
        null
    }
)

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
