package hu.bbara.purefin.app.content.movie

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.jellyfin.sdk.model.UUID

@Composable
fun MovieCard(
    movieId: String,
    modifier: Modifier = Modifier,
    viewModel: MovieScreenViewModel = hiltViewModel()
) {

    LaunchedEffect(movieId) {
        viewModel.selectMovie(UUID.fromString(movieId))
    }

    val movieItem = viewModel.movie.collectAsState()

    if (movieItem.value != null) {
        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .background(MovieBackgroundDark)
        ) {
            val isWide = maxWidth >= 900.dp
            val contentPadding = if (isWide) 32.dp else 20.dp

            Box(modifier = Modifier.fillMaxSize()) {
                if (isWide) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        MovieHero(
                            movie = movieItem.value!!,
                            height = 300.dp,
                            isWide = true,
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(0.5f)
                        )
                        MovieDetails(
                            movie = movieItem.value!!,
                            modifier = Modifier
                                .weight(0.5f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                                .padding(start = contentPadding, end = contentPadding, top = 96.dp, bottom = 32.dp)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        MovieHero(
                            movie = movieItem.value!!,
                            height = 400.dp,
                            isWide = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                        MovieDetails(
                            movie = movieItem.value!!,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = contentPadding)
                                .offset(y = (-48).dp)
                                .padding(bottom = 96.dp)
                        )
                    }
                }

                MovieTopBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                )

                if (!isWide) {
                    FloatingPlayButton(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(20.dp)
                    )
                }
            }
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MovieBackgroundDark),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Loading...",
                color = Color.White
            )
        }
    }
}
