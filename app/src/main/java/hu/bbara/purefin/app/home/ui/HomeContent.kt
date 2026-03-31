package hu.bbara.purefin.app.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.core.model.Episode
import hu.bbara.purefin.core.model.Movie
import hu.bbara.purefin.core.model.Series
import hu.bbara.purefin.feature.shared.home.ContinueWatchingItem
import hu.bbara.purefin.feature.shared.home.LibraryItem
import hu.bbara.purefin.feature.shared.home.NextUpItem
import hu.bbara.purefin.feature.shared.home.PosterItem
import hu.bbara.purefin.ui.theme.AppTheme
import org.jellyfin.sdk.model.UUID
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.CollectionType
import java.util.UUID as JavaUuid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    libraries: List<LibraryItem>,
    libraryContent: Map<UUID, List<PosterItem>>,
    continueWatching: List<ContinueWatchingItem>,
    nextUp: List<NextUpItem>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onMovieSelected: (UUID) -> Unit,
    onSeriesSelected: (UUID) -> Unit,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
    onLibrarySelected: (LibraryItem) -> Unit,
    onBrowseLibrariesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val visibleLibraries = remember(libraries, libraryContent) {
        libraries.filter { libraryContent[it.id].orEmpty().isNotEmpty() }
    }
    val featuredItems = remember(continueWatching, nextUp, visibleLibraries, libraryContent) {
        buildFeaturedItems(
            continueWatching = continueWatching,
            nextUp = nextUp,
            visibleLibraries = visibleLibraries,
            libraryContent = libraryContent
        )
    }
    val featuredLead = featuredItems.firstOrNull()
    val filteredContinueWatching = remember(continueWatching, featuredLead) {
        if (featuredLead?.source == FeaturedHomeSource.CONTINUE_WATCHING) {
            continueWatching.filterNot { it.id == featuredLead.id }
        } else {
            continueWatching
        }
    }
    val filteredNextUp = remember(nextUp, featuredLead) {
        if (featuredLead?.source == FeaturedHomeSource.NEXT_UP) {
            nextUp.filterNot { it.id == featuredLead.id }
        } else {
            nextUp
        }
    }
    val hasContent = featuredItems.isNotEmpty() ||
        filteredContinueWatching.isNotEmpty() ||
        filteredNextUp.isNotEmpty() ||
        visibleLibraries.isNotEmpty()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scheme.background)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (featuredItems.isNotEmpty()) {
                    item(key = "featured") {
                        HomeFeaturedSection(
                            items = featuredItems,
                            onOpenFeaturedItem = { item ->
                                openHomeDestination(
                                    destination = item.destination,
                                    onMovieSelected = onMovieSelected,
                                    onSeriesSelected = onSeriesSelected,
                                    onEpisodeSelected = onEpisodeSelected
                                )
                            }
                        )
                    }
                }

                if (filteredContinueWatching.isNotEmpty()) {
                    item(key = "continue-watching") {
                        ContinueWatchingSection(
                            items = filteredContinueWatching,
                            onMovieSelected = onMovieSelected,
                            onEpisodeSelected = onEpisodeSelected
                        )
                    }
                }

                if (filteredNextUp.isNotEmpty()) {
                    item(key = "next-up") {
                        NextUpSection(
                            items = filteredNextUp,
                            onEpisodeSelected = onEpisodeSelected
                        )
                    }
                }

                items(
                    items = visibleLibraries,
                    key = { library -> library.id }
                ) { library ->
                    LibraryPosterSection(
                        library = library,
                        items = libraryContent[library.id].orEmpty(),
                        onLibrarySelected = onLibrarySelected,
                        onMovieSelected = onMovieSelected,
                        onSeriesSelected = onSeriesSelected,
                        onEpisodeSelected = onEpisodeSelected
                    )
                }

                if (!hasContent) {
                    item(key = "empty-state") {
                        HomeEmptyState(
                            onRefresh = onRefresh,
                            onBrowseLibrariesClick = onBrowseLibrariesClick
                        )
                    }
                }
            }
        }
    }
}

private fun buildFeaturedItems(
    continueWatching: List<ContinueWatchingItem>,
    nextUp: List<NextUpItem>,
    visibleLibraries: List<LibraryItem>,
    libraryContent: Map<UUID, List<PosterItem>>
): List<FeaturedHomeItem> {
    val candidates = buildList {
        addAll(continueWatching.map { it.toFeaturedHomeItem() })
        addAll(nextUp.map { it.toFeaturedHomeItem() })
        visibleLibraries.forEach { library ->
            libraryContent[library.id]
                .orEmpty()
                .firstOrNull()
                ?.let { add(it.toFeaturedHomeItem(library)) }
        }
    }
    return candidates
        .distinctBy { "${it.destination.kind}:${it.id}" }
        .take(5)
}

