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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
    isLoggingIn: Boolean,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    if (isLoggingIn) {
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

            Text(
                text = "Connect to server",
                color = scheme.onBackground,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.align(Alignment.Start)
            )
            Text(
                text = "Enter your server and account details.",
                color = scheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 2.dp, bottom = 16.dp)
            )

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

            PurefinComplexTextField(
                label = "Server URL",
                value = state.serverUrl,
                onValueChange = callbacks.onServerUrlChange,
                placeholder = "http://192.168.1.100:8096",
                leadingIcon = Icons.Default.Storage
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )
        }
    }
}
