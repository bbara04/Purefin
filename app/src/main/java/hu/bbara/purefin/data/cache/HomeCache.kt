package hu.bbara.purefin.data.cache

import kotlinx.serialization.Serializable

@Serializable
data class CachedMediaItem(
    val type: String,
    val id: String,
    val mediaId: String? = null
)

@Serializable
data class HomeCache(
    val continueWatching: List<CachedMediaItem> = emptyList(),
    val latestLibraryContent: Map<String, List<CachedMediaItem>> = emptyMap()
)
