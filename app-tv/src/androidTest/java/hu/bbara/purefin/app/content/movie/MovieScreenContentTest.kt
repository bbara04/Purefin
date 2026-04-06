package hu.bbara.purefin.app.content.movie

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
import hu.bbara.purefin.core.model.CastMember
import hu.bbara.purefin.core.model.Movie
import hu.bbara.purefin.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalTestApi::class)
class MovieScreenContentTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun movieScreenContent_focusesPlayButton() {
        composeRule.setContent {
            AppTheme {
                MovieScreenContent(
                    movie = sampleMovie(progress = 42.0),
                    onPlay = {}
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithText("Blade Runner 2049").assertIsDisplayed()
        composeRule.onNodeWithText("Overview").assertIsDisplayed()
        composeRule.onAllNodesWithText("Playback").assertCountEquals(1)
        composeRule.onNodeWithTag(MoviePlayButtonTag)
            .assertIsDisplayed()
            .assertIsFocused()
    }

    @Test
    fun movieScreenContent_startsPlaybackOnFirstCenterPress() {
        var playCount = 0

        composeRule.setContent {
            AppTheme {
                MovieScreenContent(
                    movie = sampleMovie(progress = 42.0),
                    onPlay = { playCount++ }
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag(MoviePlayButtonTag).assertIsFocused()
        composeRule.onRoot().performKeyInput {
            pressKey(Key.DirectionCenter)
        }

        composeRule.waitForIdle()

        assertEquals(1, playCount)
    }

    private fun sampleMovie(progress: Double?): Movie {
        return Movie(
            id = UUID.fromString("11111111-1111-1111-1111-111111111111"),
            libraryId = UUID.fromString("22222222-2222-2222-2222-222222222222"),
            title = "Blade Runner 2049",
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
            cast = listOf(
                CastMember("Ryan Gosling", "K", null)
            )
        )
    }
}
