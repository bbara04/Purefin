package hu.bbara.purefin.ui.screen.home.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.bbara.purefin.core.model.MediaUiModel
import hu.bbara.purefin.ui.common.image.PurefinAsyncImage
import kotlin.math.roundToInt

internal const val TvHomeHeroTitleTag = "tv-home-hero-title"
internal const val TvHomeHeroProgressLabelTag = "tv-home-hero-progress-label"

private const val TvHomeHeroAnimationMillis = 180
// The billboard backdrop covers the top portion of the home screen so the hero
// text sits at the bottom of the backdrop and the content rows begin below it.
internal const val TvHomeHeroHeightFraction = 0.45f
internal const val TvHomeBackdropOffset = 0.20f

/**
 * The home screen background image plus the darkening gradients that keep the
 * hero text readable. Drawn behind the home content and sized to the top
 * [TvHomeHeroHeightFraction] of the screen.
 */
@Composable
internal fun TvHomeHeroBackdrop(
    backdropImageUrl: String?,
    modifier: Modifier = Modifier,
    heightFraction: Float = TvHomeHeroHeightFraction + TvHomeBackdropOffset,
) {
    val scheme = MaterialTheme.colorScheme
    Crossfade(
        targetState = backdropImageUrl,
        animationSpec = tween(durationMillis = TvHomeHeroAnimationMillis),
        label = "tv-home-hero-backdrop",
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(heightFraction)
            .drawWithContent {
                drawContent()
                // Darken the left side so the text block stays readable over any backdrop.
                drawRect(
                    Brush.horizontalGradient(
                        colorStops = arrayOf(
                            0.0f to scheme.background.copy(alpha = 0.86f),
                            0.34f to scheme.background.copy(alpha = 0.62f),
                            0.66f to scheme.background.copy(alpha = 0.22f),
                            1.0f to scheme.background.copy(alpha = 0.04f)
                        )
                    )
                )
                // Strong bottom fade so the title and metadata are legible.
                drawRect(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to scheme.background.copy(alpha = 0f),
                            0.40f to scheme.background.copy(alpha = 0.10f),
                            0.72f to scheme.background.copy(alpha = 0.58f),
                            1.0f to scheme.background
                        )
                    )
                )
            }
    ) { imageUrl ->
        PurefinAsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
internal fun TvFocusedItemHero(
    item: MediaUiModel,
    modifier: Modifier = Modifier,
    heightFraction: Float = TvHomeHeroHeightFraction,
) {
    val scheme = MaterialTheme.colorScheme
    Crossfade(
        targetState = item,
        animationSpec = tween(durationMillis = TvHomeHeroAnimationMillis),
        label = "tv-home-hero-content",
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(heightFraction)
    ) { hero ->
        Column(
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp, vertical = 16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.widthIn(max = 720.dp)
            ) {
                Text(
                    text = hero.primaryText,
                    color = scheme.onBackground,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 44.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag(TvHomeHeroTitleTag)
                )
                TvHomeHeroMetadataRow(item = hero)
                if (hero.description.isNotBlank()) {
                    Text(
                        text = hero.description,
                        color = scheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun TvHomeHeroMetadataRow(item: MediaUiModel) {
    val hasSecondary = item.secondaryText.isNotBlank()
    val progress = item.progress
    val hasProgress = progress != null && progress > 0f && progress < 1f
    if (!hasSecondary && !hasProgress) return
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (hasSecondary) {
            TvHomeMetaChip(text = item.secondaryText)
        }
        if (hasProgress) {
            val percent = ((progress ?: 0f) * 100f).roundToInt()
            TvHomeMetaChip(
                text = "Resume \u00B7 $percent%",
                highlighted = true,
                modifier = Modifier.testTag(TvHomeHeroProgressLabelTag)
            )
        }
    }
}
