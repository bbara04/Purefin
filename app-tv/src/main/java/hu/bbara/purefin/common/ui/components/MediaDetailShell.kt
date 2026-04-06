package hu.bbara.purefin.common.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import hu.bbara.purefin.common.ui.MediaSynopsis

internal val MediaDetailHorizontalPadding = 48.dp
private val MediaDetailHeaderTopPadding = 104.dp
private val MediaDetailHeaderBottomPadding = 36.dp
private val MediaDetailCornerArtworkTopPadding = 40.dp
private val MediaDetailCornerArtworkShape = RoundedCornerShape(24.dp)
private val MediaDetailMinimumContentWidth = 280.dp
private val MediaDetailContentArtworkGap = 32.dp

@Composable
internal fun TvMediaDetailScaffold(
    artworkImageUrl: String,
    artworkWidth: Dp,
    artworkAspectRatio: Float,
    resetScrollKey: Any,
    modifier: Modifier = Modifier,
    headerHeightFraction: Float = 0.48f,
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
                TvMediaDetailHeader(
                    artworkImageUrl = artworkImageUrl,
                    artworkWidth = artworkWidth,
                    artworkAspectRatio = artworkAspectRatio,
                    headerHeightFraction = headerHeightFraction,
                    heroContent = heroContent
                )
            }
            bodyContent(contentPadding)
        }
    }
}

@Composable
private fun TvMediaDetailHeader(
    artworkImageUrl: String,
    artworkWidth: Dp,
    artworkAspectRatio: Float,
    headerHeightFraction: Float,
    heroContent: @Composable ColumnScope.() -> Unit
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val headerHeight = screenHeight * headerHeightFraction

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = headerHeight)
    ) {
        val contentMaxWidth = (
            maxWidth -
                (MediaDetailHorizontalPadding * 2) -
                artworkWidth -
                MediaDetailContentArtworkGap
            ).coerceAtLeast(MediaDetailMinimumContentWidth)

        Box(modifier = Modifier.fillMaxWidth()) {
            TvMediaDetailCornerArtwork(
                artworkImageUrl = artworkImageUrl,
                artworkWidth = artworkWidth,
                artworkAspectRatio = artworkAspectRatio,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(
                        top = MediaDetailCornerArtworkTopPadding,
                        end = MediaDetailHorizontalPadding
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(
                        start = MediaDetailHorizontalPadding,
                        top = MediaDetailHeaderTopPadding,
                        end = MediaDetailHorizontalPadding,
                        bottom = MediaDetailHeaderBottomPadding
                    )
                    .widthIn(max = contentMaxWidth)
            ) {
                heroContent()
            }
        }
    }
}

@Composable
private fun TvMediaDetailCornerArtwork(
    artworkImageUrl: String,
    artworkWidth: Dp,
    artworkAspectRatio: Float,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .width(artworkWidth)
            .aspectRatio(artworkAspectRatio)
            .clip(MediaDetailCornerArtworkShape)
            .background(scheme.surfaceVariant.copy(alpha = 0.28f))
    ) {
        PurefinAsyncImage(
            model = artworkImageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = 0.52f },
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colorStops = arrayOf(
                            0.0f to scheme.background.copy(alpha = 0.08f),
                            0.55f to scheme.background.copy(alpha = 0.2f),
                            1.0f to scheme.background.copy(alpha = 0.56f)
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to scheme.background.copy(alpha = 0.06f),
                            0.65f to scheme.background.copy(alpha = 0.22f),
                            1.0f to scheme.background.copy(alpha = 0.74f)
                        )
                    )
                )
        )
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
