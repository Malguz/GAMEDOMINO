package com.example.gamedomino.ui.lobby

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
import com.example.gamedomino.util.Resource

@Composable
fun LobbyScreen(
    currentUser: User,
    viewModel: RoomViewModel,
    onRoomJoined: (roomId: String) -> Unit
) {
    var codeInput by remember { mutableStateOf("") }
    val roomActionState by viewModel.roomActionState.collectAsState()

    // Si se crea o se une con éxito a una sala, navegamos a la Sala de Espera
    LaunchedEffect(roomActionState) {
        if (roomActionState is Resource.Success) {
            val room = (roomActionState as Resource.Success).data
            onRoomJoined(room.idSala)
            viewModel.resetActionState()
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
                    text = "LOBBY DE DOMINÓ",
                    fontSize = 22.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Bienvenido, ${currentUser.username}",
                    fontSize = 16.sp,
                    color = Color.LightGray
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Botón Crear Sala
                Button(
                    onClick = { viewModel.createRoom(currentUser) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Crear Nueva Sala")
                }

                Spacer(modifier = Modifier.height(20.dp))

                HorizontalDivider(color = Color.Gray)

                Spacer(modifier = Modifier.height(20.dp))

                // Unirse con Código
                OutlinedTextField(
                    value = codeInput,
                    onValueChange = { if (it.length <= 6) codeInput = it.uppercase() },
                    label = { Text("Código de 6 dígitos", color = Color.LightGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Gray
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.joinRoom(codeInput, currentUser) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Unirse a Sala")
                }

                if (roomActionState is Resource.Loading) {
                    Spacer(modifier = Modifier.height(12.dp))
                    CircularProgressIndicator(color = Color.White)
                }

                val error = (roomActionState as? Resource.Error)?.message
                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = error, color = Color.Red, fontSize = 14.sp)
                }
            }
        }
    }
}