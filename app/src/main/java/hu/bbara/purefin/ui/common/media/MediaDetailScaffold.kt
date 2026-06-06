package hu.bbara.purefin.ui.common.media

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.bbara.purefin.core.download.DownloadState
import hu.bbara.purefin.model.CastMember
import hu.bbara.purefin.ui.common.button.DownloadActionButton
import hu.bbara.purefin.ui.common.button.MediaActionButton
import hu.bbara.purefin.ui.common.button.MediaResumeButton
import hu.bbara.purefin.ui.common.image.PurefinAsyncImage

data class MediaDetailScaffoldUiModel(
    val imageUrl: String,
    val title: String,
    val imageContentDescription: String? = null,
    val titleFontSize: TextUnit = 32.sp,
    val titleLineHeight: TextUnit = 38.sp,
    val subtitle: String? = null,
    val subtitleFontSize: TextUnit = 14.sp,
    val subtitleFontWeight: FontWeight = FontWeight.Medium,
    val metadataItems: List<String> = emptyList(),
    val highlightedMetadataItem: String? = null,
    val actions: MediaDetailActionsUiModel? = null,
    val synopsis: MediaDetailSynopsisUiModel? = null,
    val playbackSettings: MediaDetailPlaybackSettingsUiModel? = null,
    val cast: MediaDetailCastUiModel? = null,
)

data class MediaDetailActionsUiModel(
    val primaryAction: MediaDetailPrimaryActionUiModel,
    val secondaryActions: List<MediaDetailSecondaryActionUiModel> = emptyList(),
    val dividerThickness: Dp? = null,
)

data class MediaDetailPrimaryActionUiModel(
    val text: String,
    val progress: Float,
    val onClick: () -> Unit,
    val testTag: String? = null,
)

sealed interface MediaDetailSecondaryActionUiModel {
    val testTag: String?

    data class Download(
        val downloadState: DownloadState,
        val onClick: () -> Unit,
        override val testTag: String? = null,
    ) : MediaDetailSecondaryActionUiModel

    data class Icon(
        val icon: ImageVector,
        val onClick: () -> Unit = {},
        override val testTag: String? = null,
    ) : MediaDetailSecondaryActionUiModel
}

data class MediaDetailSynopsisUiModel(
    val text: String,
    val bodyColor: Color? = null,
    val bodyFontSize: TextUnit = 15.sp,
    val bodyLineHeight: TextUnit? = 22.sp,
    val titleSpacing: Dp = 12.dp,
)

data class MediaDetailPlaybackSettingsUiModel(
    val audioTrack: String,
    val subtitles: String,
)

data class MediaDetailCastUiModel(
    val members: List<CastMember>,
    val title: String = "Cast",
    val topSpacing: Dp = 0.dp,
    val cardWidth: Dp = 96.dp,
    val nameFontSize: TextUnit = 12.sp,
    val roleFontSize: TextUnit = 10.sp,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDetailScaffold(
    uiModel: MediaDetailScaffoldUiModel,
    modifier: Modifier = Modifier,
    imageHeight: Dp = 320.dp,
    contentOverlap: Dp = 24.dp,
    contentSpacing: Dp = 16.dp,
    topBar: @Composable (scrollBehavior: TopAppBarScrollBehavior) -> Unit = {},
    content: @Composable ColumnScope.(Modifier) -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme
    val isHomeMediaTransitionActive = isHomeMediaSharedBoundsTransitionActive()
    val topBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier
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
                    model = uiModel.imageUrl,
                    contentDescription = uiModel.imageContentDescription,
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
                val contentModifier = Modifier.padding(horizontal = 16.dp)
                MediaDetailScaffoldContent(
                    uiModel = uiModel,
                    modifier = contentModifier
                )
                content(contentModifier)
            }
        }
    }
}

