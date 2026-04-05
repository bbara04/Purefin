package hu.bbara.purefin.tv.home.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import hu.bbara.purefin.core.model.Movie
import hu.bbara.purefin.feature.shared.home.ContinueWatchingItem
import hu.bbara.purefin.feature.shared.home.LibraryItem
import hu.bbara.purefin.feature.shared.home.PosterItem
import hu.bbara.purefin.ui.theme.AppTheme
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.CollectionType
import org.junit.Rule
import org.junit.Test
import java.util.UUID

class TvHomeContentTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun tvHomeContent_focusesFirstAvailableItemByDefault() {
        composeRule.setContent {
            AppTheme {
                TvHomeContent(
                    libraries = sampleLibraries(),
                    libraryContent = sampleLibraryContent(),
                    continueWatching = sampleContinueWatching(),
                    nextUp = emptyList(),
                    onMediaFocused = {},
                    onMovieSelected = {},
                    onSeriesSelected = {},
                    onEpisodeSelected = { _, _, _ -> }
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TvHomeInitialFocusTag).assertIsFocused()
    }

    private fun sampleContinueWatching(): List<ContinueWatchingItem> {
        return listOf(
            ContinueWatchingItem(
                type = BaseItemKind.MOVIE,
                movie = Movie(
                    id = UUID.fromString("11111111-1111-1111-1111-111111111111"),
                    libraryId = UUID.fromString("22222222-2222-2222-2222-222222222222"),
                    title = "Blade Runner 2049",
                    progress = 42.0,
                    watched = false,
                    year = "2017",
                    rating = "16+",
                    runtime = "164m",
                    format = "4K",
                    synopsis = "Officer K uncovers a secret that sends him searching for Rick Deckard.",
                    imageUrlPrefix = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee",
                    audioTrack = "ENG 5.1",
                    subtitles = "ENG",
                    cast = emptyList()
                )
            )
        )
    }

    private fun sampleLibraries(): List<LibraryItem> {
        return listOf(
            LibraryItem(
                id = UUID.fromString("33333333-3333-3333-3333-333333333333"),
                name = "Movies",
                type = CollectionType.MOVIES,
                posterUrl = "",
                isEmpty = false
            )
        )
    }

    private fun sampleLibraryContent(): Map<UUID, List<PosterItem>> {
        val libraryId = UUID.fromString("33333333-3333-3333-3333-333333333333")

        return mapOf(
            libraryId to listOf(
                PosterItem(
                    type = BaseItemKind.MOVIE,
                    movie = Movie(
                        id = UUID.fromString("44444444-4444-4444-4444-444444444444"),
                        libraryId = libraryId,
                        title = "Arrival",
                        progress = null,
                        watched = false,
                        year = "2016",
                        rating = "12+",
                        runtime = "116m",
                        format = "4K",
                        synopsis = "A linguist works to communicate with mysterious visitors.",
                        imageUrlPrefix = "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa",
                        audioTrack = "ENG 5.1",
                        subtitles = "ENG",
                        cast = emptyList()
                    )
                )
            )
        )
    }
}
