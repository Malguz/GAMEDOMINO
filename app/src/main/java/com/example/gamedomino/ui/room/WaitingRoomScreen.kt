package com.example.gamedomino.ui.room

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gamedomino.R
import com.example.gamedomino.data.model.User
import com.example.gamedomino.ui.lobby.RoomViewModel
import com.example.gamedomino.util.Resource

@Composable
fun WaitingRoomScreen(
    roomId: String,
    currentUser: User,
    viewModel: RoomViewModel,
    onGameStarted: () -> Unit
) {
    // Escuchar la sala en tiempo real tan pronto se cargue la pantalla
    LaunchedEffect(roomId) {
        viewModel.listenToRoomUpdates(roomId)
    }

    val activeRoomState by viewModel.activeRoomState.collectAsState()

    // Si el estado de la partida cambia a "PLAYING", navegamos al tablero (Integrante 2)
    LaunchedEffect(activeRoomState) {
        if (activeRoomState is Resource.Success) {
            val room = (activeRoomState as Resource.Success).data
            if (room.estadoPartida == "PLAYING") {
                onGameStarted()
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.fondo_domino),
            contentDescription = "Fondo Dominó",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        when (val state = activeRoomState) {
            is Resource.Loading -> {
                CircularProgressIndicator(color = Color.White)
            }
            is Resource.Error -> {
                Text(text = state.message, color = Color.Red, fontSize = 18.sp)
            }
            is Resource.Success -> {
                val room = state.data
                val isHost = room.hostId == currentUser.id

                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.8f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "SALA DE ESPERA",
                            fontSize = 20.sp,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "CÓDIGO: ${room.codigoSala}",
                            fontSize = 26.sp,
                            color = Color.Yellow
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Jugadores Conectados (${room.listaJugadores.size}/4):",
                            fontSize = 14.sp,
                            color = Color.LightGray
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Lista en tiempo real de los jugadores conectados
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 180.dp)
                        ) {
                            items(room.listaJugadores) { player ->
                                Text(
                                    text = "• ${player.username}" + if (player.id == room.hostId) " (Anfitrión)" else "",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (isHost) {
                            Button(
                                onClick = { viewModel.startGame(room.idSala) },
                                enabled = room.listaJugadores.isNotEmpty(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Iniciar Partida")
                            }
                        } else {
                            Text(
                                text = "Esperando que el anfitrión inicie...",
                                color = Color.LightGray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
            null -> {}
        }
    }
}