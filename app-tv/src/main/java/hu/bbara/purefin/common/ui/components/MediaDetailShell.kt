package hu.bbara.purefin.common.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.bbara.purefin.common.ui.MediaSynopsis
import hu.bbara.purefin.core.data.image.JellyfinImageHelper
import org.jellyfin.sdk.model.api.ImageType

internal val MediaDetailHorizontalPadding = 48.dp
private val MediaDetailHeaderTopPadding = 104.dp
private val MediaDetailHeaderBottomPadding = 36.dp

@Composable
internal fun TvMediaDetailScaffold(
    backgroundImageUrl: String,
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

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
    ) {
        item {
            TvMediaDetailHeader(
                backgroundImageUrl = backgroundImageUrl,
                headerHeightFraction = headerHeightFraction,
                heroContent = heroContent
            )
        }
        bodyContent(contentPadding)
    }
}

@Composable
private fun TvMediaDetailHeader(
    backgroundImageUrl: String,
    headerHeightFraction: Float,
    heroContent: @Composable ColumnScope.() -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val headerHeight = screenHeight * headerHeightFraction

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = headerHeight)
    ) {
        PurefinAsyncImage(
            model = backgroundImageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
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
                .fillMaxSize()
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
                    start = MediaDetailHorizontalPadding,
                    top = MediaDetailHeaderTopPadding,
                    end = MediaDetailHorizontalPadding,
                    bottom = MediaDetailHeaderBottomPadding
                )
        ) {
            heroContent()
        }
    }
}

internal fun tvMediaDetailBackgroundImageUrl(imageUrlPrefix: String?): String {
    val primaryImageUrl = JellyfinImageHelper.finishImageUrl(imageUrlPrefix, ImageType.PRIMARY)
    val backdropImageUrl = JellyfinImageHelper.finishImageUrl(imageUrlPrefix, ImageType.BACKDROP)
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
