package hu.bbara.purefin.tv.home

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import hu.bbara.purefin.feature.shared.home.ContinueWatchingItem
import hu.bbara.purefin.feature.shared.home.FocusableItem
import hu.bbara.purefin.feature.shared.home.LibraryItem
import hu.bbara.purefin.feature.shared.home.NextUpItem
import hu.bbara.purefin.feature.shared.home.PosterItem
import hu.bbara.purefin.tv.home.ui.TvHomeContent
import org.jellyfin.sdk.model.UUID

@SuppressLint("RememberInComposition")
@Composable
fun TvHomeScreen(
    libraries: List<LibraryItem>,
    libraryContent: Map<UUID, List<PosterItem>>,
    continueWatching: List<ContinueWatchingItem>,
    nextUp: List<NextUpItem>,
    serverUrl: String,
    onMovieSelected: (UUID) -> Unit,
    onSeriesSelected: (UUID) -> Unit,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
    modifier: Modifier = Modifier,
) {
    var focusableItem by remember { mutableStateOf<FocusableItem?>(null) }

    TvHomeContent(
        libraries = libraries,
        libraryContent = libraryContent,
        continueWatching = continueWatching,
        nextUp = nextUp,
        onMediaFocused = { item: FocusableItem ->
            focusableItem = item
        },
        onMovieSelected = onMovieSelected,
        onSeriesSelected = onSeriesSelected,
        onEpisodeSelected = onEpisodeSelected,
        modifier = modifier
    )

}
