package hu.bbara.purefin.ui.screen.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import hu.bbara.purefin.core.model.LibraryUiModel
import hu.bbara.purefin.core.model.MediaUiModel
import hu.bbara.purefin.core.model.MovieUiModel
import hu.bbara.purefin.ui.screen.home.components.TvFocusedItemHero
import hu.bbara.purefin.ui.screen.home.components.TvHomeContent
import hu.bbara.purefin.ui.screen.home.components.TvHomeHeroBackdrop
import java.util.UUID

@Composable
fun TvHomeScreen(
    libraries: List<LibraryUiModel>,
    libraryContent: Map<UUID, List<MediaUiModel>>,
    continueWatching: List<MediaUiModel>,
    nextUp: List<MediaUiModel>,
    onMediaSelected: (MediaUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val focusedMediaUiModel = remember { mutableStateOf<MediaUiModel>(MovieUiModel.createPlaceholder()) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = scheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            TvHomeHeroBackdrop(
                backdropImageUrl = focusedMediaUiModel.value.backdropImageUrl
            )
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                TvFocusedItemHero(
                    item = focusedMediaUiModel.value
                )
                TvHomeContent(
                    libraries = libraries,
                    libraryContent = libraryContent,
                    continueWatching = continueWatching,
                    nextUp = nextUp,
                    onMediaFocused = {
                        focusedMediaUiModel.value = it
                    },
                    onMediaSelected = onMediaSelected,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            }
        }
    }
}
