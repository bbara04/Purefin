package hu.bbara.purefin.ui.screen.player.components

import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.player.model.PlayerUiState
import hu.bbara.purefin.player.model.QueueItemUi
import hu.bbara.purefin.player.model.TrackOption
import hu.bbara.purefin.player.model.TrackType
import hu.bbara.purefin.ui.screen.player.TV_HIDDEN_STOP_FEEDBACK_MS
import hu.bbara.purefin.ui.screen.player.TvPlayerHiddenStopFeedback
import hu.bbara.purefin.ui.screen.player.TvPlayerHiddenStopFeedbackTag
import hu.bbara.purefin.ui.screen.player.handleTvPlayerRootKeyEvent
import hu.bbara.purefin.ui.theme.AppTheme
import kotlinx.coroutines.delay
import org.junit.Rule
import org.junit.Test

private const val TvPlayerRootTag = "tv_player_root"
private const val TvPlayerHiddenPauseCountTag = "tv_player_hidden_pause_count"
private const val TvPlayerHiddenResumeCountTag = "tv_player_hidden_resume_count"
private const val TvPlayerHiddenSeekDeltaTag = "tv_player_hidden_seek_delta"

@OptIn(ExperimentalTestApi::class)
class TvPlayerControlsOverlayTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun requestingControlsFocus_focusesPlayPauseInsteadOfSeekBar() {
        composeRule.setContent {
            AppTheme {
                OverlayHost(
                    uiState = samplePlayerState(),
                    requestFocus = true
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TvPlayerPlayPauseButtonTag).assertIsFocused()
        composeRule.onNodeWithTag(TvPlayerSeekBarTag)
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.Focused,
                    false
                )
            )
    }

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
    fun playlistShowsQueueAsRowAndCurrentItemCanReceiveFocus() {
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
        composeRule.onNodeWithTag(TvPlayerPlaylistCurrentItemTag)
            .performSemanticsAction(SemanticsActions.RequestFocus)
            .assertIsFocused()
        composeRule.onNodeWithText("Currently Playing").assertIsDisplayed()
        composeRule.onNodeWithText("Episode 2").assertIsDisplayed()
        composeRule.onNodeWithTag(TvPlayerPlaylistFirstItemTag).assertIsDisplayed()
        composeRule.onNodeWithText("Current").assertIsDisplayed()
        composeRule.onNodeWithText("Already Played").assertIsDisplayed()
    }

    @Test
    fun playlistFallsBackToFirstItemWhenNoCurrentItemExists_andFallbackItemCanReceiveFocus() {
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

        composeRule.onNodeWithTag(TvPlayerPlaylistCurrentItemTag)
            .performSemanticsAction(SemanticsActions.RequestFocus)
            .assertIsFocused()
        composeRule.onNodeWithText("Episode 1").assertIsDisplayed()
    }

    @Test
    fun openingTrackPanel_focusesSelectedTrackAndHasNoCloseButton() {
        composeRule.setContent {
            AppTheme {
                TrackPanelHost(uiState = samplePlayerState())
            }
        }

        composeRule.onNodeWithTag(TvPlayerAudioButtonTag)
            .performSemanticsAction(SemanticsActions.RequestFocus)
            .assertIsFocused()
            .performKeyInput {
                pressKey(Key.DirectionCenter)
            }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TvPlayerTrackPanelTag).assertIsDisplayed()
        composeRule.onNodeWithTag(TvPlayerTrackSelectedItemTag).assertIsFocused()
        composeRule.onAllNodesWithContentDescription("Close").assertCountEquals(0)
    }

    @Test
    fun openingTrackPanelWithoutSelection_focusesFirstTrack() {
        composeRule.setContent {
            AppTheme {
                TrackPanelHost(
                    uiState = samplePlayerState().copy(selectedAudioTrackId = null)
                )
            }
        }

        composeRule.onNodeWithTag(TvPlayerAudioButtonTag)
            .performSemanticsAction(SemanticsActions.RequestFocus)
            .performKeyInput {
                pressKey(Key.DirectionCenter)
            }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TvPlayerTrackFirstItemTag).assertIsFocused()
    }

    @Test
    fun backOnVisibleControls_hidesOverlayImmediately() {
        composeRule.setContent {
            AppTheme {
                TrackPanelHost(uiState = samplePlayerState())
            }
        }

        composeRule.onNodeWithTag(TvPlayerPlayPauseButtonTag)
            .performSemanticsAction(SemanticsActions.RequestFocus)
            .assertIsFocused()
            .performKeyInput {
                pressKey(Key.Back)
            }

        composeRule.waitForIdle()

        composeRule.onAllNodesWithTag(TvPlayerPlayPauseButtonTag).assertCountEquals(0)
        composeRule.onAllNodesWithTag(TvPlayerTrackPanelTag).assertCountEquals(0)
    }

    @Test
    fun escapeOnVisibleControls_hidesOverlayImmediately() {
        composeRule.setContent {
            AppTheme {
                TrackPanelHost(uiState = samplePlayerState())
            }
        }

        composeRule.onNodeWithTag(TvPlayerPlayPauseButtonTag)
            .performSemanticsAction(SemanticsActions.RequestFocus)
            .assertIsFocused()
            .performKeyInput {
                pressKey(Key.Escape)
            }

        composeRule.waitForIdle()

        composeRule.onAllNodesWithTag(TvPlayerPlayPauseButtonTag).assertCountEquals(0)
        composeRule.onAllNodesWithTag(TvPlayerTrackPanelTag).assertCountEquals(0)
    }

    @Test
    fun hiddenControls_centerWhilePlaying_pausesWithoutShowingControlsAndShowsStopFeedback() {
        composeRule.setContent {
            AppTheme {
                TrackPanelHost(
                    uiState = samplePlayerState(),
                    initialControlsVisible = false
                )
            }
        }

        composeRule.onNodeWithTag(TvPlayerRootTag)
            .performSemanticsAction(SemanticsActions.RequestFocus)
            .performKeyInput {
                pressKey(Key.DirectionCenter)
            }

        composeRule.waitForIdle()

        composeRule.onAllNodesWithTag(TvPlayerPlayPauseButtonTag).assertCountEquals(0)
        composeRule.onNodeWithTag(TvPlayerHiddenStopFeedbackTag).assertIsDisplayed()
        composeRule.onNodeWithTag(TvPlayerHiddenPauseCountTag)
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    "1"
                )
            )
    }

    @Test
    fun hiddenControls_enterWhilePlaying_pausesWithoutShowingControlsAndShowsStopFeedback() {
        composeRule.setContent {
            AppTheme {
                TrackPanelHost(
                    uiState = samplePlayerState(),
                    initialControlsVisible = false
                )
            }
        }

        composeRule.onNodeWithTag(TvPlayerRootTag)
            .performSemanticsAction(SemanticsActions.RequestFocus)
            .performKeyInput {
                pressKey(Key.Enter)
            }

        composeRule.waitForIdle()

        composeRule.onAllNodesWithTag(TvPlayerPlayPauseButtonTag).assertCountEquals(0)
        composeRule.onNodeWithTag(TvPlayerHiddenStopFeedbackTag).assertIsDisplayed()
        composeRule.onNodeWithTag(TvPlayerHiddenPauseCountTag)
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    "1"
                )
            )
    }

    @Test
    fun hiddenControls_stopFeedbackAutoHidesAfterTimeout() {
        composeRule.mainClock.autoAdvance = false

        composeRule.setContent {
            AppTheme {
                TrackPanelHost(
                    uiState = samplePlayerState(),
                    initialControlsVisible = false
                )
            }
        }

        composeRule.onNodeWithTag(TvPlayerRootTag)
            .performSemanticsAction(SemanticsActions.RequestFocus)
            .performKeyInput {
                pressKey(Key.DirectionCenter)
            }

        composeRule.mainClock.advanceTimeByFrame()
        composeRule.onNodeWithTag(TvPlayerHiddenStopFeedbackTag).assertIsDisplayed()

        composeRule.mainClock.advanceTimeBy(TV_HIDDEN_STOP_FEEDBACK_MS - 1)
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.onNodeWithTag(TvPlayerHiddenStopFeedbackTag).assertIsDisplayed()

        composeRule.mainClock.advanceTimeBy(1)
        composeRule.mainClock.advanceTimeBy(300)
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.onAllNodesWithTag(TvPlayerHiddenStopFeedbackTag).assertCountEquals(0)

        composeRule.mainClock.autoAdvance = true
    }

    @Test
    fun hiddenControls_centerWhilePaused_resumesWithoutShowingControls() {
        composeRule.setContent {
            AppTheme {
                TrackPanelHost(
                    uiState = samplePlayerState().copy(isPlaying = false),
                    initialControlsVisible = false
                )
            }
        }

        composeRule.onNodeWithTag(TvPlayerRootTag)
            .performSemanticsAction(SemanticsActions.RequestFocus)
            .performKeyInput {
                pressKey(Key.DirectionCenter)
            }

        composeRule.waitForIdle()

        composeRule.onAllNodesWithTag(TvPlayerPlayPauseButtonTag).assertCountEquals(0)
        composeRule.onAllNodesWithTag(TvPlayerHiddenStopFeedbackTag).assertCountEquals(0)
        composeRule.onNodeWithTag(TvPlayerHiddenPauseCountTag)
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    "0"
                )
            )
        composeRule.onNodeWithTag(TvPlayerHiddenResumeCountTag)
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    "1"
                )
            )
    }

    @Test
    fun hiddenControls_leftSeeksWithoutShowingControls() {
        composeRule.setContent {
            AppTheme {
                TrackPanelHost(
                    uiState = samplePlayerState(),
                    initialControlsVisible = false
                )
            }
        }

        composeRule.onNodeWithTag(TvPlayerRootTag)
            .performSemanticsAction(SemanticsActions.RequestFocus)
            .performKeyInput {
                pressKey(Key.DirectionLeft)
            }

        composeRule.waitForIdle()

        composeRule.onAllNodesWithTag(TvPlayerPlayPauseButtonTag).assertCountEquals(0)
        composeRule.onAllNodesWithTag(TvPlayerHiddenStopFeedbackTag).assertCountEquals(0)
        composeRule.onNodeWithTag(TvPlayerHiddenSeekDeltaTag)
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    "-10000"
                )
            )
    }

    @Test
    fun hiddenControls_rightSeeksWithoutShowingControls() {
        composeRule.setContent {
            AppTheme {
                TrackPanelHost(
                    uiState = samplePlayerState(),
                    initialControlsVisible = false
                )
            }
        }

        composeRule.onNodeWithTag(TvPlayerRootTag)
            .performSemanticsAction(SemanticsActions.RequestFocus)
            .performKeyInput {
                pressKey(Key.DirectionRight)
            }

        composeRule.waitForIdle()

        composeRule.onAllNodesWithTag(TvPlayerPlayPauseButtonTag).assertCountEquals(0)
        composeRule.onAllNodesWithTag(TvPlayerHiddenStopFeedbackTag).assertCountEquals(0)
        composeRule.onNodeWithTag(TvPlayerHiddenSeekDeltaTag)
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    "10000"
                )
            )
    }

    @Test
    fun backOnTrackPanel_closesItAndRestoresFocusToOpeningButton() {
        composeRule.setContent {
            AppTheme {
                TrackPanelHost(uiState = samplePlayerState())
            }
        }

        composeRule.onNodeWithTag(TvPlayerAudioButtonTag)
            .performSemanticsAction(SemanticsActions.RequestFocus)
            .performKeyInput {
                pressKey(Key.DirectionCenter)
            }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TvPlayerTrackSelectedItemTag)
            .assertIsFocused()
            .performKeyInput {
                pressKey(Key.Back)
            }

        composeRule.waitForIdle()

        composeRule.onAllNodesWithTag(TvPlayerTrackPanelTag).assertCountEquals(0)
        composeRule.onNodeWithTag(TvPlayerAudioButtonTag).assertIsFocused()
    }

    @Test
    fun leftOnTrackPanel_keepsFocusInsidePanel() {
        composeRule.setContent {
            AppTheme {
                TrackPanelHost(uiState = samplePlayerState())
            }
        }

        composeRule.onNodeWithTag(TvPlayerAudioButtonTag)
            .performSemanticsAction(SemanticsActions.RequestFocus)
            .performKeyInput {
                pressKey(Key.DirectionCenter)
            }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TvPlayerTrackSelectedItemTag)
            .assertIsFocused()
            .performKeyInput {
                pressKey(Key.DirectionLeft)
            }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TvPlayerTrackPanelTag).assertIsDisplayed()
        composeRule.onNodeWithTag(TvPlayerTrackSelectedItemTag).assertIsFocused()
    }

    @Test
    fun selectingTrack_closesPanelAndRestoresFocusToOpeningButton() {
        composeRule.setContent {
            AppTheme {
                TrackPanelHost(uiState = samplePlayerState())
            }
        }

        composeRule.onNodeWithTag(TvPlayerQualityButtonTag)
            .performSemanticsAction(SemanticsActions.RequestFocus)
            .performKeyInput {
                pressKey(Key.DirectionCenter)
            }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TvPlayerTrackSelectedItemTag)
            .assertIsFocused()
            .performKeyInput {
                pressKey(Key.DirectionCenter)
            }

        composeRule.waitForIdle()

        composeRule.onAllNodesWithTag(TvPlayerTrackPanelTag).assertCountEquals(0)
        composeRule.onNodeWithTag(TvPlayerQualityButtonTag).assertIsFocused()
    }

    @Test
    fun trackButtonsWithoutOptions_areDisabled() {
        composeRule.setContent {
            AppTheme {
                TrackPanelHost(
                    uiState = samplePlayerState().copy(
                        audioTracks = emptyList(),
                        textTracks = emptyList(),
                        qualityTracks = emptyList(),
                        selectedAudioTrackId = null,
                        selectedTextTrackId = null,
                        selectedQualityTrackId = null
                    )
                )
            }
        }

        composeRule.onNodeWithTag(TvPlayerAudioButtonTag).assertIsNotEnabled()
        composeRule.onNodeWithTag(TvPlayerSubtitlesButtonTag).assertIsNotEnabled()
        composeRule.onNodeWithTag(TvPlayerQualityButtonTag).assertIsNotEnabled()
    }
}

