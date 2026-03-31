package hu.bbara.purefin.app.home.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.common.ui.components.MediaProgressBar
import hu.bbara.purefin.common.ui.components.PurefinAsyncImage
import hu.bbara.purefin.common.ui.components.UnwatchedEpisodeIndicator
import hu.bbara.purefin.common.ui.components.WatchStateIndicator
import hu.bbara.purefin.feature.shared.home.ContinueWatchingItem
import hu.bbara.purefin.feature.shared.home.LibraryItem
import hu.bbara.purefin.feature.shared.home.NextUpItem
import hu.bbara.purefin.feature.shared.home.PosterItem
import org.jellyfin.sdk.model.UUID
import org.jellyfin.sdk.model.api.BaseItemKind

@Composable
fun HomeFeaturedSection(
    items: List<FeaturedHomeItem>,
    onOpenFeaturedItem: (FeaturedHomeItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { items.size })

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
    ) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(start = 16.dp, end = 56.dp),
            pageSpacing = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            HomeFeaturedCard(
                item = items[page],
                onClick = { onOpenFeaturedItem(items[page]) }
            )
        }
        if (items.size > 1) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                repeat(items.size) { index ->
                    val selected = pagerState.currentPage == index
                    val indicatorWidth = animateDpAsState(
                        targetValue = if (selected) 22.dp else 8.dp
                    )
                    val indicatorColor = animateColorAsState(
                        targetValue = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        }
                    )
                    Box(
                        modifier = Modifier
                            .width(indicatorWidth.value)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(indicatorColor.value)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeFeaturedCard(
    item: FeaturedHomeItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val description = item.description.trim()

    Surface(
        color = scheme.surfaceContainerLow,
        shape = RoundedCornerShape(30.dp),
        tonalElevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 11f)
        ) {
            PurefinAsyncImage(
                model = item.imageUrl,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.08f),
                                Color.Black.copy(alpha = 0.18f),
                                Color.Black.copy(alpha = 0.72f)
                            )
                        )
                    )
            )
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = item.title
                    )
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.metadata.isNotEmpty()) {
                        Text(
                            text = item.metadata.joinToString(" • "),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.88f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (description.isNotBlank()) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.88f),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 520.dp)
                        )
                    }
                }
            }
            if (item.progress != null && item.progress > 0f) {
                MediaProgressBar(
                    progress = item.progress.coerceIn(0f, 1f),
                    foregroundColor = scheme.primary,
                    backgroundColor = Color.White.copy(alpha = 0.26f),
                    modifier = Modifier.align(Alignment.BottomStart)
                )
            }
        }
    }
}

@Composable
fun ContinueWatchingSection(
    items: List<ContinueWatchingItem>,
    onMovieSelected: (UUID) -> Unit,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        SectionHeader(title = "Continue Watching")
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items = items, key = { item -> item.id }) { item ->
                ContinueWatchingCard(
                    item = item,
                    onMovieSelected = onMovieSelected,
                    onEpisodeSelected = onEpisodeSelected
                )
            }
        }
    }
}

@Composable
private fun ContinueWatchingCard(
    item: ContinueWatchingItem,
    onMovieSelected: (UUID) -> Unit,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val supportingText = when (item.type) {
        BaseItemKind.MOVIE -> listOf(
            item.movie?.year,
            item.movie?.runtime
        ).filterNotNull().filter { it.isNotBlank() }.joinToString(" • ")

        BaseItemKind.EPISODE -> listOf(
            "Episode ${item.episode?.index}",
            item.episode?.runtime
        ).filterNotNull().filter { it.isNotBlank() }.joinToString(" • ")

        else -> ""
    }
    val imageUrl = when (item.type) {
        BaseItemKind.MOVIE -> item.movie?.heroImageUrl
        BaseItemKind.EPISODE -> item.episode?.heroImageUrl
        else -> null
    }

    Surface(
        shape = RoundedCornerShape(26.dp),
        color = scheme.surfaceContainer,
        tonalElevation = 3.dp,
        modifier = modifier.width(320.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    when (item.type) {
                        BaseItemKind.MOVIE -> onMovieSelected(item.movie!!.id)
                        BaseItemKind.EPISODE -> {
                            val episode = item.episode!!
                            onEpisodeSelected(episode.seriesId, episode.seasonId, episode.id)
                        }

                        else -> Unit
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(scheme.surfaceContainer)
            ) {
                if (imageUrl != null) {
                    PurefinAsyncImage(
                        model = imageUrl,
                        contentDescription = item.primaryText,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.08f),
                                    Color.Black.copy(alpha = 0.38f)
                                )
                            )
                        )
                )
                MediaProgressBar(
                    progress = (item.progress.toFloat() / 100f).coerceIn(0f, 1f),
                    foregroundColor = scheme.primary,
                    backgroundColor = Color.White.copy(alpha = 0.24f),
                    modifier = Modifier.align(Alignment.BottomStart)
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = item.primaryText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (supportingText.isNotBlank()) {
                    Text(
                        text = supportingText,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun NextUpSection(
    items: List<NextUpItem>,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        SectionHeader(title = "Next Up")
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items = items, key = { item -> item.id }) { item ->
                NextUpCard(
                    item = item,
                    onEpisodeSelected = onEpisodeSelected
                )
            }
        }
    }
}

@Composable
private fun NextUpCard(
    item: NextUpItem,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = scheme.surfaceContainer,
        tonalElevation = 2.dp,
        modifier = modifier.width(256.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onEpisodeSelected(
                        item.episode.seriesId, item.episode.seasonId, item.episode.id
                    )
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .background(scheme.surfaceVariant)
            ) {
                PurefinAsyncImage(
                    model = item.episode.heroImageUrl,
                    contentDescription = item.primaryText,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.26f)
                                )
                            )
                        )
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Text(
                    text = item.primaryText,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = listOf("Episode ${item.episode.index}", item.episode.runtime, item.secondaryText)
                        .filter { it.isNotBlank() }
                        .joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun LibraryPosterSection(
    library: LibraryItem,
    items: List<PosterItem>,
    onLibrarySelected: (LibraryItem) -> Unit,
    onMovieSelected: (UUID) -> Unit,
    onSeriesSelected: (UUID) -> Unit,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        SectionHeader(
            title = library.name,
            actionLabel = "See all",
            onActionClick = { onLibrarySelected(library) }
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items = items, key = { item -> item.id }) { item ->
                HomeBrowseCard(
                    item = item,
                    onMovieSelected = onMovieSelected,
                    onSeriesSelected = onSeriesSelected,
                    onEpisodeSelected = onEpisodeSelected
                )
            }
        }
    }
}

