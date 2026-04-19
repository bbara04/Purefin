package hu.bbara.purefin.ui.screen.home.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.bbara.purefin.core.ui.model.EpisodeUiModel
import hu.bbara.purefin.core.ui.model.MediaUiModel
import hu.bbara.purefin.ui.common.bar.MediaProgressBar
import hu.bbara.purefin.ui.common.card.PosterCard
import hu.bbara.purefin.ui.common.image.PurefinAsyncImage
import java.util.UUID

private val TvHomeSectionsThumbShape = RoundedCornerShape(20.dp)
private val TvHomeSectionsHorizontalPadding = 32.dp
private val TvHomeSectionsRowSpacing = 18.dp
private val TvHomeLandscapeCardWidth = 248.dp
private val TvHomePosterCardWidth = 136.dp
internal const val TvHomeSectionRowTagPrefix = "tv-home-section-row-"
internal const val TvHomeContinueWatchingRowTag = "${TvHomeSectionRowTagPrefix}continue-watching"
internal const val TvHomeNextUpRowTag = "${TvHomeSectionRowTagPrefix}next-up"

internal fun tvHomeLibraryRowTag(libraryId: UUID): String =
    "${TvHomeSectionRowTagPrefix}library-$libraryId"

@Composable
fun TvContinueWatchingSection(
    items: List<MediaUiModel>,
    onFocusedItem: (MediaUiModel) -> Unit = {},
    onMovieSelected: (UUID) -> Unit,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
    firstItemFocusRequester: FocusRequester? = null,
    firstItemTestTag: String? = null,
    rowTestTag: String? = null,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return
    TvSectionHeader(
        title = "Continue Watching",
    )
    TvHomeSectionRow(
        modifier = modifier,
        rowTestTag = rowTestTag
    ) {
        itemsIndexed(items = items, key = { _, item -> item.id }) { index, item ->
            TvHomeLandscapeCard(
                title = item.primaryText,
                supporting = item.secondaryText,
                imageUrl = item.primaryImageUrl,
                progress = item.progress ?: 0f,
                imageModifier = Modifier
                    .then(
                        if (index == 0 && firstItemFocusRequester != null) {
                            Modifier.focusRequester(firstItemFocusRequester)
                        } else {
                            Modifier
                        }
                    )
                    .then(
                        if (index == 0 && firstItemTestTag != null) {
                            Modifier.testTag(firstItemTestTag)
                        } else {
                            Modifier
                        }
                    ),
                onFocusedItem = { onFocusedItem(item) },
                onClick = {
                    when (item) {
                        //TODO fix this shit
                        is EpisodeUiModel -> onEpisodeSelected(item.seriesId, item.seasonId, item.id)
                        else -> Unit
                    }
                }
            )
        }
    }
}

@Composable
fun TvNextUpSection(
    items: List<MediaUiModel>,
    onFocusedItem: (MediaUiModel) -> Unit = {},
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
    firstItemFocusRequester: FocusRequester? = null,
    firstItemTestTag: String? = null,
    rowTestTag: String? = null,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return
    TvSectionHeader(
        title = "Next Up",
    )
    TvHomeSectionRow(
        modifier = modifier,
        rowTestTag = rowTestTag
    ) {
        itemsIndexed(items = items, key = { _, item -> item.id }) { index, item ->
            TvHomeLandscapeCard(
                title = item.primaryText,
                supporting = item.secondaryText,
                imageUrl = item.primaryImageUrl,
                imageModifier = Modifier
                    .then(
                        if (index == 0 && firstItemFocusRequester != null) {
                            Modifier.focusRequester(firstItemFocusRequester)
                        } else {
                            Modifier
                        }
                    )
                    .then(
                        if (index == 0 && firstItemTestTag != null) {
                            Modifier.testTag(firstItemTestTag)
                        } else {
                            Modifier
                        }
                    ),
                onFocusedItem = { onFocusedItem(item) },
                onClick = {
                    //TODO FIX
                    (item as? EpisodeUiModel)?.let { episode ->
                        onEpisodeSelected(episode.seriesId, episode.seasonId, episode.id)
                    }
                }
            )
        }
    }
}

