package hu.bbara.purefin.tv.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.feature.browse.home.ContinueWatchingItem
import hu.bbara.purefin.feature.browse.home.LibraryItem
import hu.bbara.purefin.feature.browse.home.NextUpItem
import hu.bbara.purefin.feature.browse.home.PosterItem
import hu.bbara.purefin.tv.home.ui.TvFocusedItemHero
import hu.bbara.purefin.tv.home.ui.TvHomeContent
import hu.bbara.purefin.tv.home.ui.rememberTvHomeHeroState
import java.util.UUID

private const val TvHomeHeroHeightFraction = 0.32f
private val TvHomeMinHeroHeight = 160.dp
private val TvHomeMaxHeroHeight = 200.dp

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
    val scheme = MaterialTheme.colorScheme
    val heroState = rememberTvHomeHeroState(
        libraries = libraries,
        libraryContent = libraryContent,
        continueWatching = continueWatching,
        nextUp = nextUp
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
    ) {
        val heroHeight = heroState.focusedHero?.let {
            (maxHeight * TvHomeHeroHeightFraction)
                .coerceIn(TvHomeMinHeroHeight, TvHomeMaxHeroHeight)
        }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            heroState.focusedHero?.let { hero ->
                TvFocusedItemHero(
                    item = hero,
                    height = heroHeight ?: TvHomeMinHeroHeight
                )
            }
            TvHomeContent(
                libraries = libraries,
                libraryContent = libraryContent,
                continueWatching = continueWatching,
                nextUp = nextUp,
                onMediaFocused = heroState.onMediaFocused,
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
