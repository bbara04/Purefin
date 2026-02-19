package hu.bbara.purefin.common.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp

@Composable
fun MediaPlayButton(
    backgroundColor: Color,
    foregroundColor: Color,
    text: String = "Play",
    subText: String? = null,
    size: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(size)
            .clip(CircleShape)
            .background(backgroundColor)
            .padding(start = 16.dp, end = 32.dp)
            .clickable { onClick() },
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = "Play",
            tint = foregroundColor,
            modifier = Modifier.size(42.dp)
        )
        Column() {
            Text(
                text = text,
                color = foregroundColor,
                fontSize = TextUnit(
                    value = 16f,
                    type = TextUnitType.Sp
                )
            )
            subText?.let {
                Text(
                    text = subText,
                    color = foregroundColor.copy(alpha = 0.7f),
                    fontSize = TextUnit(
                        value = 14f,
                        type = TextUnitType.Sp
                    )
                )
            }
        }
    }
}