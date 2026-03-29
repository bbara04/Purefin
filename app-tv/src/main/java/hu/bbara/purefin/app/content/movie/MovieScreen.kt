package hu.bbara.purefin.app.content.movie

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.common.ui.MediaCastRow
import hu.bbara.purefin.common.ui.PurefinWaitingScreen
import hu.bbara.purefin.common.ui.components.MediaDetailHeaderRow
import hu.bbara.purefin.common.ui.components.MediaDetailSectionTitle
import hu.bbara.purefin.common.ui.components.TvMediaDetailScaffold
import hu.bbara.purefin.core.data.navigation.MovieDto
import hu.bbara.purefin.core.model.Movie
import hu.bbara.purefin.feature.shared.content.movie.MovieScreenViewModel

@Composable
fun MovieScreen(
    movie: MovieDto, viewModel: MovieScreenViewModel = hiltViewModel(), modifier: Modifier = Modifier
) {
    LaunchedEffect(movie.id) {
        viewModel.selectMovie(movie.id)
    }

    val movieItem = viewModel.movie.collectAsState()

    if (movieItem.value != null) {
        MovieScreenContent(
            movie = movieItem.value!!,
            onBack = viewModel::onBack,
            onPlay = viewModel::onPlay,
            modifier = modifier
        )
    } else {
        PurefinWaitingScreen()
    }
}

@Composable
internal fun MovieScreenContent(
    movie: Movie,
    onBack: () -> Unit,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val playFocusRequester = remember { FocusRequester() }

    LaunchedEffect(movie.id) {
        playFocusRequester.requestFocus()
    }

    TvMediaDetailScaffold(
        heroImageUrl = movie.heroImageUrl,
        modifier = modifier,
        topBar = {
            MovieTopBar(
                onBack = onBack,
                downFocusRequester = playFocusRequester,
                modifier = Modifier.align(Alignment.TopStart)
            )
        },
        heroContent = {
            MediaDetailHeaderRow(
                leftContent = { headerModifier ->
                    MovieHeroSection(
                        movie = movie,
                        onPlay = onPlay,
                        playFocusRequester = playFocusRequester,
                        modifier = headerModifier
                    )
                },
                rightContent = { panelModifier ->
                    MovieOverviewPanel(
                        movie = movie,
                        modifier = panelModifier
                    )
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    ) {
        if (movie.cast.isNotEmpty()) {
            item {
                Column(modifier = it) {
                    Spacer(modifier = Modifier.height(8.dp))
                    MediaDetailSectionTitle(text = "Cast")
                    Spacer(modifier = Modifier.height(14.dp))
                    MediaCastRow(cast = movie.cast)
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
