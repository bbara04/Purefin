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
import androidx.compose.material3.MaterialTheme
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
    primaryNavItems: List<HomeNavItem>,
    secondaryNavItems: List<HomeNavItem>,
    user: HomeUser,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        HomeDrawerHeader(
            title = title,
            subtitle = subtitle
        )
        HomeDrawerNav(
            primaryItems = primaryNavItems,
            secondaryItems = secondaryNavItems
        )
        Spacer(modifier = Modifier.weight(1f))
        HomeDrawerFooter(user = user)
    }
}

@Composable
fun HomeDrawerHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 16.dp, top = 24.dp, bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .size(40.dp)
                .background(scheme.primary, RoundedCornerShape(12.dp)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Play",
                tint = scheme.onPrimary
            )
        }
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = title,
                color = scheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = scheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
    }
    HorizontalDivider(color = scheme.onSurfaceVariant.copy(alpha = 0.2f))
}

@Composable
fun HomeDrawerNav(
    primaryItems: List<HomeNavItem>,
    secondaryItems: List<HomeNavItem>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        primaryItems.forEach { item ->
            HomeDrawerNavItem(item = item)
        }
        if (secondaryItems.isNotEmpty()) {
            HorizontalDivider(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            secondaryItems.forEach { item ->
                HomeDrawerNavItem(item = item)
            }
        }
    }
}

@Composable
fun HomeDrawerNavItem(
    item: HomeNavItem,
    modifier: Modifier = Modifier,
    viewModel: HomePageViewModel = hiltViewModel(),
) {
    val scheme = MaterialTheme.colorScheme
    val background = if (item.selected) scheme.primary.copy(alpha = 0.12f) else Color.Transparent
    val tint = if (item.selected) scheme.primary else scheme.onSurfaceVariant
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
            color = if (item.selected) scheme.primary else scheme.onBackground,
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
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(scheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HomeAvatar(
            size = 32.dp,
            borderWidth = 1.dp,
            borderColor = scheme.outlineVariant,
            backgroundColor = scheme.primaryContainer,
            icon = Icons.Outlined.Person,
            iconTint = scheme.onBackground
        )
        Column(modifier = Modifier.padding(start = 12.dp)
            .clickable {viewModel.logout()}) {
            Text(
                text = user.name,
                color = scheme.onBackground,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = user.plan,
                color = scheme.onSurfaceVariant,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
