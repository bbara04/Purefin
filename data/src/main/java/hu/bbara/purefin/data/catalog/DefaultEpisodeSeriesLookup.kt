package hu.bbara.purefin.data.catalog

import hu.bbara.purefin.data.EpisodeSeriesLookup
import hu.bbara.purefin.data.MediaCatalogReader
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultEpisodeSeriesLookup @Inject constructor(
    private val mediaCatalogReader: MediaCatalogReader,
) : EpisodeSeriesLookup {
    override suspend fun preferenceKeyFor(mediaId: UUID): String {
        return mediaCatalogReader.episodes.value[mediaId]?.seriesId?.toString() ?: mediaId.toString()
    }
}
