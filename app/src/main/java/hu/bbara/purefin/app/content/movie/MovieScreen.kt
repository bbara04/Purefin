package hu.bbara.purefin.app.content.movie

import android.content.Intent
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
import androidx.compose.runtime.remember
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
            movie = movieItem.value!!,
            onBack = viewModel::onBack,
            modifier = modifier
        )
    } else {
        PurefinWaitingScreen()
    }
}

@Composable
private fun MovieScreenInternal(
    movie: MovieUiModel,
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

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MovieTopBar(
                onBack = onBack,
                modifier = Modifier
            )
        },
        floatingActionButton = {
            MediaFloatingPlayButton(
                containerColor = MaterialTheme.colorScheme.primary,
                onContainerColor = MaterialTheme.colorScheme.onPrimary,
                onClick = playAction,
                modifier = Modifier
                    .padding(16.dp)
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
                height = 200.dp,
                modifier = Modifier.fillMaxWidth()
            )
            MovieDetails(
                movie = movie,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = innerPadding.calculateBottomPadding())
            )
        }
    }
}


@Preview
@Composable
fun MovieScreenPreview() {
    MovieScreenInternal(
        movie = ContentMockData.movie(),
        onBack = {}
    )
}
