package hu.bbara.purefin.app.content.movie

import android.content.Intent
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.app.content.ContentMockData
import hu.bbara.purefin.common.ui.MediaFloatingPlayButton
import hu.bbara.purefin.common.ui.PurefinWaitingScreen
import hu.bbara.purefin.common.ui.components.MediaHero
import hu.bbara.purefin.navigation.ItemDto
import hu.bbara.purefin.player.PlayerActivity

@Composable
fun MovieScreen(
    movie: ItemDto, viewModel: MovieScreenViewModel = hiltViewModel(), modifier: Modifier = Modifier
) {
    LaunchedEffect(movie.id) {
        viewModel.selectMovie(movie.id)
    }

    val movieItem = viewModel.movie.collectAsState()

    if (movieItem.value != null) {
        MovieScreenInternal(
            movie = movieItem.value!!, modifier = modifier
        )
    } else {
        PurefinWaitingScreen()
    }
}

@Composable
private fun MovieScreenInternal(
    movie: MovieUiModel,
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

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val isWide = maxWidth >= 900.dp
        val contentPadding = if (isWide) 32.dp else 20.dp

        Box(modifier = Modifier.fillMaxSize()) {
            if (isWide) {
                Row(modifier = Modifier.fillMaxSize()) {
                    MediaHero(
                        imageUrl = movie.heroImageUrl,
                        backgroundColor = MaterialTheme.colorScheme.background,
                        height = 300.dp,
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.5f)
                    )
                    MovieDetails(
                        movie = movie,
                        modifier = Modifier
                            .weight(0.5f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(
                                start = contentPadding,
                                end = contentPadding,
                                top = 96.dp,
                                bottom = 32.dp
                            )
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    MediaHero(
                        imageUrl = movie.heroImageUrl,
                        height = 400.dp,
                        backgroundColor = MaterialTheme.colorScheme.background,
                        modifier = Modifier.fillMaxWidth()
                    )
                    MovieDetails(
                        movie = movie,
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
                MediaFloatingPlayButton(
                    containerColor = MaterialTheme.colorScheme.primary,
                    onContainerColor = MaterialTheme.colorScheme.onPrimary,

                    onClick = playAction,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(20.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun MovieScreenPreview() {
    MovieScreenInternal(movie = ContentMockData.movie())
}
