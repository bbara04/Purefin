package hu.bbara.purefin.ui.screen.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.ui.common.button.PurefinTextButton
import hu.bbara.purefin.ui.common.image.PurefinLogo
import hu.bbara.purefin.ui.common.textfield.PurefinComplexTextField
import hu.bbara.purefin.ui.common.textfield.PurefinPasswordField
import hu.bbara.purefin.ui.screen.waiting.PurefinWaitingScreen

@Composable
fun LoginContent(
    state: LoginContentState,
    callbacks: LoginContentCallbacks,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    if (state.isLoggingIn) {
        PurefinWaitingScreen(modifier = modifier)
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
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
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Personal media, your way",
                color = scheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(28.dp))

            state.errorMessage?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    color = scheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            scheme.errorContainer,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            when (state.phase) {
                LoginContentPhase.ServerSearch -> ServerSearchContent(state, callbacks)
                LoginContentPhase.Login -> LoginPhaseContent(state, callbacks)
            }
        }
    }
}

@Composable
private fun ServerSearchContent(
    state: LoginContentState,
    callbacks: LoginContentCallbacks
) {
    val scheme = MaterialTheme.colorScheme

    Text(
        text = "Find your server",
        color = scheme.onBackground,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.fillMaxWidth()
    )
    Text(
        text = "Search by address or choose a nearby Jellyfin server.",
        color = scheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = 2.dp, bottom = 16.dp).fillMaxWidth()
    )

    PurefinComplexTextField(
        label = "Server URL",
        value = state.serverUrl,
        onValueChange = callbacks.onServerUrlChange,
        placeholder = "http://192.168.1.100:8096",
        leadingIcon = Icons.Default.Storage
    )

    Spacer(modifier = Modifier.height(16.dp))

    PurefinTextButton(
        content = { Text(if (state.isSearching) "Searching..." else "Find server") },
        onClick = callbacks.onFindServer,
        enabled = !state.isSearching,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    )

    if (state.discoveredServers.isNotEmpty()) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Nearby servers",
            color = scheme.onBackground,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        state.discoveredServers.forEach { server ->
            TextButton(
                onClick = { callbacks.onDiscoveredServerClick(server) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = server.name?.let { "$it\n${server.address}" } ?: server.address,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun LoginPhaseContent(
    state: LoginContentState,
    callbacks: LoginContentCallbacks
) {
    val scheme = MaterialTheme.colorScheme
    val selectedServer = state.selectedServerName ?: state.selectedServerUrl.orEmpty()

    Text(
        text = "Log in",
        color = scheme.onBackground,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.fillMaxWidth()
    )
    Text(
        text = selectedServer,
        color = scheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = 2.dp).fillMaxWidth()
    )
    TextButton(
        onClick = callbacks.onChangeServer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Change server")
    }

    if (state.quickConnectAvailable) {
        Spacer(modifier = Modifier.height(8.dp))
        state.quickConnectCode?.let { code ->
            Text(
                text = code,
                color = scheme.primary,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Approve this code in another Jellyfin client.",
                color = scheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            TextButton(onClick = callbacks.onCancelQuickConnect) {
                Text("Cancel Quick Connect")
            }
        } ?: PurefinTextButton(
            content = { Text("Quick Connect") },
            onClick = callbacks.onQuickConnect,
            enabled = !state.isQuickConnecting,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        )
    } else {
        Text(
            text = "Quick Connect is not enabled on this server.",
            color = scheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth()
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(
        text = "Manual login",
        color = scheme.onBackground,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(12.dp))

    PurefinComplexTextField(
        label = "Username",
        value = state.username,
        onValueChange = callbacks.onUsernameChange,
        placeholder = "Enter your username",
        leadingIcon = Icons.Default.Person
    )

    Spacer(modifier = Modifier.height(12.dp))

    PurefinPasswordField(
        label = "Password",
        value = state.password,
        onValueChange = callbacks.onPasswordChange,
        placeholder = "Enter your password",
        leadingIcon = Icons.Default.Lock,
    )

    Spacer(modifier = Modifier.height(24.dp))

    PurefinTextButton(
        content = { Text("Connect") },
        onClick = callbacks.onConnect,
        enabled = !state.isQuickConnecting,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    )
}
