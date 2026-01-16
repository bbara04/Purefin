package hu.bbara.purefin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import hu.bbara.purefin.login.ui.LoginScreen
import hu.bbara.purefin.session.UserSessionRepository
import hu.bbara.purefin.ui.theme.PurefinTheme
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PurefinActivity : ComponentActivity() {

    @Inject
    lateinit var userSessionRepository: UserSessionRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PurefinTheme {
                val scope = rememberCoroutineScope()

                val isLoggedIn by userSessionRepository.isLoggedIn.collectAsState(initial = false)

                if (isLoggedIn) {
                    HomeScreen(logout = { scope.launch { userSessionRepository.setLoggedIn(false) } })
                } else {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        LoginScreen(modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier,
               logout: () -> Unit) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Welcome to PureFin", style = MaterialTheme.typography.headlineLarge)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(5) { index ->
                    StreamingCard("Show ${index + 1}", "Description for show ${index + 1}")
                }
            }
            Button(onClick = logout) {
                Text("Logout")
            }
        }
    }
}

@Composable
fun StreamingCard(title: String, description: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodySmall)
        }
    }
}
