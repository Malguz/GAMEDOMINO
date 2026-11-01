package com.example.gamedomino.data.repository

import com.example.gamedomino.data.model.GameRoom
import com.example.gamedomino.data.model.User
import com.example.gamedomino.util.CodeGenerator
import com.example.gamedomino.util.Resource
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repositorio para la creación, gestión tiempo real de las salas de juego.
 */
class GameRoomRepository(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance("https://gamedomino-f34b5-default-rtdb.firebaseio.com/")
) {

    private val roomsRef = database.getReference("rooms")

    /**
     * Crea una sala nueva con un código de 6 dígitos alfanuméricos.
     */
    suspend fun createRoom(hostUser: User): Resource<GameRoom> {
        return try {
            val roomId = roomsRef.push().key ?: throw Exception("No se pudo generar la sala")
            val code = CodeGenerator.generateRoomCode()

            val room = GameRoom(
                idSala = roomId,
                codigoSala = code,
                hostId = hostUser.id,
                estadoPartida = "WAITING",
                listaJugadores = listOf(hostUser)
            )

            roomsRef.child(roomId).setValue(room).await()
            Resource.Success(room)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error al crear la sala")
        }
    }

    /**
     * Permite a un usuario unirse a una sala de espera mediante su código de 6 dígitos.
     */
    suspend fun joinRoomWithCode(code: String, user: User): Resource<GameRoom> {
        return try {
            val snapshot = roomsRef.orderByChild("codigoSala").equalTo(code.uppercase()).get().await()

            if (!snapshot.exists()) {
                return Resource.Error("La sala con el código $code no existe")
            }

            val roomSnapshot = snapshot.children.first()
            val room = roomSnapshot.getValue(GameRoom::class.java)
                ?: return Resource.Error("Error al leer datos de la sala")

            if (room.estadoPartida != "WAITING") {
                return Resource.Error("La partida ya ha comenzado o finalizado")
            }

            // Verificar si el jugador ya está dentro de la sala
            val listUpdated = room.listaJugadores.toMutableList()
            if (listUpdated.none { it.id == user.id }) {
                listUpdated.add(user)
                roomsRef.child(room.idSala).child("listaJugadores").setValue(listUpdated).await()
            }

            Resource.Success(room.copy(listaJugadores = listUpdated))
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error al unirse a la sala")
        }
    }

    /**
     * Escucha en tiempo real cualquier cambio en la sala de espera.
     */
    fun listenRoomUpdates(roomId: String): Flow<Resource<GameRoom>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val room = snapshot.getValue(GameRoom::class.java)
                if (room != null) {
                    trySend(Resource.Success(room))
                } else {
                    trySend(Resource.Error("La sala fue eliminada"))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Resource.Error(error.message))
            }
        }

        val roomNode = roomsRef.child(roomId)
        roomNode.addValueEventListener(listener)

        awaitClose { roomNode.removeEventListener(listener) }
    }

    /**
     * Cambia el estado de la sala a "PLAYING" para dar inicio a la partida de Dominó.
     */
    suspend fun startGame(roomId: String): Resource<Unit> {
        return try {
            roomsRef.child(roomId).child("estadoPartida").setValue("PLAYING").await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "No se pudo iniciar la partida")
        }
    }
}