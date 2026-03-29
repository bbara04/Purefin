package hu.bbara.purefin.app.content.episode

import hu.bbara.purefin.core.data.navigation.Route
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EpisodeTopBarShortcutTest {

    @Test
    fun `home route exposes series shortcut`() {
        var clicked = false

        val shortcut = episodeTopBarShortcut(Route.Home) {
            clicked = true
        }

        assertNotNull(shortcut)
        assertEquals("Series", shortcut?.label)

        shortcut?.onClick?.invoke()

        assertTrue(clicked)
    }

    @Test
    fun `non home route hides series shortcut`() {
        val shortcut = episodeTopBarShortcut(
            previousRoute = Route.PlayerRoute(mediaId = "episode-1"),
            onSeriesClick = {}
        )

        assertNull(shortcut)
    }
}
