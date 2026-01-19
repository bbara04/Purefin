package hu.bbara.purefin.app.content.series

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.common.ui.PurefinWaitingScreen
import hu.bbara.purefin.navigation.ItemDto

@Composable
fun SeriesScreen(
    series: ItemDto,
    modifier: Modifier = Modifier,
    viewModel: SeriesViewModel = hiltViewModel()
) {
    LaunchedEffect(series.id) {
        viewModel.selectSeries(series.id)
    }

    val series = viewModel.series.collectAsState()

    if (series.value != null) {
        SeriesCard(
            series = series.value!!,
            modifier = modifier
        )
    } else {
        PurefinWaitingScreen()
    }
}
