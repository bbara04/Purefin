package hu.bbara.purefin.app.content.episode

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.bbara.purefin.common.ui.MediaMetaChip
import hu.bbara.purefin.common.ui.components.MediaDetailsTopBar
import hu.bbara.purefin.common.ui.components.MediaDetailsTopBarShortcut
import hu.bbara.purefin.common.ui.components.MediaResumeButton
import hu.bbara.purefin.core.data.navigation.Route
import hu.bbara.purefin.core.model.Episode

internal const val EpisodePlayButtonTag = "episode-play-button"

internal sealed interface EpisodeTopBarShortcut {
    val label: String
    val onClick: () -> Unit

    data class Series(override val onClick: () -> Unit) : EpisodeTopBarShortcut {
        override val label: String = "Series"
    }
}

internal fun episodeTopBarShortcut(
    previousRoute: Route?,
    onSeriesClick: () -> Unit
): EpisodeTopBarShortcut? {
    return when (previousRoute) {
        Route.Home -> EpisodeTopBarShortcut.Series(onClick = onSeriesClick)
        else -> null
    }
}

@Composable
internal fun EpisodeTopBar(
    onBack: () -> Unit,
    shortcut: EpisodeTopBarShortcut? = null,
    modifier: Modifier = Modifier,
    downFocusRequester: FocusRequester? = null
) {
    MediaDetailsTopBar(
        onBack = onBack,
        shortcut = shortcut?.let { MediaDetailsTopBarShortcut(label = it.label, onClick = it.onClick) },
        modifier = modifier,
        downFocusRequester = downFocusRequester
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun EpisodeHeroSection(
    episode: Episode,
    seriesTitle: String?,
    onPlay: () -> Unit,
    playFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val mutedStrong = scheme.onSurfaceVariant.copy(alpha = 0.82f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 760.dp)
    ) {
        if (!seriesTitle.isNullOrBlank()) {
            Text(
                text = seriesTitle,
                color = scheme.primary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
        Text(
            text = episode.title,
            color = scheme.onBackground,
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 48.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Episode ${episode.index}",
            color = mutedStrong,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(18.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MediaMetaChip(text = episode.releaseDate)
            MediaMetaChip(text = episode.rating)
            MediaMetaChip(text = episode.runtime)
            MediaMetaChip(
                text = episode.format,
                background = scheme.primary.copy(alpha = 0.2f),
                border = scheme.primary.copy(alpha = 0.35f),
                textColor = scheme.primary
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        MediaResumeButton(
            text = episode.playButtonText(),
            progress = episode.progress?.div(100)?.toFloat() ?: 0f,
            onClick = onPlay,
            modifier = Modifier
                .sizeIn(minWidth = 216.dp, maxWidth = 240.dp)
                .focusRequester(playFocusRequester)
                .testTag(EpisodePlayButtonTag)
        )
    }
}

private fun Episode.playButtonText(): String {
    return if ((progress ?: 0.0) > 0.0 && !watched) "Resume" else "Play"
}
