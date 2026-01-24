package hu.bbara.purefin.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.ui.PlayerView
import dagger.hilt.android.AndroidEntryPoint
import hu.bbara.purefin.player.viewmodel.PlayerViewModel
import hu.bbara.purefin.ui.theme.PurefinTheme

@AndroidEntryPoint
class PlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PurefinTheme(darkTheme = false) {
                val viewModel = hiltViewModel<PlayerViewModel>()
                Box(
                    modifier = Modifier.fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    AndroidView(
                        factory = { context ->
                            PlayerView(context).also {
                                it.player = viewModel.player
                            }
                        },
                        modifier = Modifier.fillMaxHeight()
                            .align(Alignment.Center)
                            .aspectRatio(16f / 9f)

                    )
                }
            }
        }
    }
}
