package com.example.gamedomino.model

/** The result of a finished game: who won and why. */
data class GameResult(
    val winner: Player,
    val points: Int,          // winner score
    val territories: Int,     // cells owned by the winner
    val wonByTiebreak: Boolean // true when the score was a tie and pips broke it
)

/*End-of-game rules. */
object GameRules {

    /** True when some player has no tile left in hand. */
    fun isDomino(state: GameState): Boolean =
        state.players.any { it.hand.isEmpty() }

    /** True when the pool is empty and no player has a valid move. */
    fun isBlocked(state: GameState): Boolean =
        state.pool.isEmpty() && state.players.none { MoveValidator.hasValidMove(state, it.id) }

    /** Returns the same state, but marked FINISHED when the game is over. */
    fun checkEndGame(state: GameState): GameState {
        if (state.status != GameStatus.PLAYING) return state
        return if (isDomino(state) || isBlocked(state)) {
            state.copy(status = GameStatus.FINISHED)
        } else {
            state
        }
    }

    /** Sum of pips of every tile still in a player's hand. */
    fun pipsInHand(player: Player): Int =
        player.hand.sumOf { it.a + it.b }

    /** Number of board cells owned by [playerId]. */
    fun territoriesOf(state: GameState, playerId: String): Int =
        state.board.count { it.ownerId == playerId }

    /**
     * Finds the winner of a finished game.

     */
    fun winnerOf(state: GameState): GameResult? {
        if (state.players.isEmpty()) return null

        val topScore = state.players.maxOf { it.score }
        val leaders = state.players.filter { it.score == topScore }

        val winner: Player
        val wonByTiebreak: Boolean
        if (leaders.size == 1) {
            winner = leaders.first()
            wonByTiebreak = false
        } else {
            // Tie on score: fewer pips in hand wins.
            winner = leaders.minBy { pipsInHand(it) }
            wonByTiebreak = true
        }

        return GameResult(
            winner = winner,
            points = winner.score,
            territories = territoriesOf(state, winner.id),
            wonByTiebreak = wonByTiebreak
        )
    }
}
