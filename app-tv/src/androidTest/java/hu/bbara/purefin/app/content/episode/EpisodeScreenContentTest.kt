package hu.bbara.purefin.app.content.episode

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.input.key.Key
import hu.bbara.purefin.core.data.navigation.Route
import hu.bbara.purefin.core.model.CastMember
import hu.bbara.purefin.core.model.Episode
import hu.bbara.purefin.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalTestApi::class)
class EpisodeScreenContentTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun episodeScreenContent_showsSeriesContext_andMovesFromBackToPlayButton() {
        composeRule.setContent {
            AppTheme {
                EpisodeScreenContent(
                    episode = sampleEpisode(progress = 63.0),
                    seriesTitle = "Severance",
                    topBarShortcut = episodeTopBarShortcut(Route.Home, onSeriesClick = {}),
                    onBack = {},
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
        composeRule.onNodeWithContentDescription("Back")
            .assertIsDisplayed()
            .assertIsFocused()
            .performKeyInput {
                pressKey(Key.DirectionDown)
            }
        composeRule.onNodeWithTag(EpisodePlayButtonTag).assertIsFocused()
    }

    @Test
    fun episodeScreenContent_hidesShortcut_whenNoShortcutIsProvided() {
        composeRule.setContent {
            AppTheme {
                EpisodeScreenContent(
                    episode = sampleEpisode(progress = null),
                    seriesTitle = "Severance",
                    topBarShortcut = episodeTopBarShortcut(
                        previousRoute = Route.PlayerRoute(mediaId = "episode-4"),
                        onSeriesClick = {}
                    ),
                    onBack = {},
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

    private fun sampleEpisode(progress: Double?): Episode {
        val seriesId = UUID.fromString("11111111-1111-1111-1111-111111111111")
        val seasonId = UUID.fromString("22222222-2222-2222-2222-222222222222")

        return Episode(
            id = UUID.fromString("33333333-3333-3333-3333-333333333333"),
            seriesId = seriesId,
            seasonId = seasonId,
            index = 4,
            title = "The You You Are",
            synopsis = "Mark is pulled deeper into Lumon's fractured world as the team chases a clue.",
            releaseDate = "2025",
            rating = "16+",
            runtime = "49m",
            progress = progress,
            watched = false,
            format = "4K",
            heroImageUrl = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee",
            cast = listOf(
                CastMember("Adam Scott", "Mark Scout", null)
            )
        )
    }
}
