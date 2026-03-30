package hu.bbara.purefin.app.home.ui

import org.jellyfin.sdk.model.UUID

enum class FeaturedHomeSource {
    CONTINUE_WATCHING,
    NEXT_UP,
    LIBRARY
}

enum class HomeDestinationKind {
    MOVIE,
    SERIES,
    EPISODE
}

data class HomeDestination(
    val kind: HomeDestinationKind,
    val id: UUID,
    val seriesId: UUID? = null,
    val seasonId: UUID? = null
)

data class FeaturedHomeItem(
    val id: UUID,
    val source: FeaturedHomeSource,
    val badge: String,
    val title: String,
    val supportingText: String,
    val description: String,
    val metadata: List<String>,
    val imageUrl: String,
    val ctaLabel: String,
    val progress: Float? = null,
    val destination: HomeDestination
)
