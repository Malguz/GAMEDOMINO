package com.example.gamedomino.ui.lobby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamedomino.data.model.GameRoom
import com.example.gamedomino.data.model.User
import com.example.gamedomino.data.repository.GameRoomRepository
import com.example.gamedomino.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class RoomViewModel(
    private val repository: GameRoomRepository = GameRoomRepository()
) : ViewModel() {

    // Estado para la creación o ingreso a sala
    private val _roomActionState = MutableStateFlow<Resource<GameRoom>?>(null)
    val roomActionState: StateFlow<Resource<GameRoom>?> = _roomActionState.asStateFlow()

    // Estado para escuchar cambios en tiempo real dentro de una sala
    private val _activeRoomState = MutableStateFlow<Resource<GameRoom>?>(null)
    val activeRoomState: StateFlow<Resource<GameRoom>?> = _activeRoomState.asStateFlow()


    fun createRoom(currentUser: User) {
        viewModelScope.launch {
            _roomActionState.value = Resource.Loading
            _roomActionState.value = repository.createRoom(currentUser)
        }
    }


    fun joinRoom(code: String, currentUser: User) {
        if (code.isBlank() || code.length < 6) {
            _roomActionState.value = Resource.Error("Ingresa un código válido de 6 caracteres")
            return
        }

        viewModelScope.launch {
            _roomActionState.value = Resource.Loading
            _roomActionState.value = repository.joinRoomWithCode(code, currentUser)
        }
    }

    fun listenToRoomUpdates(roomId: String) {
        viewModelScope.launch {
            repository.listenRoomUpdates(roomId).collect { resource ->
                _activeRoomState.value = resource
            }
        }
    }


    fun startGame(roomId: String) {
        viewModelScope.launch {
            repository.startGame(roomId)
        }
    }


    fun resetActionState() {
        _roomActionState.value = null
    }
}