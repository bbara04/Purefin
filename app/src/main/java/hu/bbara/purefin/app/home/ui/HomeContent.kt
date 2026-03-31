package hu.bbara.purefin.app.home.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.app.home.ui.continuewatching.ContinueWatchingSection
import hu.bbara.purefin.app.home.ui.featured.SuggestionsSection
import hu.bbara.purefin.app.home.ui.library.LibraryPosterSection
import hu.bbara.purefin.app.home.ui.nextup.NextUpSection
import hu.bbara.purefin.app.home.ui.shared.HomeEmptyState
import hu.bbara.purefin.core.model.Episode
import hu.bbara.purefin.core.model.Movie
import hu.bbara.purefin.core.model.Series
import hu.bbara.purefin.feature.shared.home.ContinueWatchingItem
import hu.bbara.purefin.feature.shared.home.LibraryItem
import hu.bbara.purefin.feature.shared.home.NextUpItem
import hu.bbara.purefin.feature.shared.home.PosterItem
import hu.bbara.purefin.feature.shared.home.SuggestedItem
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
    suggestions: List<SuggestedItem>,
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
    val listState = rememberLazyListState()
    var pendingInitialSuggestionsReveal by rememberSaveable { mutableStateOf(suggestions.isEmpty()) }
    var userInteractedBeforeSuggestionsLoaded by rememberSaveable { mutableStateOf(false) }

    val hasContent = libraryContent.isNotEmpty() || continueWatching.isNotEmpty() || nextUp.isNotEmpty() || suggestions.isNotEmpty()

    LaunchedEffect(listState, pendingInitialSuggestionsReveal) {
        if (!pendingInitialSuggestionsReveal) return@LaunchedEffect
        snapshotFlow { listState.isScrollInProgress }
            .collect { isScrolling ->
                if (isScrolling) {
                    userInteractedBeforeSuggestionsLoaded = true
                }
            }
    }

    LaunchedEffect(
        suggestions.isNotEmpty(),
        pendingInitialSuggestionsReveal,
        userInteractedBeforeSuggestionsLoaded
    ) {
        if (!suggestions.isNotEmpty() || !pendingInitialSuggestionsReveal) return@LaunchedEffect
        if (!userInteractedBeforeSuggestionsLoaded) {
            listState.scrollToItem(0)
        }
        pendingInitialSuggestionsReveal = false
    }

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
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (suggestions.isNotEmpty()) {
                    item(key = "featured") {
                        SuggestionsSection(
                            items = suggestions,
                            onItemOpen = { item ->
                                when (item.type) {
                                    BaseItemKind.MOVIE -> onMovieSelected(item.id)
                                    BaseItemKind.SERIES -> onSeriesSelected(item.id)
                                    BaseItemKind.EPISODE -> onEpisodeSelected(item.id, item.id, item.id)
                                    else -> {
                                        Log.e("HomeContent", "Unsupported item type: ${item.type}")
                                    }
                                }
                            }
                        )
                    }
                }

                if (continueWatching.isNotEmpty()) {
                    item(key = "continue-watching") {
                        ContinueWatchingSection(
                            items = continueWatching,
                            onMovieSelected = onMovieSelected,
                            onEpisodeSelected = onEpisodeSelected
                        )
                    }
                }

                if (nextUp.isNotEmpty()) {
                    item(key = "next-up") {
                        NextUpSection(
                            items = nextUp,
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

@Preview(name = "Home Full", showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun HomeContentPreview() {
    AppTheme(darkTheme = true) {
        HomeContent(
            libraries = homePreviewLibraries(),
            libraryContent = homePreviewLibraryContent(),
            suggestions = emptyList(),
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
            suggestions = emptyList(),
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
            suggestions = emptyList(),
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
