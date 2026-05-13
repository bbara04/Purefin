package hu.bbara.purefin.ui.screen.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme as TvMaterialTheme
import androidx.tv.material3.Text as TvText
import androidx.tv.material3.darkColorScheme
import hu.bbara.purefin.ui.common.image.PurefinLogo
import hu.bbara.purefin.ui.screen.waiting.PurefinWaitingScreen

@Composable
fun TvLoginContent(
    state: LoginContentState,
    callbacks: LoginContentCallbacks,
    modifier: Modifier = Modifier
) {
    if (state.isLoggingIn) {
        PurefinWaitingScreen(modifier = modifier)
        return
    }

    ProvideTvLoginTheme {
        TvLoginForm(
            state = state,
            callbacks = callbacks,
            modifier = modifier
        )
    }
}

@Composable
private fun ProvideTvLoginTheme(content: @Composable () -> Unit) {
    val scheme = MaterialTheme.colorScheme

    TvMaterialTheme(
        colorScheme = darkColorScheme(
            primary = scheme.primary,
            onPrimary = scheme.onPrimary,
            primaryContainer = scheme.primaryContainer,
            onPrimaryContainer = scheme.onPrimaryContainer,
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
private fun TvLoginForm(
    state: LoginContentState,
    callbacks: LoginContentCallbacks,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val serverFocusRequester = remember { FocusRequester() }
    val findFocusRequester = remember { FocusRequester() }
    val changeServerFocusRequester = remember { FocusRequester() }
    val quickConnectFocusRequester = remember { FocusRequester() }
    val usernameFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val connectFocusRequester = remember { FocusRequester() }

    LaunchedEffect(state.phase) {
        if (state.phase == LoginContentPhase.ServerSearch) {
            serverFocusRequester.requestFocus()
        } else if (state.quickConnectAvailable) {
            quickConnectFocusRequester.requestFocus()
        } else {
            usernameFocusRequester.requestFocus()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
            .padding(horizontal = 64.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = if (state.phase == LoginContentPhase.Login) 1040.dp else 480.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PurefinLogo(
                color = scheme.primary,
                modifier = Modifier.size(64.dp)
            )

            Text(
                text = "Purefin",
                color = scheme.onBackground,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = if (state.phase == LoginContentPhase.ServerSearch) {
                    "Find your Jellyfin server"
                } else {
                    "Connect to your media server"
                },
                color = scheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(20.dp))

            state.errorMessage?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    color = scheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = scheme.errorContainer,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            when (state.phase) {
                LoginContentPhase.ServerSearch -> TvServerSearchFields(
                    state = state,
                    callbacks = callbacks,
                    serverFocusRequester = serverFocusRequester,
                    findFocusRequester = findFocusRequester
                )
                LoginContentPhase.Login -> TvLoginFields(
                    state = state,
                    callbacks = callbacks,
                    changeServerFocusRequester = changeServerFocusRequester,
                    quickConnectFocusRequester = quickConnectFocusRequester,
                    usernameFocusRequester = usernameFocusRequester,
                    passwordFocusRequester = passwordFocusRequester,
                    connectFocusRequester = connectFocusRequester
                )
            }
        }
    }
}

@Composable
private fun TvServerSearchFields(
    state: LoginContentState,
    callbacks: LoginContentCallbacks,
    serverFocusRequester: FocusRequester,
    findFocusRequester: FocusRequester
) {
    val scheme = MaterialTheme.colorScheme

    TvLoginTextField(
        label = "Server URL",
        value = state.serverUrl,
        onValueChange = callbacks.onServerUrlChange,
        placeholder = "http://192.168.1.100:8096",
        leadingIcon = Icons.Default.Storage,
        modifier = Modifier
            .focusRequester(serverFocusRequester)
            .focusProperties {
                down = findFocusRequester
            }
    )

    Spacer(modifier = Modifier.height(12.dp))

    Button(
        onClick = callbacks.onFindServer,
        enabled = !state.isSearching,
        modifier = Modifier
            .focusRequester(findFocusRequester)
            .focusProperties {
                up = serverFocusRequester
            }
            .fillMaxWidth()
            .height(48.dp)
    ) {
        TvText(
            text = if (state.isSearching) "Searching..." else "Find server",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }

    if (state.discoveredServers.isNotEmpty()) {
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Nearby servers",
            color = scheme.onBackground,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        state.discoveredServers.take(3).forEach { server ->
            Button(
                onClick = { callbacks.onDiscoveredServerClick(server) },
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                TvText(
                    text = server.name ?: server.address,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 15.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TvLoginFields(
    state: LoginContentState,
    callbacks: LoginContentCallbacks,
    changeServerFocusRequester: FocusRequester,
    quickConnectFocusRequester: FocusRequester,
    usernameFocusRequester: FocusRequester,
    passwordFocusRequester: FocusRequester,
    connectFocusRequester: FocusRequester
) {
    val scheme = MaterialTheme.colorScheme
    val selectedServer = state.selectedServerName ?: state.selectedServerUrl.orEmpty()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Selected server",
                color = scheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = selectedServer,
                color = scheme.onBackground,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Button(
            onClick = callbacks.onChangeServer,
            modifier = Modifier
                .focusRequester(changeServerFocusRequester)
                .focusProperties {
                    down = if (state.quickConnectAvailable) quickConnectFocusRequester else usernameFocusRequester
                }
                .width(176.dp)
                .height(44.dp)
        ) {
            TvText(text = "Change server", fontSize = 15.sp)
        }
    }

    Spacer(modifier = Modifier.height(18.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        TvLoginOptionPanel(
            title = "Quick Connect",
            modifier = Modifier.weight(1f)
        ) {
            if (state.quickConnectAvailable) {
                val quickConnectCode = state.quickConnectCode
                if (quickConnectCode != null) {
                    Text(
                        text = quickConnectCode,
                        color = scheme.primary,
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Approve this code in another Jellyfin client.",
                        color = scheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    Text(
                        text = "Use another Jellyfin client to approve this TV without entering your password.",
                        color = scheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Button(
                    onClick = if (state.quickConnectCode == null) {
                        callbacks.onQuickConnect
                    } else {
                        callbacks.onCancelQuickConnect
                    },
                    enabled = state.quickConnectCode != null || !state.isQuickConnecting,
                    modifier = Modifier
                        .focusRequester(quickConnectFocusRequester)
                        .focusProperties {
                            up = changeServerFocusRequester
                            right = usernameFocusRequester
                            down = usernameFocusRequester
                        }
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    TvText(
                        text = if (state.quickConnectCode == null) {
                            "Quick Connect"
                        } else {
                            "Cancel Quick Connect"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = "Quick Connect is not enabled on this server.",
                    color = scheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        TvLoginOptionPanel(
            title = "Manual login",
            modifier = Modifier.weight(1f)
        ) {
            TvLoginTextField(
                label = "Username",
                value = state.username,
                onValueChange = callbacks.onUsernameChange,
                placeholder = "Enter your username",
                leadingIcon = Icons.Default.Person,
                modifier = Modifier
                    .focusRequester(usernameFocusRequester)
                    .focusProperties {
                        up = changeServerFocusRequester
                        left = if (state.quickConnectAvailable) {
                            quickConnectFocusRequester
                        } else {
                            FocusRequester.Default
                        }
                        down = passwordFocusRequester
                    }
            )

            Spacer(modifier = Modifier.height(12.dp))

            TvLoginPasswordField(
                label = "Password",
                value = state.password,
                onValueChange = callbacks.onPasswordChange,
                placeholder = "Enter your password",
                leadingIcon = Icons.Default.Lock,
                modifier = Modifier
                    .focusRequester(passwordFocusRequester)
                    .focusProperties {
                        up = usernameFocusRequester
                        down = connectFocusRequester
                    }
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = callbacks.onConnect,
                enabled = !state.isQuickConnecting,
                modifier = Modifier
                    .focusRequester(connectFocusRequester)
                    .focusProperties {
                        up = passwordFocusRequester
                    }
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                TvText(
                    text = "Connect",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TvLoginOptionPanel(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(18.dp)

    Column(
        modifier = modifier
            .clip(shape)
            .background(scheme.surface)
            .border(
                width = 1.dp,
                color = scheme.outlineVariant.copy(alpha = 0.45f),
                shape = shape
            )
            .padding(22.dp)
    ) {
        Text(
            text = title,
            color = scheme.onBackground,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(18.dp))
        content()
    }
}

@Composable
private fun TvLoginTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    trailingIcon: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(14.dp)
    var isFocused by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = scheme.onBackground,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier
                .fillMaxWidth()
                .height(52.dp)
                .onFocusChanged { isFocused = it.isFocused || it.hasFocus }
                .border(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = if (isFocused) scheme.primary else scheme.outlineVariant.copy(alpha = 0.4f),
                    shape = shape
                )
                .clip(shape),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge,
            placeholder = {
                Text(
                    text = placeholder,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant
                )
            },
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = scheme.surfaceContainer,
                unfocusedContainerColor = scheme.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                cursorColor = scheme.primary,
                focusedTextColor = scheme.onSurface,
                unfocusedTextColor = scheme.onSurface
            )
        )
    }
}

@Composable
private fun TvLoginPasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    var isPasswordVisible by remember { mutableStateOf(false) }

    TvLoginTextField(
        label = label,
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        modifier = modifier,
        trailingIcon = {
            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                Icon(
                    imageVector = if (isPasswordVisible) {
                        Icons.Default.VisibilityOff
                    } else {
                        Icons.Default.Visibility
                    },
                    contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                    tint = scheme.onSurfaceVariant
                )
            }
        },
        visualTransformation = if (isPasswordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        }
    )
}
