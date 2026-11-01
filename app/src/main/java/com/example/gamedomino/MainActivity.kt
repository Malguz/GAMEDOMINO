package com.example.gamedomino

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gamedomino.data.model.User
import com.example.gamedomino.ui.auth.AuthScreen
import com.example.gamedomino.ui.auth.AuthViewModel
import com.example.gamedomino.ui.lobby.LobbyScreen
import com.example.gamedomino.ui.lobby.RoomViewModel
import com.example.gamedomino.ui.room.WaitingRoomScreen
import com.example.gamedomino.ui.theme.GameDominoTheme
import com.example.gamedomino.util.Resource

enum class Screen {
    AUTH, LOBBY, WAITING_ROOM, GAME_BOARD
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GameDominoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val authViewModel: AuthViewModel = viewModel()
    val roomViewModel: RoomViewModel = viewModel()

    var currentScreen by remember {
        mutableStateOf(if (authViewModel.isUserLoggedIn()) Screen.LOBBY else Screen.AUTH)
    }

    var activeRoomId by remember { mutableStateOf("") }

    val loginState by authViewModel.loginState.collectAsState()
    val currentUser = User(
        id = (loginState as? Resource.Success)?.data?.id ?: "user_id",
        username = (loginState as? Resource.Success)?.data?.username ?: "Jugador"
    )

    when (currentScreen) {
        Screen.AUTH -> {
            AuthScreen(
                viewModel = authViewModel,
                onAuthSuccess = { currentScreen = Screen.LOBBY }
            )
        }
        Screen.LOBBY -> {
            LobbyScreen(
                currentUser = currentUser,
                viewModel = roomViewModel,
                onRoomJoined = { roomId ->
                    activeRoomId = roomId
                    currentScreen = Screen.WAITING_ROOM
                }
            )
        }
        Screen.WAITING_ROOM -> {
            WaitingRoomScreen(
                roomId = activeRoomId,
                currentUser = currentUser,
                viewModel = roomViewModel,
                onGameStarted = { currentScreen = Screen.GAME_BOARD }
            )
        }
        Screen.GAME_BOARD -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "¡La Partida Ha Comenzado!\n(Módulo del Integrante 2)",
                    fontSize = 20.sp,
                    color = Color.Black
                )
            }
        }
    }
}