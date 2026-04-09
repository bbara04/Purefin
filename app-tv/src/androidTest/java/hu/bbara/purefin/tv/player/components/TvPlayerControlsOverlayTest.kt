package hu.bbara.purefin.tv.player.components

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.core.player.model.PlayerUiState
import hu.bbara.purefin.core.player.model.QueueItemUi
import hu.bbara.purefin.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class TvPlayerControlsOverlayTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun downFromSeekBar_movesFocusToControlButtonsBeforePlaylist() {
        composeRule.setContent {
            AppTheme {
                OverlayHost(uiState = samplePlayerState())
            }
        }

        composeRule.onNodeWithTag(TvPlayerSeekBarTag)
            .performSemanticsAction(SemanticsActions.RequestFocus)
            .assertIsFocused()
            .performKeyInput {
                pressKey(Key.DirectionDown)
            }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TvPlayerPlaylistStateTag)
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    "collapsed"
                )
            )
        composeRule.onNodeWithTag(TvPlayerPlayPauseButtonTag).assertIsFocused()
    }

    @Test
    fun downFromControls_expandsPlaylistAndMovesFocusToCurrentQueueItem() {
        composeRule.setContent {
            AppTheme {
                OverlayHost(uiState = samplePlayerState())
            }
        }

        composeRule.onNodeWithTag(TvPlayerPlayPauseButtonTag)
            .performSemanticsAction(SemanticsActions.RequestFocus)
            .assertIsFocused()
            .performKeyInput {
                pressKey(Key.DirectionDown)
            }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TvPlayerPlaylistStateTag)
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    "expanded"
                )
            )
        composeRule.onNodeWithTag(TvPlayerPlaylistCurrentItemTag).assertIsFocused()
    }

    @Test
    fun upFromCurrentQueueItem_collapsesPlaylistAndRestoresFocusedControl() {
        composeRule.setContent {
            AppTheme {
                OverlayHost(uiState = samplePlayerState())
            }
        }

        composeRule.onNodeWithTag(TvPlayerPlayPauseButtonTag)
            .performSemanticsAction(SemanticsActions.RequestFocus)
            .performKeyInput {
                pressKey(Key.DirectionDown)
            }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TvPlayerPlaylistCurrentItemTag)
            .assertIsFocused()
            .performKeyInput {
                pressKey(Key.DirectionUp)
            }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TvPlayerPlaylistStateTag)
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    "collapsed"
                )
            )
        composeRule.onNodeWithTag(TvPlayerPlayPauseButtonTag).assertIsFocused()
    }

    @Test
    fun rightOnLastPlaylistItem_keepsFocusInPlaylist() {
        composeRule.setContent {
            AppTheme {
                OverlayHost(uiState = samplePlayerState())
            }
        }

        composeRule.onNodeWithTag(TvPlayerPlayPauseButtonTag)
            .performSemanticsAction(SemanticsActions.RequestFocus)
            .performKeyInput {
                pressKey(Key.DirectionDown)
            }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TvPlayerPlaylistCurrentItemTag)
            .assertIsFocused()
            .performKeyInput {
                pressKey(Key.DirectionRight)
            }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TvPlayerPlaylistLastItemTag)
            .assertIsFocused()
            .performKeyInput {
                pressKey(Key.DirectionRight)
            }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TvPlayerPlaylistStateTag)
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    "expanded"
                )
            )
        composeRule.onNodeWithTag(TvPlayerPlaylistLastItemTag).assertIsFocused()
    }

    @Test
    fun playlistShowsQueueAsRowAndFocusesCurrentItem() {
        composeRule.setContent {
            AppTheme {
                OverlayHost(
                    uiState = samplePlayerState(),
                    initiallyExpanded = true
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TvPlayerPlaylistRowTag).assertIsDisplayed()
        composeRule.onNodeWithTag(TvPlayerPlaylistCurrentItemTag).assertIsFocused()
        composeRule.onNodeWithText("Currently Playing").assertIsDisplayed()
        composeRule.onNodeWithText("Episode 2").assertIsDisplayed()
        composeRule.onNodeWithTag(TvPlayerPlaylistFirstItemTag).assertIsDisplayed()
        composeRule.onNodeWithText("Current").assertIsDisplayed()
        composeRule.onNodeWithText("Already Played").assertIsDisplayed()
    }

    @Test
    fun playlistFallsBackToFirstItemWhenNoCurrentItemExists() {
        composeRule.setContent {
            AppTheme {
                OverlayHost(
                    uiState = samplePlayerState().copy(
                        queue = listOf(
                            QueueItemUi(
                                id = "first",
                                title = "Episode 1",
                                subtitle = "Fallback entry",
                                artworkUrl = null,
                                isCurrent = false
                            ),
                            QueueItemUi(
                                id = "second",
                                title = "Episode 2",
                                subtitle = "Later",
                                artworkUrl = null,
                                isCurrent = false
                            )
                        )
                    ),
                    initiallyExpanded = true
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TvPlayerPlaylistCurrentItemTag).assertIsFocused()
        composeRule.onNodeWithText("Episode 1").assertIsDisplayed()
    }
}

@Composable
private fun OverlayHost(
    uiState: PlayerUiState,
    initiallyExpanded: Boolean = false
) {
    var isPlaylistExpanded by remember { mutableStateOf(initiallyExpanded) }
    val focusRequester = remember { FocusRequester() }

    Box(modifier = Modifier.size(width = 960.dp, height = 540.dp)) {
        TvPlayerControlsOverlay(
            uiState = uiState,
            focusRequester = focusRequester,
            isPlaylistExpanded = isPlaylistExpanded,
            onPlayPause = {},
            onSeek = { _ -> },
            onSeekRelative = { _ -> },
            onSeekLiveEdge = {},
            onNext = {},
            onPrevious = {},
            onOpenAudioPanel = {},
            onOpenSubtitlesPanel = {},
            onOpenQualityPanel = {},
            onExpandPlaylist = { isPlaylistExpanded = true },
            onCollapsePlaylist = { isPlaylistExpanded = false },
            onSelectQueueItem = { _ -> }
        )
    }
}

private fun samplePlayerState(): PlayerUiState = PlayerUiState(
    isPlaying = true,
    title = "Sample Player",
    subtitle = "Season 1",
    durationMs = 3_600_000,
    positionMs = 120_000,
    bufferedMs = 240_000,
    queue = listOf(
        QueueItemUi(
            id = "played",
            title = "Already Played",
            subtitle = "Episode 0",
            artworkUrl = null,
            isCurrent = false
        ),
        QueueItemUi(
            id = "current",
            title = "Currently Playing",
            subtitle = "Episode 1",
            artworkUrl = null,
            isCurrent = true
        ),
        QueueItemUi(
            id = "next",
            title = "Episode 2",
            subtitle = "Up next",
            artworkUrl = null,
            isCurrent = false
        )
    )
)
