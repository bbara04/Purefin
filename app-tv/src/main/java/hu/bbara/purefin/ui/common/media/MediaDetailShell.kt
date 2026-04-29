@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package hu.bbara.purefin.ui.common.media

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.bbara.purefin.image.ArtworkKind
import hu.bbara.purefin.image.ImageUrlBuilder
import hu.bbara.purefin.ui.common.image.PurefinAsyncImage

internal val MediaDetailHorizontalPadding = 48.dp
private val MediaDetailHeaderTopPadding = 24.dp
private val MediaDetailHeaderBottomPadding = 24.dp
private const val MediaDetailBodyImageWidthFraction = 0.66f

internal val TvMediaDetailBringIntoViewSpec: BringIntoViewSpec =
    object : BringIntoViewSpec {
        override fun calculateScrollDistance(
            offset: Float,
            size: Float,
            containerSize: Float,
        ): Float {
            val trailingEdge = offset + size

            return when {
                offset < 0f -> offset
                trailingEdge > containerSize -> trailingEdge - containerSize
                else -> 0f
            }
        }
    }

@Composable
internal fun TvMediaDetailScaffold(
    resetScrollKey: Any,
    modifier: Modifier = Modifier,
    bodyContent: @Composable (Modifier) -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme
    val contentPadding = Modifier.padding(horizontal = MediaDetailHorizontalPadding)

    CompositionLocalProvider(LocalBringIntoViewSpec provides TvMediaDetailBringIntoViewSpec) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(scheme.background)
        ) {
            bodyContent(contentPadding)
        }
    }
}

@Composable
internal fun TvMediaDetailBodyBox(
    backgroundImageUrl: String,
    modifier: Modifier = Modifier,
    heightFraction: Float = 0.48f,
    content: @Composable ColumnScope.() -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val bodyHeight = screenHeight * heightFraction

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = bodyHeight)
    ) {
        Box(modifier = Modifier.matchParentSize()) {
            PurefinAsyncImage(
                model = backgroundImageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(MediaDetailBodyImageWidthFraction)
                    .align(Alignment.TopEnd),
                contentScale = ContentScale.Crop
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.horizontalGradient(
                        colorStops = arrayOf(
                            0.0f to scheme.background,
                            0.28f to scheme.background.copy(alpha = 0.88f),
                            0.62f to scheme.background.copy(alpha = 0.42f),
                            1.0f to scheme.background.copy(alpha = 0.12f)
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to scheme.background.copy(alpha = 0.08f),
                            0.55f to scheme.background.copy(alpha = 0.16f),
                            1.0f to scheme.background
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = MediaDetailHeaderTopPadding,
                    bottom = MediaDetailHeaderBottomPadding
                )
        ) {
            content()
        }
    }
}

internal fun tvMediaDetailBackgroundImageUrl(imageUrlPrefix: String?): String {
    val primaryImageUrl = ImageUrlBuilder.finishImageUrl(imageUrlPrefix, ArtworkKind.PRIMARY)
    val backdropImageUrl = ImageUrlBuilder.finishImageUrl(imageUrlPrefix, ArtworkKind.BACKDROP)
    return backdropImageUrl.ifBlank { primaryImageUrl }
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
