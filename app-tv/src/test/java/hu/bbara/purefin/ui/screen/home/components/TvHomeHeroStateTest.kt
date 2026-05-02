package hu.bbara.purefin.ui.screen.home.components

import hu.bbara.purefin.model.Episode
import hu.bbara.purefin.model.LibraryKind
import hu.bbara.purefin.model.MediaKind
import hu.bbara.purefin.model.Movie
import hu.bbara.purefin.feature.browse.home.ContinueWatchingItem
import hu.bbara.purefin.ui.model.LibraryUiModel
import hu.bbara.purefin.feature.browse.home.NextUpItem
import hu.bbara.purefin.feature.browse.home.PosterItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.UUID

class TvHomeHeroStateTest {

    @Test
    fun itemRegistry_prefersContinueWatchingForFirstAvailableItem() {
        val continueWatching = continueWatchingItem(
            id = "11111111-1111-1111-1111-111111111111",
            title = "Blade Runner 2049"
        )
        val nextUp = nextUpItem(id = "22222222-2222-2222-2222-222222222222")
        val libraryId = UUID.fromString("33333333-3333-3333-3333-333333333333")
        val registry = createTvHomeItemRegistry(
            libraries = listOf(sampleLibrary(libraryId)),
            libraryContent = mapOf(libraryId to listOf(posterItem(id = "44444444-4444-4444-4444-444444444444", libraryId = libraryId))),
            continueWatching = listOf(continueWatching),
            nextUp = listOf(nextUp)
        )

        assertEquals(continueWatching.id, registry.firstAvailableItemId)
        assertEquals(continueWatching, registry.itemById(continueWatching.id))
    }

    @Test
    fun itemRegistry_fallsBackToNextUpWhenContinueWatchingIsEmpty() {
        val nextUp = nextUpItem(id = "22222222-2222-2222-2222-222222222222")
        val registry = createTvHomeItemRegistry(
            libraries = emptyList(),
            libraryContent = emptyMap(),
            continueWatching = emptyList(),
            nextUp = listOf(nextUp)
        )

        assertEquals(nextUp.id, registry.firstAvailableItemId)
        assertEquals(nextUp, registry.itemById(nextUp.id))
    }

    @Test
    fun itemRegistry_fallsBackToFirstVisibleLibraryItemWhenRowsAreEmpty() {
        val firstLibraryId = UUID.fromString("33333333-3333-3333-3333-333333333333")
        val firstPoster = posterItem(
            id = "44444444-4444-4444-4444-444444444444",
            libraryId = firstLibraryId
        )
        val registry = createTvHomeItemRegistry(
            libraries = listOf(
                sampleLibrary(firstLibraryId),
                sampleLibrary(
                    id = UUID.fromString("55555555-5555-5555-5555-555555555555"),
                    isEmpty = true
                )
            ),
            libraryContent = mapOf(firstLibraryId to listOf(firstPoster)),
            continueWatching = emptyList(),
            nextUp = emptyList()
        )

        assertEquals(firstPoster.id, registry.firstAvailableItemId)
        assertEquals(firstPoster, registry.itemById(firstPoster.id))
    }

    @Test
    fun itemRegistry_returnsNullWhenNoItemsAreAvailable() {
        val registry = createTvHomeItemRegistry(
            libraries = emptyList(),
            libraryContent = emptyMap(),
            continueWatching = emptyList(),
            nextUp = emptyList()
        )

        assertNull(registry.firstAvailableItemId)
        assertNull(registry.firstAvailableItem)
        assertNull(registry.itemById(UUID.fromString("99999999-9999-9999-9999-999999999999")))
    }

    @Test
    fun itemRegistry_exposesFirstAvailableItem() {
        val continueWatching = continueWatchingItem(
            id = "11111111-1111-1111-1111-111111111111",
            title = "Blade Runner 2049"
        )
        val registry = createTvHomeItemRegistry(
            libraries = emptyList(),
            libraryContent = emptyMap(),
            continueWatching = listOf(continueWatching),
            nextUp = emptyList()
        )

        assertNotNull(registry.firstAvailableItem)
        assertEquals(continueWatching, registry.firstAvailableItem)
    }

    private fun sampleLibrary(id: UUID, isEmpty: Boolean = false): LibraryUiModel {
        return LibraryUiModel(
            id = id,
            name = "Movies",
            type = LibraryKind.MOVIES,
            posterUrl = "",
            size = 1,
            isEmpty = isEmpty
        )
    }

    private fun continueWatchingItem(id: String, title: String): ContinueWatchingItem {
        return ContinueWatchingItem(
            type = MediaKind.MOVIE,
            movie = sampleMovie(id = id, title = title)
        )
    }

    private fun nextUpItem(id: String): NextUpItem {
        return NextUpItem(
            episode = Episode(
                id = UUID.fromString(id),
                seriesId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                seriesName = "Sample Series",
                seasonId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
                seasonIndex = 1,
                index = 1,
                title = "Next Episode",
                synopsis = "Episode synopsis",
                releaseDate = "2024-01-01",
                rating = "12+",
                runtime = "48m",
                progress = 10.0,
                watched = false,
                format = "HD",
                imageUrlPrefix = "https://example.com/Items/$id/Images/",
                cast = emptyList()
            )
        )
    }

    private fun posterItem(id: String, libraryId: UUID): PosterItem {
        return PosterItem(
            type = MediaKind.MOVIE,
            movie = sampleMovie(id = id, title = "Arrival", libraryId = libraryId)
        )
    }

    private fun sampleMovie(
        id: String,
        title: String,
        libraryId: UUID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc")
    ): Movie {
        return Movie(
            id = UUID.fromString(id),
            libraryId = libraryId,
            title = title,
            progress = 25.0,
            watched = false,
            year = "2017",
            rating = "16+",
            runtime = "164m",
            format = "4K",
            synopsis = "Movie synopsis",
            imageUrlPrefix = "https://example.com/Items/$id/Images/",
            audioTrack = "ENG 5.1",
            subtitles = "ENG",
            cast = emptyList()
        )
    }
}
