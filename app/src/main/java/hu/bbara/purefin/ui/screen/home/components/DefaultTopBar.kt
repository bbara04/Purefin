package hu.bbara.purefin.ui.screen.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.bbara.purefin.ui.common.image.PurefinLogo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultTopBar(
    actions: @Composable RowScope.() -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    TopAppBar(
        title = {
            Row(
                modifier = Modifier,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PurefinLogo(
                    contentDescription = "Purefin",
                    modifier = Modifier.size(48.dp),
                    contentScale = ContentScale.Fit,
                )
                Text(
                    text = "PureFin",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    color = scheme.onSecondary
                )
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = scheme.background,
            navigationIconContentColor = scheme.onSurface,
            actionIconContentColor = scheme.onSurface,
            titleContentColor = scheme.onSurface
        ),
        modifier = Modifier.padding(end = 12.dp)
    )
}
