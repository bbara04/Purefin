package hu.bbara.purefin.login.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.common.ui.PurefinComplexTextField
import hu.bbara.purefin.common.ui.PurefinPasswordField
import hu.bbara.purefin.common.ui.PurefinTextButton
import hu.bbara.purefin.login.viewmodel.LoginViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val JellyfinOrange = Color(0xFFBD542E)
    val JellyfinBg = Color(0xFF141517)
    val JellyfinSurface = Color(0xFF1E2124)
    val TextSecondary = Color(0xFF9EA3A8)

    // Observe ViewModel state
    val serverUrl by viewModel.url.collectAsState("")
    val username by viewModel.username.collectAsState()
    val password by viewModel.password.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JellyfinBg)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.5f))

        // Logo Section
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(JellyfinOrange, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Movie, // Replace with actual logo resource
                contentDescription = "Logo",
                tint = Color.White,
                modifier = Modifier.size(60.dp)
            )
        }

        Text(
            text = "Jellyfin",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = "PERSONAL MEDIA SYSTEM",
            color = TextSecondary,
            fontSize = 12.sp,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Form Section
        Text(
            text = "Connect to Server",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )
        Text(
            text = "Enter your details to access your library",
            color = TextSecondary,
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 24.dp)
        )

        PurefinComplexTextField(
            label = "Server URL",
            value = serverUrl,
            onValueChange = { coroutineScope.launch { viewModel.setUrl(it) } },
            placeholder = "http://192.168.1.100:8096",
            leadingIcon = Icons.Default.Storage
        )

        Spacer(modifier = Modifier.height(16.dp))

        PurefinComplexTextField(
            label = "Username",
            value = username,
            onValueChange = { viewModel.setUsername(it) },
            placeholder = "Enter your username",
            leadingIcon = Icons.Default.Person
        )

        Spacer(modifier = Modifier.height(16.dp))

        PurefinPasswordField(
            label = "Password",
            value = password,
            onValueChange = { viewModel.setPassword(it) },
            placeholder = "••••••••",
            leadingIcon = Icons.Default.Lock,
        )

        Spacer(modifier = Modifier.height(32.dp))

        PurefinTextButton(
            content = { Text("Connect") },
            onClick = {
                coroutineScope.launch {
                    viewModel.login()
                }
            }
        )

        Spacer(modifier = Modifier.weight(0.5f))

        // Footer Links
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = {}) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                    Text(" Discover Servers", color = TextSecondary)
                }
            }
            TextButton(onClick = {}) {
                Text("Need Help?", color = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

    }
}
