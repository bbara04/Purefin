package hu.bbara.purefin.app.content.movie

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun MovieScreen(
    movieId: String,
    modifier: Modifier = Modifier
) {
    MovieCard(
        movieId = movieId,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun MovieScreenPreview() {
    MovieScreen(movieId = "test")
}
