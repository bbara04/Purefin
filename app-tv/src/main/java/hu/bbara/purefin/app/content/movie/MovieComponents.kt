package hu.bbara.purefin.app.content.movie

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
import hu.bbara.purefin.common.ui.components.MediaResumeButton
import hu.bbara.purefin.core.model.Movie

internal const val MoviePlayButtonTag = "movie-play-button"

@Composable
internal fun MovieTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    backFocusRequester: FocusRequester? = null,
    downFocusRequester: FocusRequester? = null
) {
    MediaDetailsTopBar(
        onBack = onBack,
        modifier = modifier,
        backFocusRequester = backFocusRequester,
        downFocusRequester = downFocusRequester
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MovieHeroSection(
    movie: Movie,
    onPlay: () -> Unit,
    playFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 760.dp)
    ) {
        Text(
            text = movie.title,
            color = scheme.onBackground,
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 48.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(18.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MediaMetaChip(text = movie.year)
            MediaMetaChip(text = movie.rating)
            MediaMetaChip(text = movie.runtime)
            MediaMetaChip(
                text = movie.format,
                background = scheme.primary.copy(alpha = 0.2f),
                border = scheme.primary.copy(alpha = 0.35f),
                textColor = scheme.primary
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        MediaResumeButton(
            text = movie.playButtonText(),
            progress = movie.progress?.div(100)?.toFloat() ?: 0f,
            onClick = onPlay,
            modifier = Modifier
                .sizeIn(minWidth = 216.dp, maxWidth = 240.dp)
                .focusRequester(playFocusRequester)
                .testTag(MoviePlayButtonTag)
        )
    }
}

private fun Movie.playButtonText(): String {
    return if ((progress ?: 0.0) > 0.0 && !watched) "Resume" else "Play"
}
