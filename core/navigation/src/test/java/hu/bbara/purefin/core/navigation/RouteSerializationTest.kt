package hu.bbara.purefin.core.navigation

import java.util.UUID
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class RouteSerializationTest {

    private val json = Json

    @Test
    fun `episode route round trips with nested dto ids`() {
        val route: Route = Route.EpisodeRoute(
            item = EpisodeDto(
                id = UUID.randomUUID(),
                seasonId = UUID.randomUUID(),
                seriesId = UUID.randomUUID(),
            )
        )

        assertRoundTrip(route)
    }

    @Test
    fun `library route round trips`() {
        val route: Route = Route.LibraryRoute(
            library = LibraryDto(
                id = UUID.randomUUID(),
                name = "Shows",
            )
        )

        assertRoundTrip(route)
    }

    @Test
    fun `movie and series routes round trip`() {
        assertRoundTrip(
            Route.MovieRoute(
                item = MovieDto(id = UUID.randomUUID())
            )
        )
        assertRoundTrip(
            Route.SeriesRoute(
                item = SeriesDto(id = UUID.randomUUID())
            )
        )
    }

    @Test
    fun `singleton routes round trip`() {
        assertRoundTrip(Route.Home)
        assertRoundTrip(Route.LoginRoute)
        assertRoundTrip(Route.PlayerRoute(mediaId = UUID.randomUUID().toString()))
    }

    private fun assertRoundTrip(route: Route) {
        val encoded = json.encodeToString(route)
        val decoded = json.decodeFromString<Route>(encoded)

        assertEquals(route, decoded)
    }
}
