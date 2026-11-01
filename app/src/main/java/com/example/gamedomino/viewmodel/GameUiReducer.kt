package viewmodel

import com.example.gamedomino.model.BoardFactory
import com.example.gamedomino.model.Move
import com.example.gamedomino.model.MoveResult
import com.example.gamedomino.model.MoveValidator
import com.example.gamedomino.model.PlayResult
import com.example.gamedomino.model.Position
import com.example.gamedomino.model.Tile
import com.example.gamedomino.model.TurnManager
import com.example.gamedomino.model.TurnResult
import com.example.gamedomino.model.sameAs

/**
 * Pure functions: (current UI state, user action) -> new UI state.
 * No Android code here, so it is unit tested on the computer.
 * The ViewModel only stores the state and calls these functions.
 */
object GameUiReducer {

    fun selectTile(ui: GameUiState, tile: Tile): GameUiState {
        if (!canAct(ui)) return ui
        if (ui.currentPlayer.hand.none { it.sameAs(tile) }) return ui
        if (ui.selectedTile?.sameAs(tile) == true) return clearSelection(ui)
        return refresh(ui.copy(selectedTile = tile, flipped = false, firstCell = null, lastEvent = null))
    }

    fun flip(ui: GameUiState): GameUiState {
        if (!canAct(ui) || ui.selectedTile == null) return ui
        return refresh(ui.copy(flipped = !ui.flipped, firstCell = null, lastEvent = null))
    }

    fun tapCell(ui: GameUiState, position: Position): GameUiState {
        if (!canAct(ui)) return ui
        val tile = ui.effectiveTile ?: return ui
        val first = ui.firstCell
        return when {
            first == null ->
                if (position in ui.highlighted) refresh(ui.copy(firstCell = position, lastEvent = null)) else ui
            position == first -> refresh(ui.copy(firstCell = null))
            position in ui.highlighted -> play(ui, Move(ui.game.currentPlayerId, tile, first, position))
            else -> ui
        }
    }

    fun drawTile(ui: GameUiState): GameUiState {
        if (!canAct(ui)) return ui
        return when (val result = TurnManager.drawTile(ui.game, ui.game.currentPlayerId)) {
            is TurnResult.Done -> cleared(ui.copy(game = result.state, lastEvent = GameEvent.TileDrawn))
            is TurnResult.Rejected -> ui.copy(lastEvent = GameEvent.TurnRejected(result.error))
        }
    }

    fun passTurn(ui: GameUiState): GameUiState {
        if (!canAct(ui)) return ui
        return when (val result = TurnManager.passTurn(ui.game, ui.game.currentPlayerId)) {
            is TurnResult.Done -> cleared(ui.copy(game = result.state, lastEvent = GameEvent.Passed))
            is TurnResult.Rejected -> ui.copy(lastEvent = GameEvent.TurnRejected(result.error))
        }
    }

    fun clearSelection(ui: GameUiState): GameUiState = cleared(ui.copy(lastEvent = null))

    // ---- helpers ----

    private fun canAct(ui: GameUiState) =
        ui.isMyTurn && ui.game.status == com.example.gamedomino.model.GameStatus.PLAYING

    private fun play(ui: GameUiState, move: Move): GameUiState =
        when (val result = TurnManager.playMove(ui.game, move)) {
            is PlayResult.Played -> cleared(
                ui.copy(game = result.state, lastEvent = GameEvent.Scored(result.pointsGained, result.extraTurn))
            )
            is PlayResult.Rejected -> refresh(
                ui.copy(firstCell = null, lastEvent = GameEvent.MoveRejected(result.error))
            )
        }

    private fun cleared(ui: GameUiState) =
        ui.copy(selectedTile = null, flipped = false, firstCell = null, highlighted = emptySet())

    /** Recomputes which cells to highlight for the selected tile (first cell, or partner of the first cell). */
    private fun refresh(ui: GameUiState): GameUiState {
        val tile = ui.effectiveTile ?: return ui.copy(highlighted = emptySet())
        val first = ui.firstCell
        val cells = if (first == null) {
            allPositions().filter { partnersOf(ui, tile, it).isNotEmpty() }
        } else {
            partnersOf(ui, tile, first)
        }
        return ui.copy(highlighted = cells.toSet())
    }

    private fun partnersOf(ui: GameUiState, tile: Tile, from: Position): List<Position> =
        neighborsOf(from).filter {
            MoveValidator.validate(ui.game, Move(ui.game.currentPlayerId, tile, from, it)) == MoveResult.Valid
        }

    private fun allPositions() = (0 until BoardFactory.SIZE).flatMap { row ->
        (0 until BoardFactory.SIZE).map { col -> Position(row, col) }
    }

    private fun neighborsOf(p: Position) = listOf(
        Position(p.row - 1, p.col), Position(p.row + 1, p.col),
        Position(p.row, p.col - 1), Position(p.row, p.col + 1)
    )
}