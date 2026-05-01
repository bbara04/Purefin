package hu.bbara.purefin.ui.screen.home.components

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.model.Episode
import hu.bbara.purefin.model.LibraryKind
import hu.bbara.purefin.model.Movie
import hu.bbara.purefin.ui.model.EpisodeUiModel
import hu.bbara.purefin.ui.model.MediaUiModel
import hu.bbara.purefin.ui.model.MovieUiModel
import hu.bbara.purefin.ui.model.LibraryUiModel
import hu.bbara.purefin.ui.screen.home.TvHomeScreen
import hu.bbara.purefin.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalTestApi::class)
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

    @Test
    fun tvHomeScreen_showsHeroForInitiallyFocusedItem() {
        composeRule.setContent {
            AppTheme {
                TvHomeScreen(
                    libraries = sampleLibraries(),
                    libraryContent = sampleLibraryContent(),
                    continueWatching = sampleContinueWatching(),
                    nextUp = emptyList(),
                    onMovieSelected = {},
                    onSeriesSelected = {},
                    onEpisodeSelected = { _, _, _ -> }
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TvHomeInitialFocusTag).assertIsFocused()
        composeRule.onNodeWithTag(TvHomeHeroTitleTag).assertTextEquals("Blade Runner 2049")
    }

    @Test
    fun tvHomeScreen_updatesHeroWhenFocusMovesWithinRow() {
        composeRule.setContent {
            AppTheme {
                TvHomeScreen(
                    libraries = sampleLibraries(),
                    libraryContent = sampleLibraryContent(),
                    continueWatching = sampleContinueWatchingRow(),
                    nextUp = emptyList(),
                    onMovieSelected = {},
                    onSeriesSelected = {},
                    onEpisodeSelected = { _, _, _ -> }
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TvHomeHeroTitleTag).assertTextEquals("Blade Runner 2049")
        composeRule.onNodeWithTag(TvHomeInitialFocusTag)
            .assertIsFocused()
            .performKeyInput {
                pressKey(Key.DirectionRight)
            }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TvHomeHeroTitleTag).assertTextEquals("Mad Max: Fury Road")
    }

    @Test
    fun tvHomeScreen_compactHeroKeepsFirstRowVisible() {
        composeRule.setContent {
            AppTheme {
                Box(
                    modifier = Modifier.size(width = 960.dp, height = 540.dp)
                ) {
                    TvHomeScreen(
                        libraries = sampleLibraries(),
                        libraryContent = sampleLibraryContent(),
                        continueWatching = sampleContinueWatching(),
                        nextUp = emptyList(),
                        onMovieSelected = {},
                        onSeriesSelected = {},
                        onEpisodeSelected = { _, _, _ -> }
                    )
                }
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TvHomeHeroTitleTag).assertIsDisplayed()
        composeRule.onNodeWithText("Continue Watching").assertIsDisplayed()
        composeRule.onNodeWithTag(TvHomeInitialFocusTag)
            .assertIsDisplayed()
            .assertIsFocused()
    }

    @Test
    fun tvHomeScreen_movesFocusedSectionToTopOfContent() {
        composeRule.setContent {
            AppTheme {
                Box(
                    modifier = Modifier.size(width = 960.dp, height = 540.dp)
                ) {
                    TvHomeScreen(
                        libraries = sampleLibraries(),
                        libraryContent = sampleLibraryContent(),
                        continueWatching = sampleContinueWatching(),
                        nextUp = sampleNextUp(),
                        onMovieSelected = {},
                        onSeriesSelected = {},
                        onEpisodeSelected = { _, _, _ -> }
                    )
                }
            }
        }

        composeRule.waitForIdle()

        composeRule.onRoot().performKeyInput {
            pressKey(Key.DirectionDown)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TvHomeHeroTitleTag).assertTextEquals("The Train Job")
        assertSectionRowAlignedToViewportTop(TvHomeNextUpRowTag)

        composeRule.onRoot().performKeyInput {
            pressKey(Key.DirectionDown)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TvHomeHeroTitleTag).assertTextEquals("Arrival")
        assertSectionRowAlignedToViewportTop(tvHomeLibraryRowTag(sampleLibraryId()))
    }

    @Test
    fun tvHomeScreen_skipsMissingSectionAndAlignsLibraryToTop() {
        composeRule.setContent {
            AppTheme {
                Box(
                    modifier = Modifier.size(width = 960.dp, height = 540.dp)
                ) {
                    TvHomeScreen(
                        libraries = sampleLibraries(),
                        libraryContent = sampleLibraryContent(),
                        continueWatching = sampleContinueWatching(),
                        nextUp = emptyList(),
                        onMovieSelected = {},
                        onSeriesSelected = {},
                        onEpisodeSelected = { _, _, _ -> }
                    )
                }
            }
        }

        composeRule.waitForIdle()

        composeRule.onRoot().performKeyInput {
            pressKey(Key.DirectionDown)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TvHomeHeroTitleTag).assertTextEquals("Arrival")
        assertSectionRowAlignedToViewportTop(tvHomeLibraryRowTag(sampleLibraryId()))
    }

    @Test
    fun tvHomeScreen_movesPreviousSectionToTopWhenNavigatingUp() {
        composeRule.setContent {
            AppTheme {
                Box(
                    modifier = Modifier.size(width = 960.dp, height = 540.dp)
                ) {
                    TvHomeScreen(
                        libraries = sampleLibraries(),
                        libraryContent = sampleLibraryContent(),
                        continueWatching = sampleContinueWatching(),
                        nextUp = sampleNextUp(),
                        onMovieSelected = {},
                        onSeriesSelected = {},
                        onEpisodeSelected = { _, _, _ -> }
                    )
                }
            }
        }

        composeRule.waitForIdle()

        composeRule.onRoot().performKeyInput {
            pressKey(Key.DirectionDown)
            pressKey(Key.DirectionDown)
        }
        composeRule.waitForIdle()

        composeRule.onRoot().performKeyInput {
            pressKey(Key.DirectionUp)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TvHomeHeroTitleTag).assertTextEquals("The Train Job")
        assertSectionRowAlignedToViewportTop(TvHomeNextUpRowTag)
    }

    @Test
    fun tvHomeScreen_horizontalMoveDoesNotChangeAlignedSectionPosition() {
        composeRule.setContent {
            AppTheme {
                Box(
                    modifier = Modifier.size(width = 960.dp, height = 540.dp)
                ) {
                    TvHomeScreen(
                        libraries = sampleLibraries(),
                        libraryContent = sampleLibraryContent(),
                        continueWatching = sampleContinueWatching(),
                        nextUp = sampleNextUpRow(),
                        onMovieSelected = {},
                        onSeriesSelected = {},
                        onEpisodeSelected = { _, _, _ -> }
                    )
                }
            }
        }

        composeRule.waitForIdle()

        composeRule.onRoot().performKeyInput {
            pressKey(Key.DirectionDown)
        }
        composeRule.waitForIdle()

        assertSectionRowAlignedToViewportTop(TvHomeNextUpRowTag)
        val alignedTopBefore = rowTop(TvHomeNextUpRowTag)

        composeRule.onRoot().performKeyInput {
            pressKey(Key.DirectionRight)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TvHomeHeroTitleTag).assertTextEquals("Safe")
        val alignedTopAfter = rowTop(TvHomeNextUpRowTag)
        assertEquals(alignedTopBefore, alignedTopAfter, 2f)
    }

    @Test
    fun tvHomeScreen_usesReducedMediaCardWidths() {
        composeRule.setContent {
            AppTheme {
                Box(
                    modifier = Modifier.size(width = 960.dp, height = 540.dp)
                ) {
                    TvHomeScreen(
                        libraries = sampleLibraries(),
                        libraryContent = sampleLibraryContent(),
                        continueWatching = sampleContinueWatching(),
                        nextUp = emptyList(),
                        onMovieSelected = {},
                        onSeriesSelected = {},
                        onEpisodeSelected = { _, _, _ -> }
                    )
                }
            }
        }

        composeRule.waitForIdle()

        val expectedLandscapeWidth = with(composeRule.density) { 223.2.dp.toPx() }
        assertEquals(expectedLandscapeWidth, nodeWidth(TvHomeInitialFocusTag), 2f)

        composeRule.onRoot().performKeyInput {
            pressKey(Key.DirectionDown)
        }
        composeRule.waitForIdle()

        val expectedPosterWidth = with(composeRule.density) { 122.4.dp.toPx() }
        composeRule.onNodeWithTag(tvHomeLibraryFirstItemTag(sampleLibraryId())).assertIsDisplayed()
        assertEquals(
            expectedPosterWidth,
            nodeWidth(tvHomeLibraryFirstItemTag(sampleLibraryId())),
            2f
        )
    }

    private fun sampleContinueWatching(): List<MediaUiModel> {
        return listOf(
            MovieUiModel(
                sampleMovie(
                    id = "11111111-1111-1111-1111-111111111111",
                    title = "Blade Runner 2049",
                    progress = 42.0
                )
            )
        )
    }

    private fun sampleContinueWatchingRow(): List<MediaUiModel> {
        return listOf(
            MovieUiModel(
                sampleMovie(
                    id = "11111111-1111-1111-1111-111111111111",
                    title = "Blade Runner 2049",
                    progress = 42.0
                )
            ),
            MovieUiModel(
                sampleMovie(
                    id = "55555555-5555-5555-5555-555555555555",
                    title = "Mad Max: Fury Road",
                    progress = 8.0
                )
            )
        )
    }

    private fun sampleNextUp(): List<MediaUiModel> {
        return listOf(
            EpisodeUiModel(
                sampleEpisode(
                    id = "66666666-6666-6666-6666-666666666666",
                    title = "The Train Job"
                )
            )
        )
    }

    private fun sampleNextUpRow(): List<MediaUiModel> {
        return listOf(
            EpisodeUiModel(
                sampleEpisode(
                    id = "66666666-6666-6666-6666-666666666666",
                    title = "The Train Job"
                )
            ),
            EpisodeUiModel(
                sampleEpisode(
                    id = "77777777-7777-7777-7777-777777777777",
                    title = "Safe",
                    releaseDate = "2024-02-09"
                )
            )
        )
    }

    private fun sampleLibraries(): List<LibraryUiModel> {
        return listOf(
            LibraryUiModel(
                id = sampleLibraryId(),
                name = "Movies",
                type = LibraryKind.MOVIES,
                posterUrl = "",
                isEmpty = false
            )
        )
    }

    private fun sampleLibraryContent(): Map<UUID, List<MediaUiModel>> {
        val libraryId = sampleLibraryId()

        return mapOf(
            libraryId to listOf(
                MovieUiModel(
                    sampleMovie(
                        id = "44444444-4444-4444-4444-444444444444",
                        title = "Arrival",
                        progress = null,
                        libraryId = libraryId
                    )
                )
            )
        )
    }

    private fun sampleEpisode(
        id: String,
        title: String,
        releaseDate: String = "2002-09-20",
        progress: Double? = 18.0,
        seriesId: UUID = UUID.fromString("88888888-8888-8888-8888-888888888888"),
        seasonId: UUID = UUID.fromString("99999999-9999-9999-9999-999999999999")
    ): Episode {
        return Episode(
            id = UUID.fromString(id),
            seriesId = seriesId,
            seriesName = "Firefly",
            seasonId = seasonId,
            seasonIndex = 1,
            index = 1,
            title = title,
            synopsis = "A crew member takes the shuttle for a spin and makes a mess.",
            releaseDate = releaseDate,
            rating = "16+",
            runtime = "44m",
            progress = progress,
            watched = false,
            format = "HD",
            imageUrlPrefix = "https://images.unsplash.com/photo-1511497584788-876760111969",
            cast = emptyList()
        )
    }

    private fun sampleMovie(
        id: String,
        title: String,
        progress: Double?,
        libraryId: UUID = UUID.fromString("22222222-2222-2222-2222-222222222222")
    ): Movie {
        return Movie(
            id = UUID.fromString(id),
            libraryId = libraryId,
            title = title,
            progress = progress,
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
    }

    private fun sampleLibraryId(): UUID {
        return UUID.fromString("33333333-3333-3333-3333-333333333333")
    }

    private fun assertSectionRowAlignedToViewportTop(sectionRowTag: String) {
        val expectedRowTop = viewportTop() + with(composeRule.density) {
            TvHomeFocusedItemTopOffset.toPx()
        }
        assertEquals(expectedRowTop, rowTop(sectionRowTag), 2f)
    }

    private fun viewportTop(): Float {
        return composeRule.onNodeWithTag(TvHomeContentViewportTag, useUnmergedTree = true)
            .fetchSemanticsNode()
            .boundsInRoot
            .top
    }

    private fun rowTop(sectionRowTag: String): Float {
        return composeRule.onNodeWithTag(sectionRowTag, useUnmergedTree = true)
            .fetchSemanticsNode()
            .boundsInRoot
            .top
    }

    private fun nodeWidth(tag: String): Float {
        return composeRule.onNodeWithTag(tag, useUnmergedTree = true)
            .fetchSemanticsNode()
            .boundsInRoot
            .width
    }
}
