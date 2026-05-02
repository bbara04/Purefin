package hu.bbara.purefin.ui.screen.home.components.search

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.bbara.purefin.ui.common.image.PurefinAsyncImage
import hu.bbara.purefin.ui.common.image.PurefinLogo
import hu.bbara.purefin.navigation.HOME_SEARCH_SHARED_BOUNDS_KEY
import hu.bbara.purefin.navigation.LocalNavSharedAnimatedVisibilityScope
import hu.bbara.purefin.navigation.LocalSharedTransitionScope
import hu.bbara.purefin.ui.theme.AppTheme

@OptIn(ExperimentalLayoutApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SearchFullScreen(
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavSharedAnimatedVisibilityScope.current
    val sharedBoundsModifier = if (
        sharedTransitionScope != null &&
        animatedVisibilityScope != null
    ) {
        with(sharedTransitionScope) {
            Modifier.sharedBounds(
                sharedContentState = rememberSharedContentState(
                    key = HOME_SEARCH_SHARED_BOUNDS_KEY
                ),
                animatedVisibilityScope = animatedVisibilityScope,
                enter = fadeIn(),
                exit = fadeOut(),
                resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds()
            )
        }
    } else {
        Modifier
    }

    Column(
        modifier = modifier
            .then(sharedBoundsModifier)
            .fillMaxSize()
            .background(scheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp)
    ) {
        SearchHeader(modifier = Modifier.padding(top = 12.dp))
        Spacer(modifier = Modifier.height(24.dp))
        SearchField()
        Spacer(modifier = Modifier.height(30.dp))
        SectionTitle(
            text = "Trending Searches",
            icon = {
                Icon(
                    imageVector = Icons.Outlined.LocalFireDepartment,
                    contentDescription = null,
                    tint = scheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            previewTrendingSearches.forEach { title ->
                TrendingSearchChip(title = title)
            }
        }
        Spacer(modifier = Modifier.height(34.dp))
        SectionTitle(text = "Browse Categories")
        Spacer(modifier = Modifier.height(18.dp))
        CategoryGrid(categories = previewCategories)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(
            onClick = {},
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, scheme.primary.copy(alpha = 0.52f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = scheme.primary),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = "View All Genres",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun SearchHeader(
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Outlined.Menu,
                contentDescription = "Menu",
                tint = scheme.primary,
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PurefinLogo(
                contentDescription = "PureFin",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(38.dp)
            )
            Text(
                text = "PureFin",
                color = scheme.primary,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Surface(
            shape = CircleShape,
            color = scheme.surfaceContainer,
            border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.28f)),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Profile",
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun SearchField(
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = scheme.surfaceContainer,
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.16f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 20.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Search movies, shows, genres...",
                style = MaterialTheme.typography.titleMedium,
                color = scheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .height(34.dp)
                    .width(1.dp)
                    .background(scheme.outlineVariant.copy(alpha = 0.18f))
            )
            Spacer(modifier = Modifier.width(14.dp))
            Icon(
                imageVector = Icons.Outlined.Tune,
                contentDescription = "Filter",
                tint = scheme.primary,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
private fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null
) {
    val scheme = MaterialTheme.colorScheme

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        icon?.invoke()
        Text(
            text = text,
            style = MaterialTheme.typography.headlineSmall,
            color = scheme.onBackground,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TrendingSearchChip(
    title: String,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = scheme.surfaceContainer,
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.14f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 11.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.TrendingUp,
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = scheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CategoryGrid(
    categories: List<SearchCategory>,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        categories.chunked(2).forEach { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowItems.forEach { category ->
                    CategoryCard(
                        category = category,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: SearchCategory,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(18.dp)

    Surface(
        shape = shape,
        color = scheme.surfaceContainer,
        modifier = modifier
            .clip(shape)
            .aspectRatio(1.45f)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            PurefinAsyncImage(
                model = category.imageUrl,
                contentDescription = category.name,
                fallbackIcon = Icons.Outlined.Movie,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.18f),
                                Color.Black.copy(alpha = 0.42f),
                                Color.Black.copy(alpha = 0.76f)
                            )
                        )
                    )
            )
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchFullScreenPreview() {
    AppTheme {
        SearchFullScreen(modifier = Modifier.fillMaxSize())
    }
}

private data class SearchCategory(
    val name: String,
    val imageUrl: String
)

private val previewTrendingSearches = listOf(
    "Dune",
    "Severance",
    "The Last of Us",
    "Blade Runner",
    "Foundation"
)

private val previewCategories = listOf(
    SearchCategory(
        name = "Action",
        imageUrl = "https://images.unsplash.com/photo-1535016120720-40c646be5580"
    ),
    SearchCategory(
        name = "Adventure",
        imageUrl = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee"
    ),
    SearchCategory(
        name = "Comedy",
        imageUrl = "https://images.unsplash.com/photo-1527224857830-43a7acc85260"
    ),
    SearchCategory(
        name = "Fantasy",
        imageUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af2176"
    ),
    SearchCategory(
        name = "Romance",
        imageUrl = "https://images.unsplash.com/photo-1516589178581-6cd7833ae3b2"
    ),
    SearchCategory(
        name = "Sci-Fi",
        imageUrl = "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa"
    )
)
