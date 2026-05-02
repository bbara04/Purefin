package hu.bbara.purefin.ui.screen.home.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.bbara.purefin.navigation.HOME_SEARCH_SHARED_BOUNDS_KEY
import hu.bbara.purefin.navigation.LocalNavSharedAnimatedVisibilityScope
import hu.bbara.purefin.navigation.LocalSharedTransitionScope
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
                    modifier = Modifier.size(48.dp),
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun DefaultTopBarSearchButton(
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavSharedAnimatedVisibilityScope.current

    val searchButtonModifier = if (
        sharedTransitionScope != null &&
        animatedVisibilityScope != null
    ) {
        with(sharedTransitionScope) {
            Modifier
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(
                        key = HOME_SEARCH_SHARED_BOUNDS_KEY
                    ),
                    animatedVisibilityScope = animatedVisibilityScope,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds()
                )
                .size(50.dp)
        }
    } else {
        Modifier.size(50.dp)
    }

    IconButton(
        onClick = onClick,
        colors = IconButtonColors(
            containerColor = scheme.surface,
            contentColor = scheme.onSurface,
            disabledContainerColor = scheme.surface,
            disabledContentColor = scheme.onSurface
        ),
        modifier = searchButtonModifier,
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = "Search",
            modifier = Modifier.size(30.dp),
        )
    }
}