@Composable
fun TvLibraryPosterSection(
    title: String,
    items: List<MediaUiModel>,
    onFocusedItem: (MediaUiModel) -> Unit = {},
    firstItemFocusRequester: FocusRequester? = null,
    firstItemTestTag: String? = null,
    rowTestTag: String? = null,
    modifier: Modifier = Modifier,
    onMovieSelected: (UUID) -> Unit,
    onSeriesSelected: (UUID) -> Unit,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
) {
    TvSectionHeader(
        title = title,
    )
    TvHomeSectionRow(
        modifier = modifier,
        rowTestTag = rowTestTag
    ) {
        itemsIndexed(items = items, key = { _, item -> item.id }) { index, item ->
            PosterCard(
                item = item,
                posterWidth = TvHomePosterCardWidth,
                showSecondaryText = true,
                indicatorSize = 24,
                indicatorPadding = 6.dp,
                imageModifier = Modifier
                    .then(
                        if (index == 0 && firstItemFocusRequester != null) {
                            Modifier.focusRequester(firstItemFocusRequester)
                        } else {
                            Modifier
                        }
                    )
                    .then(
                        if (index == 0 && firstItemTestTag != null) {
                            Modifier.testTag(firstItemTestTag)
                        } else {
                            Modifier
                        }
                    ),
                onFocusedItem = onFocusedItem,
                onMovieSelected = onMovieSelected,
                onSeriesSelected = onSeriesSelected,
                onEpisodeSelected = onEpisodeSelected
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TvHomeSectionRow(
    modifier: Modifier = Modifier,
    rowTestTag: String? = null,
    content: LazyListScope.() -> Unit,
) {
    CompositionLocalProvider(LocalBringIntoViewSpec provides TvHomeRowBringIntoViewSpec) {
        LazyRow(
            modifier = modifier
                .fillMaxWidth()
                .then(
                    if (rowTestTag != null) {
                        Modifier.testTag(rowTestTag)
                    } else {
                        Modifier
                    }
                ),
            contentPadding = PaddingValues(horizontal = TvHomeSectionsHorizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(TvHomeSectionsRowSpacing),
            content = content
        )
    }
}

@Composable
fun TvSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = TvHomeSectionsHorizontalPadding, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = scheme.onBackground,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TvHomeLandscapeCard(
    title: String,
    supporting: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    imageModifier: Modifier = Modifier,
    progress: Float? = null,
    onFocusedItem: () -> Unit = {},
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isFocused) 1.055f else 1f, label = "tv-home-landscape-scale")
    val cardWidth = TvHomeLandscapeCardWidth

    Column(
        modifier = modifier
            .width(cardWidth)
            .wrapContentHeight()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0.5f, 0f)
            }
    ) {
        Box(
            modifier = Modifier
                .width(cardWidth)
                .aspectRatio(16f / 9f)
                .clip(TvHomeSectionsThumbShape)
                .border(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = if (isFocused) scheme.primary else scheme.outlineVariant.copy(alpha = 0.6f),
                    shape = TvHomeSectionsThumbShape
                )
                .background(scheme.surfaceContainer)
        ) {
            PurefinAsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = imageModifier
                    .fillMaxSize()
                    .onFocusChanged {
                        isFocused = it.isFocused
                        if (it.isFocused) {
                            onFocusedItem()
                        }
                    }
                    .clickable(onClick = onClick),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                scheme.background.copy(alpha = 0.04f),
                                scheme.background.copy(alpha = 0.12f),
                                scheme.background.copy(alpha = 0.56f)
                            )
                        )
                    )
            )
            if (progress != null && progress > 0f) {
                MediaProgressBar(
                    progress = progress,
                    foregroundColor = scheme.primary,
                    backgroundColor = scheme.surfaceVariant,
                    contentPadding = PaddingValues(0.dp),
                    barHeight = 6.dp,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                )
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(top = 12.dp, start = 4.dp, end = 4.dp)
        ) {
            Text(
                text = title,
                color = scheme.onBackground,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (supporting.isNotBlank()) {
                Text(
                    text = supporting,
                    color = scheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
