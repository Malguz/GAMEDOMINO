package com.example.gamedomino.model

import kotlin.math.abs

enum class MoveError {
    GAME_NOT_ACTIVE,
    NOT_YOUR_TURN,
    TILE_NOT_IN_HAND,
    OUT_OF_BOARD,
    CELLS_NOT_ADJACENT,
    CELL_OCCUPIED,
    MUST_TOUCH_PLACED_TILE,
    PIPS_DO_NOT_MATCH
}

sealed class MoveResult {
    object Valid : MoveResult()
    data class Invalid(val error: MoveError) : MoveResult()
}

/**
 * Pure rules for placing a tile. It knows nothing about Android, Firebase or the UI,
 * so it can be unit tested on the computer.
 */
object MoveValidator {

    /** Checks turn, hand and placement for [move]. */
    fun validate(state: GameState, move: Move): MoveResult {
        if (state.status != GameStatus.PLAYING) return MoveResult.Invalid(MoveError.GAME_NOT_ACTIVE)
        if (move.playerId != state.currentPlayerId) return MoveResult.Invalid(MoveError.NOT_YOUR_TURN)
        val player = state.playerById(move.playerId) ?: return MoveResult.Invalid(MoveError.NOT_YOUR_TURN)
        if (player.hand.none { it.sameAs(move.tile) }) return MoveResult.Invalid(MoveError.TILE_NOT_IN_HAND)
        return validatePlacement(state, move)
    }

    /** Every legal placement for [playerId]. Used to decide if the player must draw from the pool. */
    fun validMoves(state: GameState, playerId: String): List<Move> {
        val player = state.playerById(playerId) ?: return emptyList()
        val moves = mutableListOf<Move>()
        for (tile in player.hand) {
            for (oriented in listOf(tile, Tile(tile.b, tile.a)).distinct()) {
                for (row in 0 until BoardFactory.SIZE) {
                    for (col in 0 until BoardFactory.SIZE) {
                        val first = Position(row, col)
                        for (second in listOf(Position(row, col + 1), Position(row + 1, col))) {
                            val move = Move(playerId, oriented, first, second)
                            if (validatePlacement(state, move) == MoveResult.Valid) moves.add(move)
                        }
                    }
                }
            }
        }
        return moves
    }

    fun hasValidMove(state: GameState, playerId: String): Boolean =
        validMoves(state, playerId).isNotEmpty()

    private fun validatePlacement(state: GameState, move: Move): MoveResult {
        val firstCell = state.cellAt(move.first)
        val secondCell = state.cellAt(move.second)
        if (firstCell == null || secondCell == null) return MoveResult.Invalid(MoveError.OUT_OF_BOARD)
        if (!areAdjacent(move.first, move.second)) return MoveResult.Invalid(MoveError.CELLS_NOT_ADJACENT)
        if (firstCell.pips != null || secondCell.pips != null) return MoveResult.Invalid(MoveError.CELL_OCCUPIED)

        // The very first tile of the game can go anywhere.
        if (state.board.none { it.pips != null }) return MoveResult.Valid

        val contacts = contactsOf(state, move)
        if (contacts.isEmpty()) return MoveResult.Invalid(MoveError.MUST_TOUCH_PLACED_TILE)
        if (contacts.any { (mine, theirs) -> mine != theirs }) return MoveResult.Invalid(MoveError.PIPS_DO_NOT_MATCH)
        return MoveResult.Valid
    }

    /** Pairs of (pips of the new half, pips of the placed half it touches). */
    private fun contactsOf(state: GameState, move: Move): List<Pair<Int, Int>> {
        val halves = listOf(move.first to move.tile.a, move.second to move.tile.b)
        return halves.flatMap { (position, pips) ->
            neighborsOf(position)
                .filter { it != move.first && it != move.second }
                .mapNotNull { state.cellAt(it)?.pips }
                .map { neighborPips -> pips to neighborPips }
        }
    }

    private fun areAdjacent(a: Position, b: Position): Boolean =
        abs(a.row - b.row) + abs(a.col - b.col) == 1

    private fun neighborsOf(p: Position): List<Position> = listOf(
        Position(p.row - 1, p.col), Position(p.row + 1, p.col),
        Position(p.row, p.col - 1), Position(p.row, p.col + 1)
    )
}