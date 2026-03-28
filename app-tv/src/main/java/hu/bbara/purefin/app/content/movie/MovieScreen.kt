package hu.bbara.purefin.app.content.movie

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.common.ui.MediaCastRow
import hu.bbara.purefin.common.ui.MediaMetaChip
import hu.bbara.purefin.common.ui.MediaSynopsis
import hu.bbara.purefin.common.ui.PurefinWaitingScreen
import hu.bbara.purefin.common.ui.components.MediaHero
import hu.bbara.purefin.common.ui.components.MediaPlaybackSettings
import hu.bbara.purefin.common.ui.components.MediaResumeButton
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
        MovieScreenInternal(
            movie = movieItem.value!!,
            onBack = viewModel::onBack,
            onPlay = viewModel::onPlay,
            modifier = modifier
        )
    } else {
        PurefinWaitingScreen()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MovieScreenInternal(
    movie: Movie,
    onBack: () -> Unit,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val hPad = Modifier.padding(horizontal = 16.dp)
    val playFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        playFocusRequester.requestFocus()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                MediaHero(
                    imageUrl = movie.heroImageUrl,
                    backgroundColor = scheme.background,
                    heightFraction = 0.30f,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Column(modifier = hPad) {
                    Text(
                        text = movie.title,
                        color = scheme.onBackground,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 38.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MediaMetaChip(text = movie.year)
                        MediaMetaChip(text = movie.rating)
                        MediaMetaChip(text = movie.runtime)
                        MediaMetaChip(
                            text = movie.format,
                            background = scheme.primary.copy(alpha = 0.2f),
                            border = scheme.primary.copy(alpha = 0.3f),
                            textColor = scheme.primary
                        )
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
                MediaSynopsis(
                    synopsis = movie.synopsis,
                    modifier = hPad
                )
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = hPad) {
                    MediaResumeButton(
                        text = if (movie.progress == null) "Play" else "Resume",
                        progress = movie.progress?.div(100)?.toFloat() ?: 0f,
                        onClick = onPlay,
                        modifier = Modifier.sizeIn(maxWidth = 200.dp).focusRequester(playFocusRequester)
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
                MediaPlaybackSettings(
                    backgroundColor = scheme.surface,
                    foregroundColor = scheme.onSurface,
                    audioTrack = movie.audioTrack,
                    subtitles = movie.subtitles,
                    modifier = hPad
                )
            }
            if (movie.cast.isNotEmpty()) {
                item {
                    Column(modifier = hPad) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Cast",
                            color = scheme.onBackground,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        MediaCastRow(cast = movie.cast)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
        MovieTopBar(
            onBack = onBack,
            downFocusRequester = playFocusRequester,
            modifier = Modifier.align(Alignment.TopStart)
        )
    }
}
