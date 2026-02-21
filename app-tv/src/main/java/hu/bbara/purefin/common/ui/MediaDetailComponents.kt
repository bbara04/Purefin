package hu.bbara.purefin.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.bbara.purefin.common.ui.components.PurefinAsyncImage
import hu.bbara.purefin.core.model.CastMember

@Composable
fun MediaMetaChip(
    text: String,
    background: Color = MaterialTheme.colorScheme.surfaceVariant,
    border: Color = Color.Transparent,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(28.dp)
            .wrapContentHeight(Alignment.CenterVertically)
            .clip(RoundedCornerShape(6.dp))
            .background(background)
            .border(width = 1.dp, color = border, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MediaCastRow(
    cast: List<CastMember>,
    modifier: Modifier = Modifier,
    cardWidth: Dp = 96.dp,
    nameSize: TextUnit = 12.sp,
    roleSize: TextUnit = 10.sp
) {
    val scheme = MaterialTheme.colorScheme
    val mutedStrong = scheme.onSurfaceVariant.copy(alpha = 0.7f)

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(cast) { member ->
            Column(modifier = Modifier.width(cardWidth)) {
                Box(
                    modifier = Modifier
                        .aspectRatio(4f / 5f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(scheme.surfaceVariant)
                ) {
                    if (member.imageUrl == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(scheme.surfaceVariant.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = mutedStrong
                            )
                        }
                    } else {
                        PurefinAsyncImage(
                            model = member.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = member.name,
                    color = scheme.onBackground,
                    fontSize = nameSize,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = member.role,
                    color = mutedStrong,
                    fontSize = roleSize,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
