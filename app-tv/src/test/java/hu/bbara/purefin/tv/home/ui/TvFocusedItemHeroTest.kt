package hu.bbara.purefin.tv.home.ui

import hu.bbara.purefin.core.data.image.JellyfinImageHelper
import hu.bbara.purefin.core.model.Episode
import hu.bbara.purefin.core.model.Movie
import hu.bbara.purefin.core.model.Series
import hu.bbara.purefin.feature.shared.home.ContinueWatchingItem
import hu.bbara.purefin.feature.shared.home.NextUpItem
import hu.bbara.purefin.feature.shared.home.PosterItem
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.ImageType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.UUID

class TvFocusedItemHeroTest {

    @Test
    fun movieItem_mapsProgressMetadataAndBackdrop() {
        val movie = sampleMovie(progress = 42.0)
        val item = ContinueWatchingItem(
            type = BaseItemKind.MOVIE,
            movie = movie
        )

        val hero = item.toTvFocusedHeroModel()

        assertEquals("Continue watching", hero.eyebrowText)
        assertEquals("Blade Runner 2049", hero.title)
        assertEquals("2017 • 16+ • 164m • 4K", hero.metadataText)
        assertEquals(0.42f, hero.progressFraction!!, 0.0001f)
        assertEquals("42%", hero.progressLabel)
        assertNull(hero.watchedText)
        assertEquals(
            JellyfinImageHelper.finishImageUrl(movie.imageUrlPrefix, ImageType.BACKDROP),
            hero.backdropImageUrl
        )
    }

    @Test
    fun backdropImageUrl_usesFallbackWhenBackdropPrefixIsMissing() {
        assertEquals(
            "https://example.com/fallback.jpg",
            backdropImageUrl(
                imageUrlPrefix = "",
                fallbackImageUrl = "https://example.com/fallback.jpg"
            )
        )
    }

    @Test
    fun episodeItem_prefersWatchedStateOverProgress() {
        val item = NextUpItem(
            episode = sampleEpisode(
                progress = 96.0,
                watched = true
            )
        )

        val hero = item.toTvFocusedHeroModel()

        assertEquals("Next up", hero.eyebrowText)
        assertEquals("The Very Pulse of the Machine", hero.title)
        assertEquals("Episode 3 • 2022-05-20 • 16m • 12+ • HD", hero.metadataText)
        assertEquals("Watched", hero.watchedText)
        assertNull(hero.progressFraction)
        assertNull(hero.progressLabel)
    }

    @Test
    fun seriesItem_usesSeasonAndUnwatchedMetadataWithoutProgress() {
        val item = PosterItem(
            type = BaseItemKind.SERIES,
            series = sampleSeries()
        )

        val hero = item.toTvFocusedHeroModel()

        assertEquals("Series", hero.eyebrowText)
        assertEquals("Love, Death & Robots", hero.title)
        assertEquals("2019 • 3 seasons • 5 unwatched", hero.metadataText)
        assertNull(hero.watchedText)
        assertNull(hero.progressFraction)
        assertNull(hero.progressLabel)
    }

    private fun sampleMovie(progress: Double? = 42.0): Movie {
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
            imageUrlPrefix = "https://example.com/Items/11111111-1111-1111-1111-111111111111/Images/",
            audioTrack = "ENG 5.1",
            subtitles = "ENG",
            cast = emptyList()
        )
    }

    private fun sampleEpisode(progress: Double?, watched: Boolean): Episode {
        return Episode(
            id = UUID.fromString("33333333-3333-3333-3333-333333333333"),
            seriesId = UUID.fromString("44444444-4444-4444-4444-444444444444"),
            seasonId = UUID.fromString("55555555-5555-5555-5555-555555555555"),
            index = 3,
            title = "The Very Pulse of the Machine",
            synopsis = "An astronaut faces a strange world alone.",
            releaseDate = "2022-05-20",
            rating = "12+",
            runtime = "16m",
            progress = progress,
            watched = watched,
            format = "HD",
            imageUrlPrefix = "https://example.com/Items/33333333-3333-3333-3333-333333333333/Images/",
            cast = emptyList()
        )
    }

    private fun sampleSeries(): Series {
        return Series(
            id = UUID.fromString("66666666-6666-6666-6666-666666666666"),
            libraryId = UUID.fromString("77777777-7777-7777-7777-777777777777"),
            name = "Love, Death & Robots",
            synopsis = "Animated sci-fi anthology.",
            year = "2019",
            imageUrlPrefix = "https://example.com/Items/66666666-6666-6666-6666-666666666666/Images/",
            unwatchedEpisodeCount = 5,
            seasonCount = 3,
            seasons = emptyList(),
            cast = emptyList()
        )
    }
}
