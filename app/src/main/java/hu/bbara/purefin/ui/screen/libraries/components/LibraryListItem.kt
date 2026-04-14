package hu.bbara.purefin.ui.screen.libraries.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.ui.screen.home.components.HomeNavItem
import hu.bbara.purefin.ui.common.image.PurefinAsyncImage

@Composable
fun LibraryListItem(
    item: HomeNavItem,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        PurefinAsyncImage(
            model = item.posterUrl,
            contentDescription = item.label,
            contentScale = ContentScale.Inside,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(24.dp))
        )
        Text(
            text = item.label,
            style = MaterialTheme.typography.displaySmall
        )
    }
}
