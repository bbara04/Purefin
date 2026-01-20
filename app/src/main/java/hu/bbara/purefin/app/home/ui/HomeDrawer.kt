package hu.bbara.purefin.app.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.app.home.HomePageViewModel

@Composable
fun HomeDrawerContent(
    title: String,
    subtitle: String,
    colors: HomeColors,
    primaryNavItems: List<HomeNavItem>,
    secondaryNavItems: List<HomeNavItem>,
    user: HomeUser,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        HomeDrawerHeader(
            title = title,
            subtitle = subtitle,
            colors = colors
        )
        HomeDrawerNav(
            primaryItems = primaryNavItems,
            secondaryItems = secondaryNavItems,
            colors = colors,
        )
        Spacer(modifier = Modifier.weight(1f))
        HomeDrawerFooter(user = user, colors = colors)
    }
}

@Composable
fun HomeDrawerHeader(
    title: String,
    subtitle: String,
    colors: HomeColors,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 16.dp, top = 24.dp, bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .size(40.dp)
                .background(colors.primary, RoundedCornerShape(12.dp)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Play",
                tint = colors.onPrimary
            )
        }
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = title,
                color = colors.textPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = colors.textSecondary,
                fontSize = 12.sp
            )
        }
    }
    HorizontalDivider(color = colors.textSecondary.copy(alpha = 0.2f))
}

@Composable
fun HomeDrawerNav(
    primaryItems: List<HomeNavItem>,
    secondaryItems: List<HomeNavItem>,
    colors: HomeColors,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        primaryItems.forEach { item ->
            HomeDrawerNavItem(item = item, colors = colors)
        }
        if (secondaryItems.isNotEmpty()) {
            HorizontalDivider(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                color = colors.divider
            )
            secondaryItems.forEach { item ->
                HomeDrawerNavItem(item = item, colors = colors)
            }
        }
    }
}

@Composable
fun HomeDrawerNavItem(
    item: HomeNavItem,
    colors: HomeColors,
    modifier: Modifier = Modifier,
    viewModel: HomePageViewModel = hiltViewModel(),
) {
    val background = if (item.selected) colors.primary.copy(alpha = 0.12f) else Color.Transparent
    val tint = if (item.selected) colors.primary else colors.textSecondary
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .background(background, RoundedCornerShape(12.dp))
            .clickable { viewModel.onLibrarySelected(item) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = tint
        )
        Text(
            text = item.label,
            color = if (item.selected) colors.primary else colors.textPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Composable
fun HomeDrawerFooter (
    viewModel: HomePageViewModel = hiltViewModel(),
    user: HomeUser,
    colors: HomeColors,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(colors.drawerFooterBackground, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HomeAvatar(
            size = 32.dp,
            borderWidth = 1.dp,
            borderColor = colors.divider,
            backgroundColor = colors.avatarBackground,
            icon = Icons.Outlined.Person,
            iconTint = colors.textPrimary
        )
        Column(modifier = Modifier.padding(start = 12.dp)
            .clickable {viewModel.logout()}) {
            Text(
                text = user.name,
                color = colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = user.plan,
                color = colors.textSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
