package hu.bbara.purefin.ui.screen.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun NumberSettingItem(
    title: String,
    value: Double,
    valueRange: ClosedFloatingPointRange<Double>,
    onValueChange: (Double) -> Unit,
    modifier: Modifier = Modifier.Companion
) {
    var sliderValue by remember(value) { mutableStateOf(value.toFloat()) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = String.Companion.format(Locale.US, "%.1f", sliderValue),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = { onValueChange(sliderValue.toDouble()) },
            valueRange = valueRange.start.toFloat()..valueRange.endInclusive.toFloat(),
            modifier = Modifier.Companion.fillMaxWidth()
        )
    }
}