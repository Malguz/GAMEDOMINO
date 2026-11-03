package com.example.gamedomino.viewmodel
import com.example.gamedomino.model.GameState
import com.example.gamedomino.model.MoveError
import com.example.gamedomino.model.MoveValidator
import com.example.gamedomino.model.Player
import com.example.gamedomino.model.Position
import com.example.gamedomino.model.Tile
import com.example.gamedomino.model.TurnError

/** One-shot messages the screen can show (a toast, a snackbar...). */
sealed class GameEvent {
    data class MoveRejected(val error: MoveError) : GameEvent()
    data class TurnRejected(val error: TurnError) : GameEvent()
    data class Scored(val points: Int, val extraTurn: Boolean) : GameEvent()
    object TileDrawn : GameEvent()
    object Passed : GameEvent()
}

/**
 * Everything the game screen needs to draw itself.
 * [myPlayerId] = null means "one device, everybody plays" (local mode).
 */
data class GameUiState(
    val game: GameState,
    val myPlayerId: String? = null,
    val selectedTile: Tile? = null,
    val flipped: Boolean = false,          // flipped = the second half goes on the first tapped cell
    val firstCell: Position? = null,
    val highlighted: Set<Position> = emptySet(),
    val lastEvent: GameEvent? = null
) {
    val currentPlayer: Player
        get() = game.players.first { it.id == game.currentPlayerId }

    val isMyTurn: Boolean
        get() = myPlayerId == null || myPlayerId == game.currentPlayerId

    /** The final result when the game is over, or null while it is still being played. */
    val result: com.example.gamedomino.model.GameResult?
        get() = if (game.status == com.example.gamedomino.model.GameStatus.FINISHED)
            com.example.gamedomino.model.GameRules.winnerOf(game) else null

    /** False means the current player must draw (or pass if the pool is empty). */
    val canPlay: Boolean
        get() = MoveValidator.hasValidMove(game, game.currentPlayerId)

    /** The selected tile in the orientation the player chose: its first half goes on the first tapped cell. */
    val effectiveTile: Tile?
        get() = selectedTile?.let { if (flipped) Tile(it.b, it.a) else it }
}