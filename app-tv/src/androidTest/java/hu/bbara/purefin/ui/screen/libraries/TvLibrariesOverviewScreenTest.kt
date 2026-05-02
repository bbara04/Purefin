package hu.bbara.purefin.ui.screen.libraries

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import hu.bbara.purefin.model.LibraryKind
import hu.bbara.purefin.ui.model.LibraryUiModel
import hu.bbara.purefin.ui.theme.AppTheme
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
            LibraryUiModel(
                id = UUID.fromString("11111111-1111-1111-1111-111111111111"),
                name = "Movies",
                type = LibraryKind.MOVIES,
                posterUrl = "",
                size = 1,
                isEmpty = false
            ),
            LibraryUiModel(
                id = UUID.fromString("22222222-2222-2222-2222-222222222222"),
                name = "Shows",
                type = LibraryKind.SERIES,
                posterUrl = "",
                size = 1,
                isEmpty = false
            )
        )
        var openedLibrary: LibraryUiModel? = null

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
