package hu.bbara.purefin.ui.common.media

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.ui.common.image.PurefinAsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDetailScaffold(
    imageUrl: String,
    modifier: Modifier = Modifier,
    imageHeight: Dp = 320.dp,
    contentOverlap: Dp = 24.dp,
    contentSpacing: Dp = 16.dp,
    topBar: @Composable (scrollBehavior: TopAppBarScrollBehavior) -> Unit = {},
    heroContent: @Composable ColumnScope.(Modifier) -> Unit,
    content: @Composable ColumnScope.(Modifier) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val isHomeMediaTransitionActive = isHomeMediaSharedBoundsTransitionActive()
    val topBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
        containerColor = scheme.background,
        topBar = {
            if (!isHomeMediaTransitionActive) {
                topBar(topBarScrollBehavior)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = innerPadding.calculateBottomPadding()),
            verticalArrangement = Arrangement.spacedBy(contentSpacing)
        ) {
            Box(
                modifier = Modifier
                    .homeMediaSharedBoundsDestination()
                    .fillMaxWidth()
                    .height(imageHeight)
            ) {
                PurefinAsyncImage(
                    model = imageUrl,
                    contentDescription = null,
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
                                    scheme.background.copy(alpha = 0.5f),
                                    scheme.background
                                )
                            )
                        )
                )
            }
            if (!isHomeMediaTransitionActive) {
                heroContent(Modifier.padding(horizontal = 16.dp))
                content(Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}
