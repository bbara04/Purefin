package hu.bbara.purefin.data.offline.cache

import kotlinx.serialization.Serializable

@Serializable
data class CachedMediaItem(
    val type: String,
    val id: String,
    val seriesId: String? = null
)

@Serializable
data class HomeCache(
    val suggestions: List<CachedMediaItem> = emptyList(),
    val continueWatching: List<CachedMediaItem> = emptyList(),
    val nextUp: List<CachedMediaItem> = emptyList(),
    val latestLibraryContent: Map<String, List<CachedMediaItem>> = emptyMap()
)
