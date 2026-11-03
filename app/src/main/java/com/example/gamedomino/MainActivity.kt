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
import com.example.gamedomino.ui.game.OnlineGameScreen
import com.example.gamedomino.viewmodel.OnlineGameViewModel

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
    val loggedUser = (loginState as? Resource.Success)?.data
    val currentUser = User(
        id = loggedUser?.id ?: authViewModel.currentUserId() ?: "user_id",
        username = loggedUser?.username ?: authViewModel.currentUserName() ?: "Jugador"
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
            val roomResource = roomViewModel.activeRoomState.collectAsState().value
            val room = (roomResource as? Resource.Success)?.data
            if (room == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Cargando partida...")
                }
            } else {
                // First two players of the room become the two game players.
                val players = room.listaJugadores.take(2).map { it.id to it.username }
                val isHost = room.hostId == currentUser.id
                val onlineViewModel = remember(room.idSala, currentUser.id) {
                    OnlineGameViewModel(
                        roomId = room.idSala,
                        myUserId = currentUser.id,
                        players = players,
                        isHost = isHost
                    )
                }
                OnlineGameScreen(
                    viewModel = onlineViewModel,
                    isHost = isHost,
                    onBackToMenu = { currentScreen = Screen.LOBBY }
                )
            }
        }
    }
}