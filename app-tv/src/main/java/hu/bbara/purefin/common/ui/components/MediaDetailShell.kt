package hu.bbara.purefin.common.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.bbara.purefin.common.ui.MediaSynopsis

internal val MediaDetailHorizontalPadding = 48.dp

@Composable
internal fun TvMediaDetailScaffold(
    heroImageUrl: String,
    resetScrollKey: Any,
    modifier: Modifier = Modifier,
    heroHeightFraction: Float = 0.48f,
    topBar: @Composable BoxScope.() -> Unit,
    heroContent: @Composable ColumnScope.() -> Unit,
    bodyContent: LazyListScope.(Modifier) -> Unit = { _ -> }
) {
    val scheme = MaterialTheme.colorScheme
    val contentPadding = Modifier.padding(horizontal = MediaDetailHorizontalPadding)
    val listState = rememberLazyListState()

    LaunchedEffect(resetScrollKey) {
        listState.scrollToItem(0)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    MediaHero(
                        imageUrl = heroImageUrl,
                        backgroundColor = scheme.background,
                        heightFraction = heroHeightFraction,
                        modifier = Modifier.fillMaxWidth()
                    )
                    MediaHeroScrimOverlay()
                    Column(
                        modifier = contentPadding
                            .padding(top = 104.dp, bottom = 36.dp)
                    ) {
                        heroContent()
                    }
                }
            }
            bodyContent(contentPadding)
        }
        topBar()
    }
}

@Composable
internal fun MediaDetailSectionTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

@Composable
internal fun MediaDetailOverviewSection(
    synopsis: String,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    MediaSynopsis(
        synopsis = synopsis,
        modifier = modifier,
        title = "Overview",
        titleColor = scheme.onBackground,
        bodyColor = scheme.onSurfaceVariant.copy(alpha = 0.85f),
        titleFontSize = 22.sp,
        bodyFontSize = 16.sp,
        bodyLineHeight = 24.sp,
        titleSpacing = 14.dp,
        collapsedLines = 5,
        collapseInitially = false
    )
}

@Composable
internal fun MediaDetailPlaybackSection(
    audioTrack: String,
    subtitles: String,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Column(modifier = modifier) {
        MediaDetailSectionTitle(text = "Playback")
        Spacer(modifier = Modifier.height(14.dp))
        MediaPlaybackSettings(
            backgroundColor = scheme.surfaceContainerHigh,
            foregroundColor = scheme.onBackground,
            audioTrack = audioTrack,
            subtitles = subtitles
        )
    }
}