@Composable
private fun ColumnScope.MediaDetailScaffoldContent(
    uiModel: MediaDetailScaffoldUiModel,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme

    Text(
        text = uiModel.title,
        color = scheme.onBackground,
        fontSize = uiModel.titleFontSize,
        fontWeight = FontWeight.Bold,
        lineHeight = uiModel.titleLineHeight,
        modifier = modifier
    )
    uiModel.subtitle?.takeIf { it.isNotBlank() }?.let { subtitle ->
        Text(
            text = subtitle,
            color = scheme.onBackground,
            fontSize = uiModel.subtitleFontSize,
            fontWeight = uiModel.subtitleFontWeight,
            modifier = modifier
        )
    }
    if (uiModel.metadataItems.any { it.isNotBlank() }) {
        MediaMetadataFlowRow(
            items = uiModel.metadataItems,
            highlightedItem = uiModel.highlightedMetadataItem,
            modifier = modifier
        )
    }
    uiModel.actions?.let { actions ->
        MediaDetailActions(
            uiModel = actions,
            modifier = modifier
        )
    }
    uiModel.synopsis?.takeIf { it.text.isNotBlank() }?.let { synopsis ->
        MediaSynopsis(
            synopsis = synopsis.text,
            bodyColor = synopsis.bodyColor ?: scheme.onSurfaceVariant,
            bodyFontSize = synopsis.bodyFontSize,
            bodyLineHeight = synopsis.bodyLineHeight,
            titleSpacing = synopsis.titleSpacing,
            modifier = modifier
        )
    }
    uiModel.playbackSettings?.let { playbackSettings ->
        MediaPlaybackSettings(
            backgroundColor = scheme.surface,
            foregroundColor = scheme.onSurface,
            audioTrack = playbackSettings.audioTrack,
            subtitles = playbackSettings.subtitles,
            modifier = modifier
        )
    }
    uiModel.cast?.takeIf { it.members.isNotEmpty() }?.let { cast ->
        if (cast.topSpacing > 0.dp) {
            Spacer(modifier = Modifier.height(cast.topSpacing))
        }
        Text(
            text = cast.title,
            color = scheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = modifier
        )
        Spacer(modifier = modifier.height(12.dp))
        MediaDetailCastRow(
            cast = cast,
            modifier = modifier
        )
    }
}

@Composable
private fun MediaDetailCastRow(
    cast: MediaDetailCastUiModel,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val mutedStrong = scheme.onSurfaceVariant.copy(alpha = 0.7f)

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(cast.members) { member ->
            Column(modifier = Modifier.width(cast.cardWidth)) {
                PurefinAsyncImage(
                    model = member.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .aspectRatio(4f / 5f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(scheme.surfaceVariant),
                    contentScale = ContentScale.Crop,
                    fallbackIcon = Icons.Outlined.Person
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = member.name,
                    color = scheme.onBackground,
                    fontSize = cast.nameFontSize,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = member.role,
                    color = mutedStrong,
                    fontSize = cast.roleFontSize,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MediaDetailActions(
    uiModel: MediaDetailActionsUiModel,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        MediaResumeButton(
            text = uiModel.primaryAction.text,
            progress = uiModel.primaryAction.progress,
            onClick = uiModel.primaryAction.onClick,
            modifier = Modifier
                .sizeIn(maxWidth = 200.dp)
                .optionalTestTag(uiModel.primaryAction.testTag)
        )
        uiModel.dividerThickness?.let { dividerThickness ->
            VerticalDivider(
                color = MaterialTheme.colorScheme.secondary,
                thickness = dividerThickness,
                modifier = Modifier
                    .height(48.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        } ?: Spacer(modifier = Modifier.width(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            uiModel.secondaryActions.forEach { action ->
                when (action) {
                    is MediaDetailSecondaryActionUiModel.Download -> {
                        DownloadActionButton(
                            downloadState = action.downloadState,
                            onClick = action.onClick,
                            modifier = Modifier.optionalTestTag(action.testTag)
                        )
                    }

                    is MediaDetailSecondaryActionUiModel.Icon -> {
                        MediaActionButton(
                            backgroundColor = MaterialTheme.colorScheme.surface,
                            iconColor = MaterialTheme.colorScheme.onSurface,
                            icon = action.icon,
                            height = 48.dp,
                            onClick = action.onClick,
                            modifier = Modifier.optionalTestTag(action.testTag)
                        )
                    }
                }
            }
        }
    }
}

private fun Modifier.optionalTestTag(testTag: String?): Modifier =
    if (testTag == null) this else then(Modifier.testTag(testTag))
