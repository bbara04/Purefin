package hu.bbara.purefin.app.content.movie

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.common.ui.PurefinWaitingScreen
import hu.bbara.purefin.navigation.ItemDto

@Composable
fun MovieScreen(
    movie: ItemDto,
    viewModel: MovieScreenViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    LaunchedEffect(movie.id) {
        viewModel.selectMovie(movie.id)
    }

    val movieItem = viewModel.movie.collectAsState()

    if (movieItem.value != null) {
        MovieCard(
            movie = movieItem.value!!,
            modifier = modifier
        )
    } else {
        PurefinWaitingScreen()
    }
}
