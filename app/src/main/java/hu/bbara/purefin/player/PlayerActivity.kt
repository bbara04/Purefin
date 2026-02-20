package hu.bbara.purefin.player

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import hu.bbara.purefin.core.player.viewmodel.PlayerViewModel
import hu.bbara.purefin.player.ui.PlayerScreen
import hu.bbara.purefin.ui.theme.AppTheme

@AndroidEntryPoint
class PlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterImmersiveMode()

        setContent {
            AppTheme(darkTheme = true) {
                val viewModel = hiltViewModel<PlayerViewModel>()
                val uiState = viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(uiState.value.isPlaying) {
                    if (uiState.value.isPlaying) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }

                PlayerScreen(
                    viewModel = viewModel,
                    onBack = { finish() }
                )
            }
        }
    }

    private fun enterImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
