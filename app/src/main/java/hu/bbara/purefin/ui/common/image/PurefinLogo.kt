package hu.bbara.purefin.ui.common.image

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import hu.bbara.purefin.R

@Composable
fun PurefinLogo(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF7C42F0)
) {
    Icon(
        painter = painterResource(id = R.drawable.purefin_logo_tintable),
        contentDescription = "Purefin logo",
        modifier = modifier,
        tint = color
    )
}
