package hu.bbara.purefin.ui.screen.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.DrawerState
import androidx.tv.material3.DrawerValue
import androidx.tv.material3.Icon
import androidx.tv.material3.NavigationDrawer
import androidx.tv.material3.NavigationDrawerItem
import androidx.tv.material3.Text
import androidx.tv.material3.darkColorScheme
import androidx.tv.material3.rememberDrawerState
import hu.bbara.purefin.core.navigation.Route
import hu.bbara.purefin.tv.R
import androidx.tv.material3.MaterialTheme as TvMaterialTheme

internal const val TvDrawerItemTagPrefix = "tv-drawer-item-"
internal const val TvDrawerTitleTag = "tv-drawer-title"

@Composable
fun TvNavigationDrawer(
    destinations: List<TvDrawerDestinationItem>,
    selectedDestination: Route,
    onDestinationSelected: (Route) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ProvideTvDrawerTheme {
        val drawerState = rememberDrawerState(DrawerValue.Closed)

        NavigationDrawer(
            modifier = modifier.fillMaxSize(),
            drawerState = drawerState,
            drawerContent = {
                TvNavigationDrawerRail(
                    drawerState = drawerState,
                    destinations = destinations,
                    selectedDestination = selectedDestination,
                    onDestinationSelected = onDestinationSelected
                )
            },
            content = content
        )
    }
}

@Composable
private fun ProvideTvDrawerTheme(content: @Composable () -> Unit) {
    val scheme = MaterialTheme.colorScheme

    TvMaterialTheme(
        colorScheme = darkColorScheme(
            primary = scheme.primary,
            onPrimary = scheme.onPrimary,
            primaryContainer = scheme.primaryContainer,
            onPrimaryContainer = scheme.onPrimaryContainer,
            inversePrimary = scheme.inversePrimary,
            secondary = scheme.secondary,
            onSecondary = scheme.onSecondary,
            secondaryContainer = scheme.secondaryContainer,
            onSecondaryContainer = scheme.onSecondaryContainer,
            tertiary = scheme.tertiary,
            onTertiary = scheme.onTertiary,
            tertiaryContainer = scheme.tertiaryContainer,
            onTertiaryContainer = scheme.onTertiaryContainer,
            background = scheme.background,
            onBackground = scheme.onBackground,
            surface = scheme.surface,
            onSurface = scheme.onSurface,
            surfaceVariant = scheme.surfaceVariant,
            onSurfaceVariant = scheme.onSurfaceVariant,
            surfaceTint = scheme.surfaceTint,
            inverseSurface = scheme.inverseSurface,
            inverseOnSurface = scheme.inverseOnSurface,
            error = scheme.error,
            onError = scheme.onError,
            errorContainer = scheme.errorContainer,
            onErrorContainer = scheme.onErrorContainer,
            border = scheme.outline,
            borderVariant = scheme.outlineVariant,
            scrim = scheme.scrim
        ),
        content = content
    )
}

@Composable
fun TvDrawerHeader(
    expanded: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Image(
            painter = painterResource(id = R.mipmap.purefin_logo_foreground),
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )
        AnimatedVisibility(visible = expanded) {
            Text(
                text = "Purefin",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                modifier = Modifier.testTag(TvDrawerTitleTag)
            )
        }
    }
}

@Composable
private fun androidx.tv.material3.NavigationDrawerScope.TvNavigationDrawerRail(
    drawerState: DrawerState,
    destinations: List<TvDrawerDestinationItem>,
    selectedDestination: Route,
    onDestinationSelected: (Route) -> Unit,
) {
    val expanded = drawerState.currentValue == DrawerValue.Open
    val scheme = MaterialTheme.colorScheme
    var hasDrawerFocus by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(scheme.surface.copy(alpha = 0.96f))
            .padding(horizontal = 12.dp, vertical = 24.dp)
            .selectableGroup()
            .focusGroup()
            .onFocusChanged { state ->
                hasDrawerFocus = state.isFocused
            },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TvDrawerHeader(
            expanded = expanded,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
        )
        destinations.forEachIndexed { index, destination ->
            val isSelected = destination.destination == selectedDestination
            NavigationDrawerItem(
                selected = isSelected,
                onClick = {
                    onDestinationSelected(destination.destination)
                    if (isSelected) {
                        drawerState.setValue(DrawerValue.Closed)
                    }
                },
                modifier = Modifier
                    .testTag("$TvDrawerItemTagPrefix$index")
                    .semantics { selected = isSelected }
                    .focusProperties { canFocus = hasDrawerFocus || isSelected },
                leadingContent = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label
                    )
                }
            ) {
                Text(
                    text = destination.label,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}
