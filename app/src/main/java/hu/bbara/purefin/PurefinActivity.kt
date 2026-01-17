package hu.bbara.purefin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import hu.bbara.purefin.app.HomePage
import hu.bbara.purefin.client.JellyfinApiClient
import hu.bbara.purefin.login.ui.LoginScreen
import hu.bbara.purefin.session.UserSessionRepository
import hu.bbara.purefin.ui.theme.PurefinTheme
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PurefinActivity : ComponentActivity() {

    @Inject
    lateinit var userSessionRepository: UserSessionRepository
    @Inject
    lateinit var jellyfinApiClient: JellyfinApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch { init() }
        enableEdgeToEdge()
        setContent {
            PurefinTheme {
                val isLoggedIn by userSessionRepository.isLoggedIn.collectAsState(initial = false)
                if (isLoggedIn) {
                    HomePage()
                } else {
                    LoginScreen()
                }
            }
        }
    }

    private suspend fun init() {
        jellyfinApiClient.updateApiClient()
    }
}
