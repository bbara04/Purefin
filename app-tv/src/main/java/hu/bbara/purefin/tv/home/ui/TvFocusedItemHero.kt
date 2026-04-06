package hu.bbara.purefin.tv.home.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.bbara.purefin.common.ui.components.MediaProgressBar
import hu.bbara.purefin.common.ui.components.PurefinAsyncImage

internal const val TvHomeHeroTitleTag = "tv-home-hero-title"
internal const val TvHomeHeroStatusTag = "tv-home-hero-status"
internal const val TvHomeHeroProgressLabelTag = "tv-home-hero-progress-label"

private const val TvHomeHeroAnimationMillis = 180

@Composable
internal fun TvFocusedItemHero(
    item: TvFocusedHeroModel,
    height: Dp,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(scheme.background)
    ) {
        Crossfade(
            targetState = item.backdropImageUrl,
            animationSpec = tween(durationMillis = TvHomeHeroAnimationMillis),
            label = "tv-home-hero-background"
        ) { imageUrl ->
            PurefinAsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colorStops = arrayOf(
                            0.0f to scheme.background,
                            0.28f to scheme.background.copy(alpha = 0.88f),
                            0.62f to scheme.background.copy(alpha = 0.42f),
                            1.0f to scheme.background.copy(alpha = 0.06f)
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
                            0.0f to scheme.background.copy(alpha = 0f),
                            0.56f to scheme.background.copy(alpha = 0.1f),
                            1.0f to scheme.background
                        )
                    )
                )
        )
        Crossfade(
            targetState = item,
            animationSpec = tween(durationMillis = TvHomeHeroAnimationMillis),
            label = "tv-home-hero-content"
        ) { hero ->
            Column(
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 40.dp, vertical = 18.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.widthIn(max = 720.dp)
                ) {
                    Text(
                        text = hero.eyebrowText.uppercase(),
                        color = scheme.primary,
                        fontSize = 10.sp,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = hero.title,
                        color = scheme.onBackground,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 38.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag(TvHomeHeroTitleTag)
                    )
                    hero.metadataText?.let { metadataText ->
                        Text(
                            text = metadataText,
                            color = scheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (hero.watchedText != null || hero.progressFraction != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(22.dp))
                                .background(scheme.surfaceContainerHigh.copy(alpha = 0.92f))
                                .border(1.dp, scheme.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            hero.watchedText?.let { watchedText ->
                                TvHomeMetaChip(
                                    text = watchedText,
                                    highlighted = true,
                                    modifier = Modifier.testTag(TvHomeHeroStatusTag)
                                )
                            }
                            if (hero.progressFraction != null && hero.progressLabel != null) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.width(188.dp)
                                ) {
                                    Text(
                                        text = "Progress",
                                        color = scheme.onSurfaceVariant.copy(alpha = 0.85f),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    MediaProgressBar(
                                        progress = hero.progressFraction,
                                        foregroundColor = scheme.primary,
                                        backgroundColor = scheme.surfaceVariant,
                                        contentPadding = PaddingValues(0.dp),
                                        barHeight = 6.dp,
                                        modifier = Modifier
                                    )
                                }
                                Text(
                                    text = hero.progressLabel,
                                    color = scheme.onBackground,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.testTag(TvHomeHeroProgressLabelTag)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