private fun ContinueWatchingItem.toFeaturedHomeItem(): FeaturedHomeItem {
    return when (type) {
        BaseItemKind.MOVIE -> {
            val movie = movie!!
            FeaturedHomeItem(
                id = movie.id,
                source = FeaturedHomeSource.CONTINUE_WATCHING,
                badge = "Continue watching",
                title = movie.title,
                supportingText = listOf(movie.year, movie.runtime)
                    .filter { it.isNotBlank() }
                    .joinToString(" • "),
                description = movie.synopsis,
                metadata = listOf(movie.year, movie.runtime, movie.rating, movie.format)
                    .filter { it.isNotBlank() },
                imageUrl = movie.heroImageUrl,
                ctaLabel = "Continue",
                progress = progress.toFloat() / 100f,
                destination = HomeDestination(
                    kind = HomeDestinationKind.MOVIE,
                    id = movie.id
                )
            )
        }

        BaseItemKind.EPISODE -> {
            val episode = episode!!
            FeaturedHomeItem(
                id = episode.id,
                source = FeaturedHomeSource.CONTINUE_WATCHING,
                badge = "Continue watching",
                title = episode.title,
                supportingText = listOf("Episode ${episode.index}", episode.runtime)
                    .filter { it.isNotBlank() }
                    .joinToString(" • "),
                description = episode.synopsis,
                metadata = listOf(episode.releaseDate, episode.runtime, episode.rating, episode.format)
                    .filter { it.isNotBlank() },
                imageUrl = episode.heroImageUrl,
                ctaLabel = "Continue",
                progress = progress.toFloat() / 100f,
                destination = HomeDestination(
                    kind = HomeDestinationKind.EPISODE,
                    id = episode.id,
                    seriesId = episode.seriesId,
                    seasonId = episode.seasonId
                )
            )
        }

        else -> throw IllegalArgumentException("Unsupported featured type: $type")
    }
}

private fun NextUpItem.toFeaturedHomeItem(): FeaturedHomeItem {
    return FeaturedHomeItem(
        id = episode.id,
        source = FeaturedHomeSource.NEXT_UP,
        badge = "Next up",
        title = episode.title,
        supportingText = listOf("Episode ${episode.index}", episode.runtime)
            .filter { it.isNotBlank() }
            .joinToString(" • "),
        description = episode.synopsis,
        metadata = listOf(episode.releaseDate, episode.runtime, episode.rating)
            .filter { it.isNotBlank() },
        imageUrl = episode.heroImageUrl,
        ctaLabel = "Up next",
        destination = HomeDestination(
            kind = HomeDestinationKind.EPISODE,
            id = episode.id,
            seriesId = episode.seriesId,
            seasonId = episode.seasonId
        )
    )
}

private fun PosterItem.toFeaturedHomeItem(library: LibraryItem): FeaturedHomeItem {
    return when (type) {
        BaseItemKind.MOVIE -> {
            val movie = movie!!
            FeaturedHomeItem(
                id = movie.id,
                source = FeaturedHomeSource.LIBRARY,
                badge = library.name,
                title = movie.title,
                supportingText = listOf(movie.year, movie.runtime)
                    .filter { it.isNotBlank() }
                    .joinToString(" • "),
                description = movie.synopsis,
                metadata = listOf(movie.year, movie.runtime, movie.rating)
                    .filter { it.isNotBlank() },
                imageUrl = movie.heroImageUrl,
                ctaLabel = "Open",
                destination = HomeDestination(
                    kind = HomeDestinationKind.MOVIE,
                    id = movie.id
                )
            )
        }

        BaseItemKind.SERIES -> {
            val series = series!!
            FeaturedHomeItem(
                id = series.id,
                source = FeaturedHomeSource.LIBRARY,
                badge = library.name,
                title = series.name,
                supportingText = when {
                    series.unwatchedEpisodeCount > 0 ->
                        "${series.unwatchedEpisodeCount} unwatched episodes"
                    else -> "${series.seasonCount} seasons"
                },
                description = series.synopsis,
                metadata = listOf(series.year, "${series.seasonCount} seasons")
                    .filter { it.isNotBlank() },
                imageUrl = series.heroImageUrl,
                ctaLabel = "Open",
                destination = HomeDestination(
                    kind = HomeDestinationKind.SERIES,
                    id = series.id
                )
            )
        }

        BaseItemKind.EPISODE -> {
            val episode = episode!!
            FeaturedHomeItem(
                id = episode.id,
                source = FeaturedHomeSource.LIBRARY,
                badge = library.name,
                title = episode.title,
                supportingText = listOf("Episode ${episode.index}", episode.runtime)
                    .filter { it.isNotBlank() }
                    .joinToString(" • "),
                description = episode.synopsis,
                metadata = listOf(episode.releaseDate, episode.runtime, episode.rating)
                    .filter { it.isNotBlank() },
                imageUrl = episode.heroImageUrl,
                ctaLabel = "Open",
                destination = HomeDestination(
                    kind = HomeDestinationKind.EPISODE,
                    id = episode.id,
                    seriesId = episode.seriesId,
                    seasonId = episode.seasonId
                )
            )
        }

        else -> throw IllegalArgumentException("Unsupported featured type: $type")
    }
}

