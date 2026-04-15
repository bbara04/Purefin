package hu.bbara.purefin.ui.screen.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.feature.login.LoginViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val serverUrl by viewModel.url.collectAsState()
    val username by viewModel.username.collectAsState()
    val password by viewModel.password.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var isLoggingIn by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val state = remember(serverUrl, username, password, errorMessage) {
        LoginContentState(
            serverUrl = serverUrl,
            username = username,
            password = password,
            errorMessage = errorMessage
        )
    }
    val callbacks = remember(viewModel, coroutineScope) {
        LoginContentCallbacks(
            onServerUrlChange = {
                viewModel.clearError()
                viewModel.setUrl(it)
            },
            onUsernameChange = {
                viewModel.clearError()
                viewModel.setUsername(it)
            },
            onPasswordChange = {
                viewModel.clearError()
                viewModel.setPassword(it)
            },
            onConnect = {
                coroutineScope.launch {
                    isLoggingIn = true
                    try {
                        viewModel.login()
                    } finally {
                        isLoggingIn = false
                    }
                }
            }
        )
    }

    LoginContent(
        state = state,
        callbacks = callbacks,
        isLoggingIn = isLoggingIn,
        modifier = modifier
    )
}
