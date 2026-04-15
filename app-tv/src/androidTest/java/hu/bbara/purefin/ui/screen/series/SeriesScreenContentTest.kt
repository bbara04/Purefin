package hu.bbara.purefin.ui.screen.series

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.input.key.Key
import hu.bbara.purefin.core.model.CastMember
import hu.bbara.purefin.core.model.Episode
import hu.bbara.purefin.core.model.Season
import hu.bbara.purefin.core.model.Series
import hu.bbara.purefin.ui.theme.AppTheme
import hu.bbara.purefin.ui.screen.series.components.SeriesFirstSeasonTabTag
import hu.bbara.purefin.ui.screen.series.components.SeriesPlayButtonTag
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalTestApi::class)
class SeriesScreenContentTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun seriesScreenContent_focusesPrimaryAction_whenNextUpExists() {
        composeRule.setContent {
            AppTheme {
                TvSeriesScreenContent(
                    series = sampleSeriesWithEpisodes(),
                    onPlayEpisode = {}
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithText("Severance").assertIsDisplayed()
        composeRule.onNodeWithText("Overview").assertIsDisplayed()
        composeRule.onNodeWithText("Continue Watching").assertIsDisplayed()
        composeRule.onNodeWithTag(SeriesPlayButtonTag).assertIsDisplayed()
            .assertIsFocused()
        composeRule.onNodeWithText("Season 1").assertIsDisplayed()
        composeRule.onNodeWithText("Good News About Hell").assertIsDisplayed()
        composeRule.onNodeWithText("Episode 1 • 57m").assertIsDisplayed()
    }

    @Test
    fun seriesScreenContent_focusesFirstSeason_whenNoPlayableEpisodeExists() {
        composeRule.setContent {
            AppTheme {
                TvSeriesScreenContent(
                    series = sampleSeriesWithoutEpisodes(),
                    onPlayEpisode = {}
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithText("Overview").assertIsDisplayed()
        composeRule.onNodeWithText("Choose a season below to start watching.").assertIsDisplayed()
        composeRule.onNodeWithTag(SeriesFirstSeasonTabTag)
            .assertIsDisplayed()
            .assertIsFocused()
    }

    @Test
    fun seriesScreenContent_startsPlaybackOnFirstCenterPress() {
        var playCount = 0

        composeRule.setContent {
            AppTheme {
                TvSeriesScreenContent(
                    series = sampleSeriesWithEpisodes(),
                    onPlayEpisode = { playCount++ }
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag(SeriesPlayButtonTag).assertIsFocused()
        composeRule.onRoot().performKeyInput {
            pressKey(Key.DirectionCenter)
        }

        composeRule.waitForIdle()

        assertEquals(1, playCount)
    }

    private fun sampleSeriesWithEpisodes(): Series {
        val seriesId = UUID.fromString("11111111-1111-1111-1111-111111111111")
        val seasonId = UUID.fromString("22222222-2222-2222-2222-222222222222")

        return Series(
            id = seriesId,
            libraryId = UUID.fromString("33333333-3333-3333-3333-333333333333"),
            name = "Severance",
            synopsis = "Mark leads a team of office workers whose memories have been surgically divided.",
            year = "2022",
            imageUrlPrefix = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee",
            unwatchedEpisodeCount = 3,
            seasonCount = 1,
            seasons = listOf(
                Season(
                    id = seasonId,
                    seriesId = seriesId,
                    name = "Season 1",
                    index = 1,
                    unwatchedEpisodeCount = 3,
                    episodeCount = 2,
                    episodes = listOf(
                        Episode(
                            id = UUID.fromString("44444444-4444-4444-4444-444444444444"),
                            seriesId = seriesId,
                            seasonId = seasonId,
                            index = 1,
                            title = "Good News About Hell",
                            synopsis = "Mark is promoted after an unexpected tragedy.",
                            releaseDate = "2022",
                            rating = "16+",
                            runtime = "57m",
                            progress = 18.0,
                            watched = false,
                            format = "4K",
                            imageUrlPrefix = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee",
                            cast = emptyList()
                        ),
                        Episode(
                            id = UUID.fromString("55555555-5555-5555-5555-555555555555"),
                            seriesId = seriesId,
                            seasonId = seasonId,
                            index = 2,
                            title = "Half Loop",
                            synopsis = "Mark takes the team out for a sanctioned dinner.",
                            releaseDate = "2022",
                            rating = "16+",
                            runtime = "51m",
                            progress = null,
                            watched = false,
                            format = "4K",
                            imageUrlPrefix = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee",
                            cast = emptyList()
                        )
                    )
                )
            ),
            cast = listOf(
                CastMember("Adam Scott", "Mark Scout", null)
            )
        )
    }

    private fun sampleSeriesWithoutEpisodes(): Series {
        val seriesId = UUID.fromString("66666666-6666-6666-6666-666666666666")

        return Series(
            id = seriesId,
            libraryId = UUID.fromString("77777777-7777-7777-7777-777777777777"),
            name = "Foundation",
            synopsis = "A band of exiles works to preserve knowledge through the fall of an empire.",
            year = "2021",
            imageUrlPrefix = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee",
            unwatchedEpisodeCount = 0,
            seasonCount = 1,
            seasons = listOf(
                Season(
                    id = UUID.fromString("88888888-8888-8888-8888-888888888888"),
                    seriesId = seriesId,
                    name = "Season 1",
                    index = 1,
                    unwatchedEpisodeCount = 0,
                    episodeCount = 0,
                    episodes = emptyList()
                )
            ),
            cast = emptyList()
        )
    }
}
