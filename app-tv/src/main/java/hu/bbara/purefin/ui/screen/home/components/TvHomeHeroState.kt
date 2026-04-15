package hu.bbara.purefin.ui.screen.home.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import hu.bbara.purefin.feature.browse.home.ContinueWatchingItem
import hu.bbara.purefin.feature.browse.home.FocusableItem
import hu.bbara.purefin.feature.browse.home.LibraryItem
import hu.bbara.purefin.feature.browse.home.NextUpItem
import hu.bbara.purefin.feature.browse.home.PosterItem
import java.util.UUID

internal data class TvHomeItemRegistry(
    val visibleLibraries: List<LibraryItem>,
    private val libraryContent: Map<UUID, List<PosterItem>>,
    private val continueWatching: List<ContinueWatchingItem>,
    private val nextUp: List<NextUpItem>,
    val firstAvailableItemId: UUID?,
) {
    val firstAvailableItem: FocusableItem?
        get() = firstAvailableItemId?.let(::itemById)

    fun itemById(id: UUID): FocusableItem? {
        return continueWatching.firstOrNull { it.id == id }
            ?: nextUp.firstOrNull { it.id == id }
            ?: visibleLibraries.asSequence()
                .mapNotNull { library -> libraryContent[library.id] }
                .flatten()
                .firstOrNull { it.id == id }
    }
}

internal class TvHomeHeroState(
    val focusedHero: TvFocusedHeroModel?,
    val onMediaFocused: (FocusableItem) -> Unit,
)

@Composable
internal fun rememberTvHomeItemRegistry(
    libraries: List<LibraryItem>,
    libraryContent: Map<UUID, List<PosterItem>>,
    continueWatching: List<ContinueWatchingItem>,
    nextUp: List<NextUpItem>,
): TvHomeItemRegistry {
    return remember(libraries, libraryContent, continueWatching, nextUp) {
        createTvHomeItemRegistry(
            libraries = libraries,
            libraryContent = libraryContent,
            continueWatching = continueWatching,
            nextUp = nextUp
        )
    }
}

internal fun createTvHomeItemRegistry(
    libraries: List<LibraryItem>,
    libraryContent: Map<UUID, List<PosterItem>>,
    continueWatching: List<ContinueWatchingItem>,
    nextUp: List<NextUpItem>,
): TvHomeItemRegistry {
    val visibleLibraries = libraries.filter { libraryContent[it.id]?.isEmpty() != true }
    val firstAvailableItemId = continueWatching.firstOrNull()?.id
        ?: nextUp.firstOrNull()?.id
        ?: visibleLibraries.firstOrNull()?.id?.let { libraryId ->
            libraryContent[libraryId]?.firstOrNull()?.id
        }

    return TvHomeItemRegistry(
        visibleLibraries = visibleLibraries,
        libraryContent = libraryContent,
        continueWatching = continueWatching,
        nextUp = nextUp,
        firstAvailableItemId = firstAvailableItemId
    )
}

@Composable
internal fun rememberTvHomeHeroState(
    libraries: List<LibraryItem>,
    libraryContent: Map<UUID, List<PosterItem>>,
    continueWatching: List<ContinueWatchingItem>,
    nextUp: List<NextUpItem>,
): TvHomeHeroState {
    val itemRegistry = rememberTvHomeItemRegistry(
        libraries = libraries,
        libraryContent = libraryContent,
        continueWatching = continueWatching,
        nextUp = nextUp
    )
    var focusedItemId by remember { mutableStateOf<UUID?>(null) }
    val focusedItem = remember(focusedItemId, itemRegistry) {
        focusedItemId?.let(itemRegistry::itemById)
    }
    val focusedHero = remember(focusedItem, itemRegistry) {
        (focusedItem ?: itemRegistry.firstAvailableItem)?.toTvFocusedHeroModel()
    }
    val onMediaFocused: (FocusableItem) -> Unit = remember {
        { item ->
            focusedItemId = item.id
        }
    }

    return remember(focusedHero, onMediaFocused) {
        TvHomeHeroState(
            focusedHero = focusedHero,
            onMediaFocused = onMediaFocused
        )
    }
}
