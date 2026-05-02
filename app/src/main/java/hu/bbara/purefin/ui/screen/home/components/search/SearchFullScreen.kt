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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.bbara.purefin.feature.search.SearchResult
import hu.bbara.purefin.feature.search.SearchViewModel
import hu.bbara.purefin.model.Genre
import hu.bbara.purefin.model.MediaKind
import hu.bbara.purefin.navigation.HOME_SEARCH_SHARED_BOUNDS_KEY
import hu.bbara.purefin.navigation.LocalNavSharedAnimatedVisibilityScope
import hu.bbara.purefin.navigation.LocalSharedTransitionScope
import hu.bbara.purefin.ui.common.image.PurefinLogo
import hu.bbara.purefin.ui.theme.AppTheme
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SearchFullScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavSharedAnimatedVisibilityScope.current
    val searchResults by viewModel.searchResult.collectAsStateWithLifecycle()
    val genres by viewModel.genres.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var selectedGenreName by rememberSaveable { mutableStateOf<String?>(null) }
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

    SearchFullScreenContent(
        query = query,
        searchResults = searchResults,
        genres = genres.sortedBy { it.name },
        selectedGenreName = selectedGenreName,
        onQueryChange = {
            query = it
            viewModel.search(it)
        },
        onSearch = { viewModel.search(query) },
        onResultClick = viewModel::onSearchResultSelected,
        onGenreSelected = { genre ->
            selectedGenreName = genre.name.takeIf { it != selectedGenreName }
            viewModel.setSelectedGenre(selectedGenreName)
        },
        modifier = modifier.then(sharedBoundsModifier)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchFullScreenContent(
    query: String,
    searchResults: List<SearchResult>,
    genres: List<Genre>,
    selectedGenreName: String?,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onResultClick: (SearchResult) -> Unit,
    onGenreSelected: (Genre) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp)
    ) {
        SearchHeader(modifier = Modifier.padding(top = 12.dp))
        Spacer(modifier = Modifier.height(24.dp))
        SearchField(
            query = query,
            onQueryChange = onQueryChange,
            onSearch = onSearch
        )
        Spacer(modifier = Modifier.height(30.dp))
        if (query.isBlank()) {
            SectionTitle(text = "Browse Genres")
            Spacer(modifier = Modifier.height(18.dp))
            GenreChips(
                genres = genres,
                selectedGenreName = selectedGenreName,
                onGenreSelected = onGenreSelected
            )
            if (selectedGenreName != null) {
                Spacer(modifier = Modifier.height(30.dp))
                SectionTitle(text = "Search Results")
                Spacer(modifier = Modifier.height(16.dp))
                SearchResults(
                    searchResults = searchResults,
                    onResultClick = onResultClick
                )
            }
        } else {
            SectionTitle(text = "Search Results")
            Spacer(modifier = Modifier.height(16.dp))
            SearchResults(
                searchResults = searchResults,
                onResultClick = onResultClick
            )
        }
    }
}

@Composable
private fun SearchResults(
    searchResults: List<SearchResult>,
    onResultClick: (SearchResult) -> Unit,
    modifier: Modifier = Modifier
) {
    if (searchResults.isEmpty()) {
        SearchMessage(
            title = "No matches",
            body = "Try a different title or browse your libraries.",
            modifier = modifier
        )
    } else {
        SearchResultsGrid(
            results = searchResults,
            onResultClick = onResultClick,
            modifier = modifier
        )
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
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val keyboardController = LocalSoftwareKeyboardController.current

    TextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.titleMedium,
        placeholder = {
            Text(
                text = "Search movies, shows, genres...",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.size(30.dp)
            )
        },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Clear search",
                        tint = scheme.onSurfaceVariant
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Outlined.Tune,
                    contentDescription = "Filter",
                    tint = scheme.primary,
                    modifier = Modifier.size(30.dp)
                )
            }
        },
        shape = RoundedCornerShape(24.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = scheme.surfaceContainer,
            unfocusedContainerColor = scheme.surfaceContainer,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearch()
                keyboardController?.hide()
            }
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
    )
}

@Composable
private fun SearchResultsGrid(
    results: List<SearchResult>,
    onResultClick: (SearchResult) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        results.chunked(2).forEach { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowItems.forEach { item ->
                    SearchResultCard(
                        item = item,
                        onClick = { onResultClick(item) },
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GenreChips(
    genres: List<Genre>,
    selectedGenreName: String?,
    onGenreSelected: (Genre) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        genres.forEach { genre ->
            FilterChip(
                selected = genre.name == selectedGenreName,
                onClick = { onGenreSelected(genre) },
                label = {
                    Text(
                        text = genre.name,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchFullScreenPreview() {
    var query by rememberSaveable { mutableStateOf("du") }

    AppTheme {
        SearchFullScreenContent(
            query = query,
            searchResults = previewSearchResults,
            genres = previewGenres,
            selectedGenreName = "Action",
            onQueryChange = { query = it },
            onSearch = {},
            onResultClick = {},
            onGenreSelected = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

private val previewGenres = listOf(
    Genre(name = "Action"),
    Genre(name = "Adventure"),
    Genre(name = "Comedy"),
    Genre(name = "Fantasy"),
    Genre(name = "Romance"),
    Genre(name = "Sci-Fi")
)

private val previewSearchResults = listOf(
    SearchResult(
        id = UUID.fromString("11111111-1111-1111-1111-111111111111"),
        title = "Dune",
        posterUrl = "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa",
        type = MediaKind.MOVIE
    ),
    SearchResult(
        id = UUID.fromString("22222222-2222-2222-2222-222222222222"),
        title = "Dune: Part Two",
        posterUrl = "https://images.unsplash.com/photo-1535016120720-40c646be5580",
        type = MediaKind.MOVIE
    )
)
