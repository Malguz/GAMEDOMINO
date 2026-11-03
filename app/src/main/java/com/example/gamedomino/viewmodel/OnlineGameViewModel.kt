package com.example.gamedomino.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamedomino.data.GameStateCodec
import com.example.gamedomino.data.repository.GameRoomRepository
import com.example.gamedomino.model.GameFactory
import com.example.gamedomino.model.Position
import com.example.gamedomino.model.Tile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Online game for one room. It mixes two things:
 *  - the shared game state that lives in Firebase, and
 *  - the local UI state (selected tile, highlighted cells...) that is per device.
 *
 * Rule: a device may only change the game when it is its turn
 * (currentPlayerId == myUserId). After a change it uploads the new state;
 * everyone else just renders what the listener brings.
 */
class OnlineGameViewModel(
    private val roomId: String,
    private val myUserId: String,
    private val players: List<Pair<String, String>>, // (userId, name), first 2 of the room
    private val isHost: Boolean,
    private val repository: GameRoomRepository = GameRoomRepository()
) : ViewModel() {

    // null game = we have not received the first state yet (shows "loading").
    private val _uiState = MutableStateFlow<GameUiState?>(null)
    val uiState: StateFlow<GameUiState?> = _uiState.asStateFlow()

    init {
        // The host seeds the initial deal so both devices read the very same game.
        if (isHost) {
            val initial = GameFactory.newGame(players.take(2))
            viewModelScope.launch {
                repository.setInitialGameState(roomId, GameStateCodec.encode(initial))
            }
        }
        // Everyone listens to the shared state.
        viewModelScope.launch {
            repository.listenGameState(roomId).collect { json ->
                val game = GameStateCodec.decode(json) ?: return@collect
                _uiState.update { current ->
                    when {
                        current == null ->
                            GameUiState(game = game, myPlayerId = myUserId)
                        current.game == game ->
                            current // same state we already show (our own echo): keep selection
                        else ->
                            // The board changed on the server: render it and drop stale selection.
                            GameUiState(game = game, myPlayerId = myUserId)
                    }
                }
            }
        }
    }

    // ---- user actions (only run when it is my turn) ----

    fun onTileSelected(tile: Tile) = localChange { GameUiReducer.selectTile(it, tile) }
    fun onFlip() = localChange { GameUiReducer.flip(it) }
    fun onCellTapped(position: Position) = localChange { GameUiReducer.tapCell(it, position) }
    fun onDrawTile() = localChange { GameUiReducer.drawTile(it) }
    fun onPassTurn() = localChange { GameUiReducer.passTurn(it) }

    /** Only the host may start a brand new game; it overwrites the shared state. */
    fun onNewGame() {
        if (!isHost) return
        val fresh = GameFactory.newGame(players.take(2))
        viewModelScope.launch {
            repository.pushGameState(roomId, GameStateCodec.encode(fresh))
        }
    }

    /**
     * Runs a reducer action on the local UI. If the action changed the shared game
     * (a move/draw/pass), upload the new game to Firebase.
     */
    private fun localChange(action: (GameUiState) -> GameUiState) {
        val current = _uiState.value ?: return
        val next = action(current)
        _uiState.value = next
        if (next.game != current.game) {
            viewModelScope.launch {
                repository.pushGameState(roomId, GameStateCodec.encode(next.game))
            }
        }
    }
}
