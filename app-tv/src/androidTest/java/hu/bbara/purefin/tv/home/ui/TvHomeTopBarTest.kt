package hu.bbara.purefin.tv.home.ui

import androidx.activity.ComponentActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import hu.bbara.purefin.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test

class TvHomeTopBarTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun tvHomeTopBar_updatesSelectedTab() {
        var selectedTabIndex by mutableIntStateOf(1)

        composeRule.setContent {
            AppTheme {
                TvHomeTopBar(
                    tabs = listOf(
                        TvHomeTabItem(
                            destination = TvHomeTabDestination.SEARCH,
                            label = "Search",
                            icon = Icons.Outlined.Search
                        ),
                        TvHomeTabItem(
                            destination = TvHomeTabDestination.HOME,
                            label = "Home",
                            icon = Icons.Outlined.Home
                        )
                    ),
                    selectedTabIndex = selectedTabIndex,
                    onTabSelected = { index, _ ->
                        selectedTabIndex = index
                    }
                )
            }
        }

        composeRule.onNodeWithTag("${TvHomeTabTagPrefix}1").assertIsSelected()
        composeRule.onNodeWithTag("${TvHomeTabTagPrefix}0")
            .assertIsNotSelected()
            .performClick()

        composeRule.waitForIdle()

        composeRule.onNodeWithTag("${TvHomeTabTagPrefix}0").assertIsSelected()
        composeRule.onNodeWithTag("${TvHomeTabTagPrefix}1").assertIsNotSelected()
    }
}
