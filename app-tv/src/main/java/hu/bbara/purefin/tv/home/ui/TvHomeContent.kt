package hu.bbara.purefin.tv.home.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component1
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component2
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.feature.shared.home.ContinueWatchingItem
import hu.bbara.purefin.feature.shared.home.LibraryItem
import hu.bbara.purefin.feature.shared.home.NextUpItem
import hu.bbara.purefin.feature.shared.home.PosterItem
import org.jellyfin.sdk.model.UUID

@SuppressLint("RememberInComposition")
@Composable
fun TvHomeContent(
    libraries: List<LibraryItem>,
    libraryContent: Map<UUID, List<PosterItem>>,
    continueWatching: List<ContinueWatchingItem>,
    nextUp: List<NextUpItem>,
    onMovieSelected: (UUID) -> Unit,
    onSeriesSelected: (UUID) -> Unit,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
    modifier: Modifier = Modifier
) {
    val visibleLibraries = remember(libraries, libraryContent) {
        libraries.filter { libraryContent[it.id]?.isEmpty() != true }
    }

    val ( nextUpRef, continueWatchingRef ) = remember { FocusRequester.createRefs() }
    val libraryRefs = remember(libraryContent) {
        libraryContent.keys.associateWith { FocusRequester() }
    }


    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
        item {
            TvContinueWatchingSection(
                items = continueWatching,
                onMovieSelected = onMovieSelected,
                onEpisodeSelected = onEpisodeSelected,
                modifier = Modifier.focusRequester(continueWatchingRef)
                    .focusProperties{
                        down = nextUpRef
                    }
            )
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            TvNextUpSection(
                items = nextUp,
                onEpisodeSelected = onEpisodeSelected,
                modifier = Modifier.focusRequester(nextUpRef)
                    .focusProperties{
                        up = continueWatchingRef
                        libraryRefs.values.firstOrNull()?.let { down = it }
                    }
            )
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        items(
            items = visibleLibraries,
            key = { it.id }
        ) { item ->
            val ref = libraryRefs[item.id]
            TvLibraryPosterSection(
                title = item.name,
                items = libraryContent[item.id] ?: emptyList(),
                action = "See All",
                onMovieSelected = onMovieSelected,
                onSeriesSelected = onSeriesSelected,
                onEpisodeSelected = onEpisodeSelected,
                modifier = if (ref != null) Modifier.focusRequester(ref) else Modifier
                    .focusProperties {
                        up = nextUpRef
                        libraryRefs.values.dropWhile { it != ref }.drop(1).firstOrNull()?.let { down = it }
                    }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