@Composable
private fun HomeBrowseCard(
    item: PosterItem,
    onMovieSelected: (UUID) -> Unit,
    onSeriesSelected: (UUID) -> Unit,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val supportingText = when (item.type) {
        BaseItemKind.MOVIE -> listOf(
            item.movie?.year,
            item.movie?.runtime
        ).filterNotNull().filter { it.isNotBlank() }.joinToString(" • ")

        BaseItemKind.SERIES -> item.series!!.let { series ->
            if (series.seasonCount == 1) "1 season" else "${series.seasonCount} seasons"
        }

        BaseItemKind.EPISODE -> listOf(
            "Episode ${item.episode?.index}",
            item.episode?.runtime
        ).filterNotNull().filter { it.isNotBlank() }.joinToString(" • ")

        else -> ""
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = scheme.surfaceContainer,
        modifier = modifier.width(188.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    when (item.type) {
                        BaseItemKind.MOVIE -> onMovieSelected(item.id)
                        BaseItemKind.SERIES -> onSeriesSelected(item.id)
                        BaseItemKind.EPISODE -> {
                            val episode = item.episode!!
                            onEpisodeSelected(episode.seriesId, episode.seasonId, episode.id)
                        }

                        else -> Unit
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .border(
                        1.dp, scheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(18.dp)
                    )
                    .background(scheme.surfaceVariant)
            ) {
                PurefinAsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                when (item.type) {
                    BaseItemKind.MOVIE -> {
                        val movie = item.movie!!
                        WatchStateIndicator(
                            size = 28,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp),
                            watched = movie.watched,
                            started = (movie.progress ?: 0.0) > 0
                        )
                    }

                    BaseItemKind.EPISODE -> {
                        val episode = item.episode!!
                        WatchStateIndicator(
                            size = 28,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp),
                            watched = episode.watched,
                            started = (episode.progress ?: 0.0) > 0
                        )
                    }

                    BaseItemKind.SERIES -> {
                        UnwatchedEpisodeIndicator(
                            size = 28,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp),
                            unwatchedCount = item.series!!.unwatchedEpisodeCount
                        )
                    }

                    else -> Unit
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Column(modifier = modifier.padding(12.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (supportingText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = supportingText,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    modifier: Modifier = Modifier,
    onActionClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        if (actionLabel != null) {
            TextButton(onClick = onActionClick) {
                Text(text = actionLabel)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun HomeEmptyState(
    onRefresh: () -> Unit,
    onBrowseLibrariesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(30.dp),
        color = scheme.surfaceContainerLow,
        tonalElevation = 2.dp,
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            ContentBadge(
                text = "Home is warming up",
                containerColor = scheme.primaryContainer,
                contentColor = scheme.onPrimaryContainer
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Nothing is on deck yet",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Pull to refresh for recent activity or jump into your libraries to start browsing.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = scheme.onSurfaceVariant
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledTonalButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Refresh")
                }
                OutlinedButton(onClick = onBrowseLibrariesClick) {
                    Icon(
                        imageVector = Icons.Outlined.Collections,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Browse libraries")
                }
            }
        }
    }
}

@Composable
private fun ContentBadge(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = containerColor,
        shape = CircleShape,
        modifier = modifier
    ) {
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
