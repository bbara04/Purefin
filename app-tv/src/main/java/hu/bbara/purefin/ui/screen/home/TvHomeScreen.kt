package hu.bbara.purefin.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.core.ui.model.MediaUiModel
import hu.bbara.purefin.core.ui.model.MovieUiModel
import hu.bbara.purefin.feature.browse.home.LibraryItem
import hu.bbara.purefin.ui.screen.home.components.TvFocusedItemHero
import hu.bbara.purefin.ui.screen.home.components.TvHomeContent
import java.util.UUID

@Composable
fun TvHomeScreen(
    libraries: List<LibraryItem>,
    libraryContent: Map<UUID, List<MediaUiModel>>,
    continueWatching: List<MediaUiModel>,
    nextUp: List<MediaUiModel>,
    onMovieSelected: (UUID) -> Unit,
    onSeriesSelected: (UUID) -> Unit,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val focusedMediaUiModel = remember { mutableStateOf<MediaUiModel>(MovieUiModel.createPlaceholder()) }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TvFocusedItemHero(
                item = focusedMediaUiModel.value,
                height = 220.dp
            )
            TvHomeContent(
                libraries = libraries,
                libraryContent = libraryContent,
                continueWatching = continueWatching,
                nextUp = nextUp,
                onMediaFocused = {
                    focusedMediaUiModel.value = it
                },
                onMovieSelected = onMovieSelected,
                onSeriesSelected = onSeriesSelected,
                onEpisodeSelected = onEpisodeSelected,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        }
    }
}
