package hu.bbara.purefin.tv.library.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import hu.bbara.purefin.feature.browse.home.LibraryItem
import hu.bbara.purefin.ui.theme.AppTheme
import org.jellyfin.sdk.model.api.CollectionType
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.UUID

class TvLibrariesOverviewScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun tvLibrariesOverviewScreen_opensSelectedLibrary() {
        val libraries = listOf(
            LibraryItem(
                id = UUID.fromString("11111111-1111-1111-1111-111111111111"),
                name = "Movies",
                type = CollectionType.MOVIES,
                posterUrl = "",
                isEmpty = false
            ),
            LibraryItem(
                id = UUID.fromString("22222222-2222-2222-2222-222222222222"),
                name = "Shows",
                type = CollectionType.TVSHOWS,
                posterUrl = "",
                isEmpty = false
            )
        )
        var openedLibrary: LibraryItem? = null

        composeRule.setContent {
            AppTheme {
                TvLibrariesOverviewScreen(
                    libraries = libraries,
                    onLibrarySelected = { library ->
                        openedLibrary = library
                    }
                )
            }
        }

        composeRule.onNodeWithTag("${TvLibrariesOverviewItemTagPrefix}1").performClick()
        composeRule.waitForIdle()

        assertEquals(libraries[1], openedLibrary)
    }
}
