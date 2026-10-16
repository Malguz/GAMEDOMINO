package com.example.gamedomino.model

enum class TurnError {
    GAME_NOT_ACTIVE,
    NOT_YOUR_TURN,
    HAS_VALID_MOVE,   // you cannot draw or pass while you can still play
    POOL_EMPTY,       // nothing left to draw
    POOL_NOT_EMPTY    // you must draw before passing
}

sealed class PlayResult {
    data class Played(
        val state: GameState,
        val pointsGained: Int,
        val conquered: List<Position>,
        val extraTurn: Boolean
    ) : PlayResult()

    data class Rejected(val error: MoveError) : PlayResult()
}

sealed class TurnResult {
    data class Done(val state: GameState) : TurnResult()
    data class Rejected(val error: TurnError) : TurnResult()
}

/**
 * Turn flow: play a tile, draw from the pool, or pass.
 * Conquering the PORTAL cell gives the player an extra turn.
 */
object TurnManager {

    /** Validates and applies [move], then passes the turn (unless a portal was conquered). */
    fun playMove(state: GameState, move: Move): PlayResult {
        return when (val result = GameEngine.applyMove(state, move)) {
            is GameEngine.ApplyResult.Rejected -> PlayResult.Rejected(result.error)
            is GameEngine.ApplyResult.Applied -> {
                val extraTurn = result.conquered.any { position ->
                    result.state.cellAt(position)?.type == CellType.PORTAL
                }
                val next = if (extraTurn) result.state else advanceTurn(result.state)
                PlayResult.Played(next, result.pointsGained, result.conquered, extraTurn)
            }
        }
    }

    /** Draws one tile from the pool. Only allowed when the player has no valid move. */
    fun drawTile(state: GameState, playerId: String): TurnResult {
        checkTurn(state, playerId)?.let { return TurnResult.Rejected(it) }
        if (MoveValidator.hasValidMove(state, playerId)) return TurnResult.Rejected(TurnError.HAS_VALID_MOVE)
        if (state.pool.isEmpty()) return TurnResult.Rejected(TurnError.POOL_EMPTY)

        val drawn = state.pool.first()
        val players = state.players.map {
            if (it.id == playerId) it.copy(hand = it.hand + drawn) else it
        }
        return TurnResult.Done(state.copy(players = players, pool = state.pool.drop(1)))
    }

    /** Pass the turn. Only allowed with no valid move and an empty pool. */
    fun passTurn(state: GameState, playerId: String): TurnResult {
        checkTurn(state, playerId)?.let { return TurnResult.Rejected(it) }
        if (MoveValidator.hasValidMove(state, playerId)) return TurnResult.Rejected(TurnError.HAS_VALID_MOVE)
        if (state.pool.isNotEmpty()) return TurnResult.Rejected(TurnError.POOL_NOT_EMPTY)
        return TurnResult.Done(advanceTurn(state))
    }

    /** Next player in order; the round counter goes up when play returns to the first player. */
    fun advanceTurn(state: GameState): GameState {
        val currentIndex = state.players.indexOfFirst { it.id == state.currentPlayerId }
        val nextIndex = (currentIndex + 1) % state.players.size
        val newRound = if (nextIndex == 0) state.round + 1 else state.round
        return state.copy(currentPlayerId = state.players[nextIndex].id, round = newRound)
    }

    private fun checkTurn(state: GameState, playerId: String): TurnError? = when {
        state.status != GameStatus.PLAYING -> TurnError.GAME_NOT_ACTIVE
        playerId != state.currentPlayerId -> TurnError.NOT_YOUR_TURN
        else -> null
    }
}