package hu.bbara.purefin.ui.screen.episode

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.input.key.Key
import hu.bbara.purefin.model.CastMember
import hu.bbara.purefin.model.Episode
import hu.bbara.purefin.ui.theme.AppTheme
import hu.bbara.purefin.ui.screen.episode.components.EpisodePlayButtonTag
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalTestApi::class)
class EpisodeScreenContentTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun episodeScreenContent_showsSeriesContext_andFocusesPlayButton() {
        composeRule.setContent {
            AppTheme {
                TvEpisodeScreenContent(
                    episode = sampleEpisode(progress = 63.0),
                    seriesTitle = "Severance",
                    onPlay = {}
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithText("Severance").assertIsDisplayed()
        composeRule.onNodeWithText("The You You Are").assertIsDisplayed()
        composeRule.onNodeWithText("Episode 4").assertIsDisplayed()
        composeRule.onNodeWithText("Overview").assertIsDisplayed()
        composeRule.onAllNodesWithText("Playback").assertCountEquals(1)
        composeRule.onNodeWithTag(EpisodePlayButtonTag).assertIsDisplayed()
        composeRule.onNodeWithText("Series").assertIsDisplayed()
        composeRule.onNodeWithTag(EpisodePlayButtonTag).assertIsFocused()
    }

    @Test
    fun episodeScreenContent_hidesSeriesShortcut_whenShortcutUiIsUnavailable() {
        composeRule.setContent {
            AppTheme {
                TvEpisodeScreenContent(
                    episode = sampleEpisode(progress = null),
                    seriesTitle = "Severance",
                    onPlay = {}
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithText("Overview").assertIsDisplayed()
        composeRule.onAllNodesWithText("Playback").assertCountEquals(1)
        composeRule.onNodeWithTag(EpisodePlayButtonTag).assertIsDisplayed()
        composeRule.onAllNodesWithText("Series").assertCountEquals(0)
    }

    @Test
    fun episodeScreenContent_startsPlaybackOnFirstCenterPress() {
        var playCount = 0

        composeRule.setContent {
            AppTheme {
                TvEpisodeScreenContent(
                    episode = sampleEpisode(progress = 63.0),
                    seriesTitle = "Severance",
                    onPlay = { playCount++ }
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag(EpisodePlayButtonTag).assertIsFocused()
        composeRule.onRoot().performKeyInput {
            pressKey(Key.DirectionCenter)
        }

        composeRule.waitForIdle()

        assertEquals(1, playCount)
    }

    private fun sampleEpisode(progress: Double?): Episode {
        val seriesId = UUID.fromString("11111111-1111-1111-1111-111111111111")
        val seasonId = UUID.fromString("22222222-2222-2222-2222-222222222222")

        return Episode(
            id = UUID.fromString("33333333-3333-3333-3333-333333333333"),
            seriesId = seriesId,
            seriesName = "Severance",
            seasonId = seasonId,
            seasonIndex = 2,
            index = 4,
            title = "The You You Are",
            synopsis = "Mark is pulled deeper into Lumon's fractured world as the team chases a clue.",
            releaseDate = "2025",
            rating = "16+",
            runtime = "49m",
            progress = progress,
            watched = false,
            format = "4K",
            imageUrlPrefix = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee",
            cast = listOf(
                CastMember("Adam Scott", "Mark Scout", null)
            )
        )
    }
}
