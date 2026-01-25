package hu.bbara.purefin.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import hu.bbara.purefin.player.ui.PlayerScreen
import hu.bbara.purefin.player.viewmodel.PlayerViewModel
import hu.bbara.purefin.ui.theme.PurefinTheme

@AndroidEntryPoint
class PlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterImmersiveMode()

        setContent {
            PurefinTheme(darkTheme = true) {
                val viewModel = hiltViewModel<PlayerViewModel>()
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
