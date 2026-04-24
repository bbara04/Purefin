package hu.bbara.purefin.ui.screen.libraries

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.ui.common.image.PurefinAsyncImage
import hu.bbara.purefin.feature.browse.home.LibraryItem
import hu.bbara.purefin.model.LibraryKind

internal const val TvLibrariesOverviewItemTagPrefix = "tv-libraries-overview-item-"

private val TvLibrariesOverviewCardShape = RoundedCornerShape(18.dp)

@Composable
fun TvLibrariesOverviewScreen(
    libraries: List<LibraryItem>,
    onLibrarySelected: (LibraryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val firstItemFocusRequester = remember { FocusRequester() }
    var initialFocusApplied by remember { mutableStateOf(false) }

    LaunchedEffect(libraries.size, initialFocusApplied) {
        if (!initialFocusApplied && libraries.isNotEmpty()) {
            firstItemFocusRequester.requestFocus()
            initialFocusApplied = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
            .padding(top = 24.dp)
    ) {
        Text(
            text = "Libraries",
            style = MaterialTheme.typography.headlineMedium,
            color = scheme.onBackground,
            modifier = Modifier.padding(horizontal = 28.dp)
        )
        if (libraries.isEmpty()) {
            Text(
                text = "No libraries available",
                style = MaterialTheme.typography.bodyLarge,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp)
            )
            return
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 220.dp),
            contentPadding = PaddingValues(horizontal = 28.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(libraries, key = { _, item -> item.id }) { index, item ->
                TvLibraryOverviewCard(
                    item = item,
                    onClick = { onLibrarySelected(item) },
                    modifier = Modifier
                        .then(
                            if (index == 0) {
                                Modifier.focusRequester(firstItemFocusRequester)
                            } else {
                                Modifier
                            }
                        )
                        .testTag("$TvLibrariesOverviewItemTagPrefix$index")
                )
            }
        }
    }
}

@Composable
private fun TvLibraryOverviewCard(
    item: LibraryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1f,
        label = "tv-library-overview-scale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0.5f, 0f)
            }
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(16f / 9f)
                .clip(TvLibrariesOverviewCardShape)
                .border(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = if (isFocused) scheme.primary else scheme.outlineVariant.copy(alpha = 0.45f),
                    shape = TvLibrariesOverviewCardShape
                )
                .background(scheme.surfaceVariant)
                .onFocusChanged { isFocused = it.isFocused }
                .clickable(onClick = onClick)
        ) {
            if (item.posterUrl.isNotBlank()) {
                PurefinAsyncImage(
                    model = item.posterUrl,
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = when (item.type) {
                        LibraryKind.MOVIES -> Icons.Outlined.Movie
                        LibraryKind.SERIES -> Icons.Outlined.Tv
                    },
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp)
                )
            }
        }
        Text(
            text = item.name,
            style = MaterialTheme.typography.titleMedium,
            color = scheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 10.dp)
        )
        Text(
            text = if (item.isEmpty) "Empty library" else "Open library",
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
