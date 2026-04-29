package hu.bbara.purefin.ui.screen.movie

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
import hu.bbara.purefin.feature.content.movie.MovieScreenViewModel
import hu.bbara.purefin.model.Movie
import hu.bbara.purefin.navigation.MovieDto
import hu.bbara.purefin.ui.common.media.MediaDetailOverviewSection
import hu.bbara.purefin.ui.common.media.MediaDetailPlaybackSection
import hu.bbara.purefin.ui.common.media.MediaDetailSectionTitle
import hu.bbara.purefin.ui.common.media.TvMediaDetailBodyBox
import hu.bbara.purefin.ui.common.media.TvMediaDetailScaffold
import hu.bbara.purefin.ui.common.media.tvMediaDetailBackgroundImageUrl
import hu.bbara.purefin.ui.screen.movie.components.TvMovieHeroSection
import hu.bbara.purefin.ui.screen.waiting.PurefinWaitingScreen

@Composable
fun TvMovieScreen(
    movie: MovieDto,
    viewModel: MovieScreenViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    LaunchedEffect(movie.id) {
        viewModel.selectMovie(movie.id)
    }

    val movieItem = viewModel.movie.collectAsState()

    if (movieItem.value != null) {
        TvMovieScreenContent(
            movie = movieItem.value!!,
            onPlay = viewModel::onPlay,
            modifier = modifier
        )
    } else {
        PurefinWaitingScreen()
    }
}

@Composable
internal fun TvMovieScreenContent(
    movie: Movie,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val playFocusRequester = remember { FocusRequester() }

    LaunchedEffect(movie.id) {
        withFrameNanos { }
        playFocusRequester.requestFocus()
    }

    TvMediaDetailScaffold(
        resetScrollKey = movie.id,
        modifier = modifier
    ) {
        TvMediaDetailBodyBox(
            backgroundImageUrl = tvMediaDetailBackgroundImageUrl(movie.imageUrlPrefix),
            modifier = it
        ) {
            TvMovieHeroSection(
                movie = movie,
                onPlay = onPlay,
                playFocusRequester = playFocusRequester,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(2.dp))
        }
        Column(modifier = it.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(4.dp))
            MediaDetailOverviewSection(
                synopsis = movie.synopsis,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        Column(modifier = it.fillMaxWidth()) {
            MediaDetailPlaybackSection(
                audioTrack = "ENG",
                subtitles = "ENG",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (movie.cast.isNotEmpty()) {
            Column(modifier = it.fillMaxWidth()) {
                MediaDetailSectionTitle(text = "Cast")
                Spacer(modifier = Modifier.height(14.dp))
//                    MediaCastRow(cast = movie.cast)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
