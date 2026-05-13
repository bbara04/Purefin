package hu.bbara.purefin.ui.screen.login

import androidx.compose.runtime.Immutable

@Immutable
data class LoginContentState(
    val serverUrl: String,
    val username: String,
    val password: String,
    val errorMessage: String? = null
)

class LoginContentCallbacks(
    val onServerUrlChange: (String) -> Unit,
    val onUsernameChange: (String) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onConnect: () -> Unit
)
