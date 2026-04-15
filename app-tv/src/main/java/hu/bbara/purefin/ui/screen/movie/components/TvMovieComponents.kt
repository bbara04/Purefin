package hu.bbara.purefin.ui.screen.movie.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import hu.bbara.purefin.ui.common.button.MediaResumeButton
import hu.bbara.purefin.ui.common.media.MediaMetadataFlowRow
import hu.bbara.purefin.ui.common.media.mediaPlaybackProgress
import hu.bbara.purefin.ui.common.media.mediaPlayButtonText
import hu.bbara.purefin.core.model.Movie

internal const val MoviePlayButtonTag = "movie-play-button"

@Composable
internal fun TvMovieHeroSection(
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
        MediaMetadataFlowRow(
            items = listOf(movie.year, movie.rating, movie.runtime, movie.format),
            highlightedItem = movie.format
        )
        Spacer(modifier = Modifier.height(24.dp))
        MediaResumeButton(
            text = mediaPlayButtonText(movie.progress, movie.watched),
            progress = mediaPlaybackProgress(movie.progress),
            onClick = onPlay,
            modifier = Modifier
                .sizeIn(minWidth = 216.dp, maxWidth = 240.dp)
                .focusRequester(playFocusRequester)
                .testTag(MoviePlayButtonTag),
            focusedScale = 1.08f,
            focusHaloColor = scheme.primary.copy(alpha = 0.22f),
            focusBorderWidth = 3.dp,
            focusBorderColor = scheme.onBackground,
            overlayBorderWidth = 2.dp,
            overlayBorderColor = scheme.primary.copy(alpha = 0.95f),
            focusable = true
        )
    }
}
