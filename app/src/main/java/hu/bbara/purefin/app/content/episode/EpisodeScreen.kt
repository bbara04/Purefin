package hu.bbara.purefin.app.content.episode

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun EpisodeScreen(
    seriesId: String,
    modifier: Modifier = Modifier
) {
    EpisodeCard(
        seriesId = seriesId,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun EpisodeScreenPreview() {
    EpisodeScreen(seriesId = "test")
}