private fun openHomeDestination(
    destination: HomeDestination,
    onMovieSelected: (UUID) -> Unit,
    onSeriesSelected: (UUID) -> Unit,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
) {
    when (destination.kind) {
        HomeDestinationKind.MOVIE -> onMovieSelected(destination.id)
        HomeDestinationKind.SERIES -> onSeriesSelected(destination.id)
        HomeDestinationKind.EPISODE -> onEpisodeSelected(
            destination.seriesId ?: return,
            destination.seasonId ?: return,
            destination.id
        )
    }
}

@Preview(name = "Home Full", showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun HomeContentPreview() {
    AppTheme(darkTheme = true) {
        HomeContent(
            libraries = homePreviewLibraries(),
            libraryContent = homePreviewLibraryContent(),
            continueWatching = homePreviewContinueWatching(),
            nextUp = homePreviewNextUp(),
            isRefreshing = false,
            onRefresh = {},
            onMovieSelected = {},
            onSeriesSelected = {},
            onEpisodeSelected = { _, _, _ -> },
            onLibrarySelected = {},
            onBrowseLibrariesClick = {}
        )
    }
}

@Preview(name = "Home Libraries Only", showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun HomeLibrariesOnlyPreview() {
    AppTheme(darkTheme = true) {
        HomeContent(
            libraries = homePreviewLibraries(),
            libraryContent = homePreviewLibraryContent(),
            continueWatching = emptyList(),
            nextUp = emptyList(),
            isRefreshing = false,
            onRefresh = {},
            onMovieSelected = {},
            onSeriesSelected = {},
            onEpisodeSelected = { _, _, _ -> },
            onLibrarySelected = {},
            onBrowseLibrariesClick = {}
        )
    }
}

@Preview(name = "Home Empty", showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun HomeEmptyPreview() {
    AppTheme(darkTheme = false) {
        HomeContent(
            libraries = emptyList(),
            libraryContent = emptyMap(),
            continueWatching = emptyList(),
            nextUp = emptyList(),
            isRefreshing = false,
            onRefresh = {},
            onMovieSelected = {},
            onSeriesSelected = {},
            onEpisodeSelected = { _, _, _ -> },
            onLibrarySelected = {},
            onBrowseLibrariesClick = {}
        )
    }
}

internal fun homePreviewLibraries(): List<LibraryItem> {
    return listOf(
        LibraryItem(
            id = JavaUuid.fromString("11111111-1111-1111-1111-111111111111"),
            name = "Movies",
            type = CollectionType.MOVIES,
            posterUrl = "https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c",
            isEmpty = false
        ),
        LibraryItem(
            id = JavaUuid.fromString("22222222-2222-2222-2222-222222222222"),
            name = "Series",
            type = CollectionType.TVSHOWS,
            posterUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba",
            isEmpty = false
        )
    )
}

internal fun homePreviewLibraryContent(): Map<UUID, List<PosterItem>> {
    val movie = homePreviewMovie(
        id = "33333333-3333-3333-3333-333333333333",
        title = "Blade Runner 2049",
        year = "2017",
        runtime = "2h 44m",
        rating = "16+",
        format = "Dolby Vision",
        synopsis = "A young blade runner uncovers a buried secret that pulls him toward a vanished legend.",
        heroImageUrl = "https://images.unsplash.com/photo-1519608487953-e999c86e7455",
        progress = 42.0,
        watched = false
    )
    val secondMovie = homePreviewMovie(
        id = "44444444-4444-4444-4444-444444444444",
        title = "Arrival",
        year = "2016",
        runtime = "1h 56m",
        rating = "12+",
        format = "4K",
        synopsis = "A linguist is recruited when mysterious spacecraft touch down around the world.",
        heroImageUrl = "https://images.unsplash.com/photo-1500534314209-a25ddb2bd429",
        progress = null,
        watched = false
    )
    val series = homePreviewSeries()
    val episode = homePreviewEpisode(
        id = "66666666-6666-6666-6666-666666666666",
        title = "Signals",
        index = 2,
        releaseDate = "2025",
        runtime = "48m",
        rating = "16+",
        progress = 18.0,
        watched = false,
        heroImageUrl = "https://images.unsplash.com/photo-1520034475321-cbe63696469a",
        synopsis = "Anomalies around the station point to a cover-up."
    )

    return mapOf(
        homePreviewLibraries()[0].id to listOf(
            PosterItem(type = BaseItemKind.MOVIE, movie = movie),
            PosterItem(type = BaseItemKind.MOVIE, movie = secondMovie)
        ),
        homePreviewLibraries()[1].id to listOf(
            PosterItem(type = BaseItemKind.SERIES, series = series),
            PosterItem(type = BaseItemKind.EPISODE, episode = episode)
        )
    )
}

internal fun homePreviewContinueWatching(): List<ContinueWatchingItem> {
    return listOf(
        ContinueWatchingItem(
            type = BaseItemKind.MOVIE,
            movie = homePreviewMovie(
                id = "77777777-7777-7777-7777-777777777777",
                title = "Dune: Part Two",
                year = "2024",
                runtime = "2h 46m",
                rating = "13+",
                format = "IMAX",
                synopsis = "Paul Atreides unites with the Fremen while seeking justice and revenge.",
                heroImageUrl = "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa",
                progress = 58.0,
                watched = false
            )
        ),
        ContinueWatchingItem(
            type = BaseItemKind.EPISODE,
            episode = homePreviewEpisode(
                id = "88888888-8888-8888-8888-888888888888",
                title = "A Fresh Start",
                index = 1,
                releaseDate = "2025",
                runtime = "51m",
                rating = "16+",
                progress = 23.0,
                watched = false,
                heroImageUrl = "https://images.unsplash.com/photo-1497032205916-ac775f0649ae",
                synopsis = "A fractured crew tries to reassemble after a year apart."
            )
        )
    )
}

internal fun homePreviewNextUp(): List<NextUpItem> {
    return listOf(
        NextUpItem(
            episode = homePreviewEpisode(
                id = "99999999-9999-9999-9999-999999999999",
                title = "Return Window",
                index = 3,
                releaseDate = "2025",
                runtime = "54m",
                rating = "16+",
                progress = null,
                watched = false,
                heroImageUrl = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee",
                synopsis = "A high-risk jump changes the rules of the mission."
            )
        )
    )
}

internal fun homePreviewMovie(
    id: String,
    title: String,
    year: String,
    runtime: String,
    rating: String,
    format: String,
    synopsis: String,
    heroImageUrl: String,
    progress: Double?,
    watched: Boolean
): Movie {
    return Movie(
        id = JavaUuid.fromString(id),
        libraryId = JavaUuid.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
        title = title,
        progress = progress,
        watched = watched,
        year = year,
        rating = rating,
        runtime = runtime,
        format = format,
        synopsis = synopsis,
        heroImageUrl = heroImageUrl,
        audioTrack = "English 5.1",
        subtitles = "English CC",
        cast = emptyList()
    )
}

internal fun homePreviewSeries(): Series {
    return Series(
        id = JavaUuid.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
        libraryId = JavaUuid.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
        name = "Orbital",
        synopsis = "A reluctant crew returns to a damaged station as political pressure mounts on Earth.",
        year = "2025",
        heroImageUrl = "https://images.unsplash.com/photo-1520034475321-cbe63696469a",
        unwatchedEpisodeCount = 4,
        seasonCount = 2,
        seasons = emptyList(),
        cast = emptyList()
    )
}

internal fun homePreviewEpisode(
    id: String,
    title: String,
    index: Int,
    releaseDate: String,
    runtime: String,
    rating: String,
    progress: Double?,
    watched: Boolean,
    heroImageUrl: String,
    synopsis: String
): Episode {
    return Episode(
        id = JavaUuid.fromString(id),
        seriesId = JavaUuid.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"),
        seasonId = JavaUuid.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"),
        index = index,
        title = title,
        synopsis = synopsis,
        releaseDate = releaseDate,
        rating = rating,
        runtime = runtime,
        progress = progress,
        watched = watched,
        format = "4K",
        heroImageUrl = heroImageUrl,
        cast = emptyList()
    )
}
