package hu.bbara.purefin.ui.screen.home.components

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Home
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.navigation.LibraryDto
import hu.bbara.purefin.navigation.Route
import hu.bbara.purefin.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalTestApi::class)
class TvNavigationDrawerTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun tvDrawerHeader_onlyShowsTitleWhenExpanded() {
        var expanded by mutableStateOf(false)

        composeRule.setContent {
            AppTheme {
                TvDrawerHeader(expanded = expanded)
            }
        }

        composeRule.onAllNodesWithTag(TvDrawerTitleTag).assertCountEquals(0)

        composeRule.runOnUiThread {
            expanded = true
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TvDrawerTitleTag).assertIsDisplayed()
    }

    @Test
    fun tvNavigationDrawer_updatesSelectedDestination() {
        val libraryRoute = Route.LibraryRoute(
            library = LibraryDto(
                id = UUID.fromString("22222222-2222-2222-2222-222222222222"),
                name = "Movies"
            )
        )
        var selectedDestination by mutableStateOf<Route>(Route.Home)

        composeRule.setContent {
            AppTheme {
                TvNavigationDrawer(
                    destinations = listOf(
                        TvDrawerDestinationItem(
                            destination = Route.Home,
                            label = "Home",
                            icon = Icons.Outlined.Home
                        ),
                        TvDrawerDestinationItem(
                            destination = libraryRoute,
                            label = "Movies",
                            icon = Icons.Outlined.Collections
                        )
                    ),
                    selectedDestination = selectedDestination,
                    onDestinationSelected = { destination ->
                        selectedDestination = destination
                    }
                ) {
                    Box(modifier = Modifier.size(320.dp))
                }
            }
        }

        composeRule.onNodeWithTag("${TvDrawerItemTagPrefix}0").assertIsSelected()
        composeRule.onNodeWithTag("${TvDrawerItemTagPrefix}1")
            .assertIsNotSelected()
            .performSemanticsAction(SemanticsActions.RequestFocus)
            .performKeyInput {
                pressKey(Key.DirectionCenter)
            }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag("${TvDrawerItemTagPrefix}1").assertIsSelected()
        composeRule.onNodeWithTag("${TvDrawerItemTagPrefix}0").assertIsNotSelected()
    }
}
