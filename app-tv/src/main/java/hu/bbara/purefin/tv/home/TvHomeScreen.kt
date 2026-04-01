package hu.bbara.purefin.tv.home

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import hu.bbara.purefin.feature.shared.home.ContinueWatchingItem
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
    onMovieSelected: (UUID) -> Unit,
    onSeriesSelected: (UUID) -> Unit,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
    modifier: Modifier = Modifier
) {
    TvHomeContent(
        libraries = libraries,
        libraryContent = libraryContent,
        continueWatching = continueWatching,
        nextUp = nextUp,
        onMovieSelected = onMovieSelected,
        onSeriesSelected = onSeriesSelected,
        onEpisodeSelected = onEpisodeSelected,
        modifier = modifier
    )
}
