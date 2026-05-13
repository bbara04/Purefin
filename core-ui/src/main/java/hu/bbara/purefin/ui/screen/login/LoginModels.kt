package hu.bbara.purefin.ui.screen.login

import androidx.compose.runtime.Immutable

enum class LoginContentPhase {
    ServerSearch,
    Login
}

@Immutable
data class LoginServerCandidateUi(
    val name: String?,
    val address: String
)

@Immutable
data class LoginContentState(
    val phase: LoginContentPhase,
    val serverUrl: String,
    val selectedServerName: String?,
    val selectedServerUrl: String?,
    val discoveredServers: List<LoginServerCandidateUi>,
    val username: String,
    val password: String,
    val isSearching: Boolean,
    val isLoggingIn: Boolean,
    val quickConnectAvailable: Boolean,
    val quickConnectCode: String?,
    val isQuickConnecting: Boolean,
    val errorMessage: String? = null
)

class LoginContentCallbacks(
    val onServerUrlChange: (String) -> Unit,
    val onFindServer: () -> Unit,
    val onDiscoveredServerClick: (LoginServerCandidateUi) -> Unit,
    val onChangeServer: () -> Unit,
    val onUsernameChange: (String) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onConnect: () -> Unit,
    val onQuickConnect: () -> Unit,
    val onCancelQuickConnect: () -> Unit
)
