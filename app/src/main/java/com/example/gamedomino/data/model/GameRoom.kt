package com.example.gamedomino.data.model

// Agrega esta importación:
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class GameRoom(
    val idSala: String = "",
    val codigoSala: String = "",
    val hostId: String = "",
    val estadoPartida: String = "WAITING",
    val listaJugadores: List<User> = emptyList(),
    val turnoActualId: String = "",
    val tableroState: String = "",
    val ganadorId: String = ""
)
