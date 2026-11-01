package com.example.gamedomino.viewmodel
import androidx.lifecycle.ViewModel
import com.example.gamedomino.model.GameFactory
import com.example.gamedomino.model.Position
import com.example.gamedomino.model.Tile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Holds the UI state and forwards every user action to [GameUiReducer]. */
class GameViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(newLocalGame())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    fun onTileSelected(tile: Tile) = _uiState.update { GameUiReducer.selectTile(it, tile) }
    fun onFlip() = _uiState.update { GameUiReducer.flip(it) }
    fun onCellTapped(position: Position) = _uiState.update { GameUiReducer.tapCell(it, position) }
    fun onDrawTile() = _uiState.update { GameUiReducer.drawTile(it) }
    fun onPassTurn() = _uiState.update { GameUiReducer.passTurn(it) }
    fun onNewGame() { _uiState.value = newLocalGame() }

    private fun newLocalGame() = GameUiState(
        game = GameFactory.newGame(listOf("p1" to "Player 1", "p2" to "Player 2"))
    )
}