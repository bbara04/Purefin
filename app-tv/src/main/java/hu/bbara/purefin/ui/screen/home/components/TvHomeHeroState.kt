package hu.bbara.purefin.ui.screen.home.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import hu.bbara.purefin.core.ui.model.MediaUiModel
import hu.bbara.purefin.feature.browse.home.FocusableItem
import hu.bbara.purefin.feature.browse.home.LibraryItem
import java.util.UUID

//TODO throw this out and simplify it.

internal data class TvHomeItemRegistry(
    val visibleLibraries: List<LibraryItem>,
    private val libraryContent: Map<UUID, List<MediaUiModel>>,
    private val continueWatching: List<MediaUiModel>,
    private val nextUp: List<MediaUiModel>,
    val firstAvailableItemId: UUID?,
) {
    val firstAvailableItem: MediaUiModel?
        get() = firstAvailableItemId?.let(::itemById)

    fun itemById(id: UUID): MediaUiModel? {
        return continueWatching.firstOrNull { it.id == id }
            ?: nextUp.firstOrNull { it.id == id }
            ?: visibleLibraries.asSequence()
                .mapNotNull { library -> libraryContent[library.id] }
                .flatten()
                .firstOrNull { it.id == id }
    }
}

internal class TvHomeHeroState(
    val focusedHero: MediaUiModel?,
    val onMediaFocused: (FocusableItem) -> Unit,
)

@Composable
internal fun rememberTvHomeItemRegistry(
    libraries: List<LibraryItem>,
    libraryContent: Map<UUID, List<MediaUiModel>>,
    continueWatching: List<MediaUiModel>,
    nextUp: List<MediaUiModel>,
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
    libraryContent: Map<UUID, List<MediaUiModel>>,
    continueWatching: List<MediaUiModel>,
    nextUp: List<MediaUiModel>,
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
    libraryContent: Map<UUID, List<MediaUiModel>>,
    continueWatching: List<MediaUiModel>,
    nextUp: List<MediaUiModel>,
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
        (focusedItem ?: itemRegistry.firstAvailableItem)
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
