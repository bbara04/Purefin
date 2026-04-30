package hu.bbara.purefin.ui.screen.movie

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.feature.content.movie.MovieScreenViewModel
import hu.bbara.purefin.model.Movie
import hu.bbara.purefin.navigation.MovieDto
import hu.bbara.purefin.ui.common.media.MediaDetailHorizontalPadding
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
            modifier = Modifier.fillMaxSize(),
            heightFraction = 1f
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MediaDetailHorizontalPadding)
            ) {
                TvMovieHeroSection(
                    movie = movie,
                    onPlay = onPlay,
                    playFocusRequester = playFocusRequester,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
