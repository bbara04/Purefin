package hu.bbara.purefin.ui.screen.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.core.data.JellyfinServerCandidate
import hu.bbara.purefin.core.feature.login.LoginViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val serverUrl by viewModel.url.collectAsStateWithLifecycle()
    val phase by viewModel.phase.collectAsStateWithLifecycle()
    val selectedServer by viewModel.selectedServer.collectAsStateWithLifecycle()
    val discoveredServers by viewModel.discoveredServers.collectAsStateWithLifecycle()
    val username by viewModel.username.collectAsStateWithLifecycle()
    val password by viewModel.password.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val isLoggingIn by viewModel.isLoggingIn.collectAsStateWithLifecycle()
    val quickConnectAvailable by viewModel.quickConnectAvailable.collectAsStateWithLifecycle()
    val quickConnectCode by viewModel.quickConnectCode.collectAsStateWithLifecycle()
    val isQuickConnecting by viewModel.isQuickConnecting.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    val state = remember(
        phase,
        serverUrl,
        selectedServer,
        discoveredServers,
        username,
        password,
        isSearching,
        isLoggingIn,
        quickConnectAvailable,
        quickConnectCode,
        isQuickConnecting,
        errorMessage
    ) {
        LoginContentState(
            phase = if (phase == LoginViewModel.Phase.LOGIN) {
                LoginContentPhase.Login
            } else {
                LoginContentPhase.ServerSearch
            },
            serverUrl = serverUrl,
            selectedServerName = selectedServer?.name,
            selectedServerUrl = selectedServer?.address,
            discoveredServers = discoveredServers.map {
                LoginServerCandidateUi(name = it.name, address = it.address)
            },
            username = username,
            password = password,
            isSearching = isSearching,
            isLoggingIn = isLoggingIn,
            quickConnectAvailable = quickConnectAvailable,
            quickConnectCode = quickConnectCode,
            isQuickConnecting = isQuickConnecting,
            errorMessage = errorMessage
        )
    }
    val callbacks = remember(viewModel, coroutineScope) {
        LoginContentCallbacks(
            onServerUrlChange = viewModel::setUrl,
            onFindServer = viewModel::findServer,
            onDiscoveredServerClick = {
                viewModel.selectDiscoveredServer(JellyfinServerCandidate(name = it.name, address = it.address))
            },
            onChangeServer = viewModel::changeServer,
            onUsernameChange = {
                viewModel.setUsername(it)
            },
            onPasswordChange = {
                viewModel.setPassword(it)
            },
            onConnect = {
                coroutineScope.launch {
                    viewModel.login()
                }
            },
            onQuickConnect = viewModel::startQuickConnect,
            onCancelQuickConnect = viewModel::cancelQuickConnect
        )
    }

    LoginContent(
        state = state,
        callbacks = callbacks,
        modifier = modifier
    )
}
