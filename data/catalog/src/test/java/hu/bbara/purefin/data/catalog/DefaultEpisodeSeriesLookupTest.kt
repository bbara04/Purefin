package hu.bbara.purefin.data.catalog

import hu.bbara.purefin.core.data.MediaCatalogReader
import hu.bbara.purefin.core.model.Episode
import hu.bbara.purefin.core.model.Movie
import hu.bbara.purefin.core.model.Series
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultEpisodeSeriesLookupTest {

    @Test
    fun `returns series id for known episode`() = runBlocking {
        val episodeId = UUID.randomUUID()
        val seriesId = UUID.randomUUID()
        val lookup = DefaultEpisodeSeriesLookup(
            mediaCatalogReader = FakeMediaCatalogReader(
                episodes = mapOf(
                    episodeId to episode(
                        id = episodeId,
                        seriesId = seriesId,
                    )
                )
            )
        )

        val result = lookup.preferenceKeyFor(episodeId)

        assertEquals(seriesId.toString(), result)
    }

    @Test
    fun `falls back to media id when episode is missing`() = runBlocking {
        val mediaId = UUID.randomUUID()
        val lookup = DefaultEpisodeSeriesLookup(
            mediaCatalogReader = FakeMediaCatalogReader()
        )

        val result = lookup.preferenceKeyFor(mediaId)

        assertEquals(mediaId.toString(), result)
    }

    private class FakeMediaCatalogReader(
        movies: Map<UUID, Movie> = emptyMap(),
        series: Map<UUID, Series> = emptyMap(),
        episodes: Map<UUID, Episode> = emptyMap(),
    ) : MediaCatalogReader {
        override val movies: StateFlow<Map<UUID, Movie>> = MutableStateFlow(movies)
        override val series: StateFlow<Map<UUID, Series>> = MutableStateFlow(series)
        override val episodes: StateFlow<Map<UUID, Episode>> = MutableStateFlow(episodes)

        override fun observeSeriesWithContent(seriesId: UUID): Flow<Series?> = flowOf(series.value[seriesId])
    }

    private fun episode(id: UUID, seriesId: UUID) = Episode(
        id = id,
        seriesId = seriesId,
        seasonId = UUID.randomUUID(),
        index = 1,
        title = "Episode",
        synopsis = "Synopsis",
        releaseDate = "2026",
        rating = "PG",
        runtime = "42m",
        progress = null,
        watched = false,
        format = "VIDEO",
        imageUrlPrefix = "",
        cast = emptyList(),
    )
}
