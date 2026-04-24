package hu.bbara.purefin.ui.screen.home.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.bbara.purefin.ui.model.MediaUiModel
import hu.bbara.purefin.ui.common.image.PurefinAsyncImage

internal const val TvHomeHeroTitleTag = "tv-home-hero-title"
internal const val TvHomeHeroProgressLabelTag = "tv-home-hero-progress-label"

private const val TvHomeHeroAnimationMillis = 180

@Composable
internal fun TvFocusedItemHero(
    item: MediaUiModel,
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
                        text = hero.primaryText,
                        color = scheme.onBackground,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 38.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag(TvHomeHeroTitleTag)
                    )
                    Text(
                        text = hero.description,
                        color = scheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
