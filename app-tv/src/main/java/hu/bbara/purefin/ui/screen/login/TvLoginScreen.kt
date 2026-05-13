package hu.bbara.purefin.ui.screen.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.core.data.JellyfinServerCandidate
import hu.bbara.purefin.core.feature.login.LoginViewModel
import kotlinx.coroutines.launch

@Composable
fun TvLoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val serverUrl by viewModel.url.collectAsState()
    val phase by viewModel.phase.collectAsState()
    val selectedServer by viewModel.selectedServer.collectAsState()
    val discoveredServers by viewModel.discoveredServers.collectAsState()
    val username by viewModel.username.collectAsState()
    val password by viewModel.password.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val isLoggingIn by viewModel.isLoggingIn.collectAsState()
    val quickConnectAvailable by viewModel.quickConnectAvailable.collectAsState()
    val quickConnectCode by viewModel.quickConnectCode.collectAsState()
    val isQuickConnecting by viewModel.isQuickConnecting.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
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

    TvLoginContent(
        state = state,
        callbacks = callbacks,
        modifier = modifier
    )
}