@Composable
private fun OverlayHost(
    uiState: PlayerUiState,
    initiallyExpanded: Boolean = false,
    requestFocus: Boolean = false
) {
    var isPlaylistExpanded by remember { mutableStateOf(initiallyExpanded) }
    val focusRequester = remember { FocusRequester() }
    val qualityButtonFocusRequester = remember { FocusRequester() }
    val audioButtonFocusRequester = remember { FocusRequester() }
    val subtitlesButtonFocusRequester = remember { FocusRequester() }

    LaunchedEffect(requestFocus) {
        if (requestFocus) {
            focusRequester.requestFocus()
        }
    }

    Box(modifier = Modifier.size(width = 960.dp, height = 540.dp)) {
        TvPlayerControlsOverlay(
            uiState = uiState,
            focusRequester = focusRequester,
            isPlaylistExpanded = isPlaylistExpanded,
            qualityButtonFocusRequester = qualityButtonFocusRequester,
            audioButtonFocusRequester = audioButtonFocusRequester,
            subtitlesButtonFocusRequester = subtitlesButtonFocusRequester,
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
            onSelectQueueItem = { _ -> },
            qualityButtonEnabled = uiState.qualityTracks.isNotEmpty(),
            audioButtonEnabled = uiState.audioTracks.isNotEmpty(),
            subtitlesButtonEnabled = uiState.textTracks.isNotEmpty()
        )
    }
}

