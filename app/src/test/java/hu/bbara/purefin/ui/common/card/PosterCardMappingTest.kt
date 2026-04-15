package hu.bbara.purefin.ui.common.card

import hu.bbara.purefin.core.model.CastMember
import hu.bbara.purefin.core.model.Episode
import hu.bbara.purefin.core.model.MediaKind
import hu.bbara.purefin.core.model.Movie
import hu.bbara.purefin.core.model.Season
import hu.bbara.purefin.core.model.Series
import hu.bbara.purefin.feature.browse.home.PosterItem
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PosterCardMappingTest {
    @Test
    fun `maps movie poster item to watch-state card model`() {
        val movie = Movie(
            id = UUID.fromString("11111111-1111-1111-1111-111111111111"),
            libraryId = UUID.fromString("22222222-2222-2222-2222-222222222222"),
            title = "Blade Runner 2049",
            progress = 18.0,
            watched = false,
            year = "2017",
            rating = "16+",
            runtime = "2h 44m",
            format = "Dolby Vision",
            synopsis = "Synthetic future noir.",
            imageUrlPrefix = "https://example.test/movie/",
            audioTrack = "English 5.1",
            subtitles = "English CC",
            cast = emptyList()
        )

        val model = PosterItem(type = MediaKind.MOVIE, movie = movie).toPosterCardModel()

        assertEquals("Blade Runner 2049", model.title)
        assertEquals("2017", model.secondaryText)
        assertEquals("https://example.test/movie/Primary", model.imageUrl)
        assertEquals(MediaKind.MOVIE, model.mediaKind)
        assertTrue(model.badge is PosterCardBadge.WatchState)
        val badge = model.badge as PosterCardBadge.WatchState
        assertEquals(false, badge.watched)
        assertEquals(true, badge.started)
    }

    @Test
    fun `maps series poster item to unwatched badge card model`() {
        val series = Series(
            id = UUID.fromString("33333333-3333-3333-3333-333333333333"),
            libraryId = UUID.fromString("44444444-4444-4444-4444-444444444444"),
            name = "Severance",
            synopsis = "Corporate sci-fi.",
            year = "2025",
            imageUrlPrefix = "https://example.test/series/",
            unwatchedEpisodeCount = 12,
            seasonCount = 2,
            seasons = listOf(
                Season(
                    id = UUID.fromString("55555555-5555-5555-5555-555555555555"),
                    seriesId = UUID.fromString("33333333-3333-3333-3333-333333333333"),
                    name = "Season 1",
                    index = 1,
                    unwatchedEpisodeCount = 9,
                    episodeCount = 9,
                    episodes = emptyList()
                )
            ),
            cast = listOf(CastMember("Adam Scott", "Mark", null))
        )

        val model = PosterItem(type = MediaKind.SERIES, series = series).toPosterCardModel()

        assertEquals("Severance", model.title)
        assertEquals("2025", model.secondaryText)
        assertEquals(MediaKind.SERIES, model.mediaKind)
        assertTrue(model.badge is PosterCardBadge.UnwatchedEpisodes)
        val badge = model.badge as PosterCardBadge.UnwatchedEpisodes
        assertEquals(12, badge.count)
    }

    @Test
    fun `maps episode poster item to watch-state card model`() {
        val episode = Episode(
            id = UUID.fromString("66666666-6666-6666-6666-666666666666"),
            seriesId = UUID.fromString("33333333-3333-3333-3333-333333333333"),
            seasonId = UUID.fromString("55555555-5555-5555-5555-555555555555"),
            index = 4,
            title = "The You You Are",
            synopsis = "Office mystery.",
            releaseDate = "2025",
            rating = "16+",
            runtime = "53m",
            progress = 0.0,
            watched = true,
            format = "4K",
            imageUrlPrefix = "https://example.test/episode/",
            cast = emptyList()
        )

        val model = PosterItem(type = MediaKind.EPISODE, episode = episode).toPosterCardModel()

        assertEquals("The You You Are", model.title)
        assertEquals("2025", model.secondaryText)
        assertEquals(MediaKind.EPISODE, model.mediaKind)
        assertTrue(model.badge is PosterCardBadge.WatchState)
        val badge = model.badge as PosterCardBadge.WatchState
        assertEquals(true, badge.watched)
        assertEquals(false, badge.started)
    }
}
