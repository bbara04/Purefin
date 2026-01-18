package hu.bbara.purefin.app.content.series

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import hu.bbara.purefin.navigation.ItemDto

@Composable
fun SeriesScreen(
    series: ItemDto,
    modifier: Modifier = Modifier
) {
    SeriesCard(
        series = series,
        modifier = modifier
    )
}
