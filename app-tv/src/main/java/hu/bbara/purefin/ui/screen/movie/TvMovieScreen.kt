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
import hu.bbara.purefin.ui.common.media.MediaCastRow
import hu.bbara.purefin.ui.screen.waiting.PurefinWaitingScreen
import hu.bbara.purefin.ui.common.media.MediaDetailOverviewSection
import hu.bbara.purefin.ui.common.media.MediaDetailPlaybackSection
import hu.bbara.purefin.ui.common.media.MediaDetailSectionTitle
import hu.bbara.purefin.ui.common.media.TvMediaDetailBodyBox
import hu.bbara.purefin.ui.common.media.TvMediaDetailScaffold
import hu.bbara.purefin.ui.common.media.tvMediaDetailBackgroundImageUrl
import hu.bbara.purefin.ui.screen.movie.components.TvMovieHeroSection
import hu.bbara.purefin.core.navigation.MovieDto
import hu.bbara.purefin.model.Movie
import hu.bbara.purefin.feature.content.movie.MovieScreenViewModel

@Composable
fun TvMovieScreen(
    movie: MovieDto, viewModel: MovieScreenViewModel = hiltViewModel(), modifier: Modifier = Modifier
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
        item(key = "movie-hero") {
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
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
        item(key = "movie-overview") {
            Column(modifier = it.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(16.dp))
                MediaDetailOverviewSection(
                    synopsis = movie.synopsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
        item(key = "movie-playback") {
            Column(modifier = it.fillMaxWidth()) {
                MediaDetailPlaybackSection(
                    audioTrack = movie.audioTrack,
                    subtitles = movie.subtitles,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
        if (movie.cast.isNotEmpty()) {
            item(key = "movie-cast") {
                Column(modifier = it.fillMaxWidth()) {
                    MediaDetailSectionTitle(text = "Cast")
                    Spacer(modifier = Modifier.height(14.dp))
                    MediaCastRow(cast = movie.cast)
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
