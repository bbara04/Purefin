package hu.bbara.purefin.player

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import hu.bbara.purefin.core.data.HomeRepository
import hu.bbara.purefin.core.player.viewmodel.PlayerViewModel
import hu.bbara.purefin.ui.screen.player.PlayerScreen
import hu.bbara.purefin.ui.theme.AppTheme
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PlayerActivity : ComponentActivity() {
    private val viewModel: PlayerViewModel by viewModels()

    @Inject
    lateinit var homeRepository: HomeRepository

    companion object {
        const val EXTRA_LAST_MEDIA_ID = "LAST_MEDIA_ID"
    }

    override fun finish() {
        lifecycleScope.launch { homeRepository.refreshHomeData() }
        viewModel.currentMediaId()?.let { id ->
            setResult(RESULT_OK, Intent().putExtra(EXTRA_LAST_MEDIA_ID, id.toString()))
        }
        super.finish()
    }

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

    override fun onPause() {
        super.onPause()
        if (!isChangingConfigurations) {
            viewModel.pausePlayback()
        }
    }

    override fun onResume() {
        super.onResume()
        enterImmersiveMode()
    }

    private fun enterImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