@Composable
private fun TrackPanelHost(
    uiState: PlayerUiState,
    initialControlsVisible: Boolean = true
) {
    var controlsVisible by remember { mutableStateOf(initialControlsVisible) }
    var trackPanelType by remember { mutableStateOf<TvTrackPanelType?>(null) }
    var pendingTrackButtonFocus by remember { mutableStateOf<TvTrackPanelType?>(null) }
    var stopFeedbackVisible by remember { mutableStateOf(false) }
    var stopFeedbackRequestId by remember { mutableStateOf(0) }
    var pauseWithoutControlsCount by remember { mutableStateOf(0) }
    var resumeWithoutControlsCount by remember { mutableStateOf(0) }
    var lastHiddenSeekDeltaMs by remember { mutableStateOf(0L) }
    val rootFocusRequester = remember { FocusRequester() }
    val controlsFocusRequester = remember { FocusRequester() }
    val qualityButtonFocusRequester = remember { FocusRequester() }
    val audioButtonFocusRequester = remember { FocusRequester() }
    val subtitlesButtonFocusRequester = remember { FocusRequester() }
    val closeTrackPanel: () -> Unit = {
        trackPanelType?.let { panelType ->
            pendingTrackButtonFocus = panelType
            trackPanelType = null
            controlsVisible = true
        }
    }
    val pausePlaybackWithoutShowingControls: () -> Unit = {
        pauseWithoutControlsCount += 1
        stopFeedbackVisible = true
        stopFeedbackRequestId += 1
    }
    val resumePlaybackWithoutShowingControls: () -> Unit = {
        resumeWithoutControlsCount += 1
        stopFeedbackVisible = false
    }
    val seekWithoutShowingControls: (Long) -> Unit = { deltaMs ->
        lastHiddenSeekDeltaMs = deltaMs
        stopFeedbackVisible = false
    }

    LaunchedEffect(trackPanelType, pendingTrackButtonFocus) {
        val pendingFocus = pendingTrackButtonFocus ?: return@LaunchedEffect
        if (trackPanelType != null) return@LaunchedEffect
        when (pendingFocus) {
            TvTrackPanelType.AUDIO -> audioButtonFocusRequester.requestFocus()
            TvTrackPanelType.SUBTITLES -> subtitlesButtonFocusRequester.requestFocus()
            TvTrackPanelType.QUALITY -> qualityButtonFocusRequester.requestFocus()
        }
        pendingTrackButtonFocus = null
    }

    LaunchedEffect(stopFeedbackRequestId) {
        if (stopFeedbackRequestId == 0) return@LaunchedEffect
        delay(TV_HIDDEN_STOP_FEEDBACK_MS)
        stopFeedbackVisible = false
    }

    LaunchedEffect(controlsVisible, trackPanelType) {
        if (controlsVisible || trackPanelType != null) {
            stopFeedbackVisible = false
        } else {
            rootFocusRequester.requestFocus()
        }
    }

    Box(
        modifier = Modifier
            .size(width = 960.dp, height = 540.dp)
            .focusRequester(rootFocusRequester)
            .focusable()
            .testTag(TvPlayerRootTag)
            .onPreviewKeyEvent { event ->
                handleTvPlayerRootKeyEvent(
                    event = event,
                    isPlaying = uiState.isPlaying,
                    controlsVisible = controlsVisible,
                    isPlaylistExpanded = false,
                    trackPanelType = trackPanelType,
                    onCloseTrackPanel = closeTrackPanel,
                    onCollapsePlaylist = { controlsVisible = true },
                    onHideControls = { controlsVisible = false },
                    onPausePlaybackWithoutShowingControls = pausePlaybackWithoutShowingControls,
                    onResumePlaybackWithoutShowingControls = resumePlaybackWithoutShowingControls,
                    onSeekRelative = seekWithoutShowingControls,
                    onShowControls = { controlsVisible = true },
                    onTogglePlayPause = { controlsVisible = true }
                )
            }
    ) {
        if (controlsVisible || trackPanelType != null) {
            TvPlayerControlsOverlay(
                uiState = uiState,
                focusRequester = controlsFocusRequester,
                isPlaylistExpanded = false,
                qualityButtonFocusRequester = qualityButtonFocusRequester,
                audioButtonFocusRequester = audioButtonFocusRequester,
                subtitlesButtonFocusRequester = subtitlesButtonFocusRequester,
                onPlayPause = {},
                onSeek = { _ -> },
                onSeekRelative = { _ -> },
                onSeekLiveEdge = {},
                onNext = {},
                onPrevious = {},
                onOpenAudioPanel = {
                    controlsVisible = true
                    trackPanelType = TvTrackPanelType.AUDIO
                },
                onOpenSubtitlesPanel = {
                    controlsVisible = true
                    trackPanelType = TvTrackPanelType.SUBTITLES
                },
                onOpenQualityPanel = {
                    controlsVisible = true
                    trackPanelType = TvTrackPanelType.QUALITY
                },
                onExpandPlaylist = {},
                onCollapsePlaylist = {},
                onSelectQueueItem = { _ -> },
                qualityButtonEnabled = uiState.qualityTracks.isNotEmpty(),
                audioButtonEnabled = uiState.audioTracks.isNotEmpty(),
                subtitlesButtonEnabled = uiState.textTracks.isNotEmpty()
            )
        }

        AnimatedVisibility(
            visible = stopFeedbackVisible,
            modifier = Modifier.align(Alignment.Center)
        ) {
            TvPlayerHiddenStopFeedback()
        }

        Box(
            modifier = Modifier
                .testTag(TvPlayerHiddenPauseCountTag)
                .semantics { stateDescription = pauseWithoutControlsCount.toString() }
        )

        Box(
            modifier = Modifier
                .testTag(TvPlayerHiddenResumeCountTag)
                .semantics { stateDescription = resumeWithoutControlsCount.toString() }
        )

        Box(
            modifier = Modifier
                .testTag(TvPlayerHiddenSeekDeltaTag)
                .semantics { stateDescription = lastHiddenSeekDeltaMs.toString() }
        )

        trackPanelType?.let { panelType ->
            TvTrackSelectionPanel(
                panelType = panelType,
                uiState = uiState,
                onSelect = { closeTrackPanel() },
                modifier = Modifier.fillMaxSize()
            )
        }
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
    ),
    audioTracks = listOf(
        TrackOption(
            id = "audio_en",
            label = "English 5.1",
            language = "en",
            bitrate = null,
            channelCount = 6,
            height = null,
            groupIndex = 0,
            trackIndex = 0,
            type = TrackType.AUDIO,
            isOff = false
        ),
        TrackOption(
            id = "audio_hu",
            label = "Hungarian Stereo",
            language = "hu",
            bitrate = null,
            channelCount = 2,
            height = null,
            groupIndex = 0,
            trackIndex = 1,
            type = TrackType.AUDIO,
            isOff = false
        )
    ),
    textTracks = listOf(
        TrackOption(
            id = "text_off",
            label = "Off",
            language = null,
            bitrate = null,
            channelCount = null,
            height = null,
            groupIndex = 1,
            trackIndex = -1,
            type = TrackType.TEXT,
            isOff = true
        ),
        TrackOption(
            id = "text_en",
            label = "English CC",
            language = "en",
            bitrate = null,
            channelCount = null,
            height = null,
            groupIndex = 1,
            trackIndex = 0,
            type = TrackType.TEXT,
            isOff = false
        )
    ),
    qualityTracks = listOf(
        TrackOption(
            id = "quality_auto",
            label = "Auto",
            language = null,
            bitrate = null,
            channelCount = null,
            height = 1080,
            groupIndex = 2,
            trackIndex = 0,
            type = TrackType.VIDEO,
            isOff = false
        ),
        TrackOption(
            id = "quality_720",
            label = "720p",
            language = null,
            bitrate = null,
            channelCount = null,
            height = 720,
            groupIndex = 2,
            trackIndex = 1,
            type = TrackType.VIDEO,
            isOff = false
        )
    ),
    selectedAudioTrackId = "audio_hu",
    selectedTextTrackId = "text_en",
    selectedQualityTrackId = "quality_auto"
)
