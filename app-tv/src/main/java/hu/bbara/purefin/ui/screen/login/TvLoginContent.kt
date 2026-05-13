package hu.bbara.purefin.ui.screen.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
    isLoggingIn: Boolean,
    modifier: Modifier = Modifier
) {
    if (isLoggingIn) {
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
    val usernameFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val connectFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        serverFocusRequester.requestFocus()
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
                .widthIn(max = 480.dp)
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
                text = "Connect to your media server",
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

            TvLoginTextField(
                label = "Server URL",
                value = state.serverUrl,
                onValueChange = callbacks.onServerUrlChange,
                placeholder = "http://192.168.1.100:8096",
                leadingIcon = Icons.Default.Storage,
                modifier = Modifier
                    .focusRequester(serverFocusRequester)
                    .focusProperties {
                        down = usernameFocusRequester
                    }
            )

            Spacer(modifier = Modifier.height(12.dp))

            TvLoginTextField(
                label = "Username",
                value = state.username,
                onValueChange = callbacks.onUsernameChange,
                placeholder = "Enter your username",
                leadingIcon = Icons.Default.Person,
                modifier = Modifier
                    .focusRequester(usernameFocusRequester)
                    .focusProperties {
                        up = serverFocusRequester
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

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = callbacks.onConnect,
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
