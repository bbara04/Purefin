package hu.bbara.purefin.app.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.app.home.HomePageViewModel

@Composable
fun HomeTopBar(
    viewModel: HomePageViewModel = hiltViewModel(),
    title: String,
    colors: HomeColors,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {
        HomeAvatar(
            size = 36.dp,
            borderWidth = 2.dp,
            borderColor = colors.avatarBorder,
            backgroundColor = colors.avatarBackground,
            icon = Icons.Outlined.Person,
            iconTint = colors.onPrimary
        )
    }
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background.copy(alpha = 0.95f))
            .zIndex(1f)
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Outlined.Menu,
                        contentDescription = "Menu",
                        tint = colors.textPrimary
                    )
                }
                Text(
                    text = title,
                    color = colors.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(onClick = { viewModel.loadHomePageData() }) {
                    Icon(imageVector = Icons.Outlined.Refresh, contentDescription = "Refresh")
                }
            }
        }
    }
}
