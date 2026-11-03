package com.example.gamedomino.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class TurnManagerTest {

    private fun stateWith(
        hand: List<Tile>,
        placed: Map<Position, Int> = emptyMap(),
        pool: List<Tile>? = null
    ): GameState {
        val base = GameFactory.newGame(listOf("p1" to "Ana", "p2" to "Luis"), Random(1))
        val board = base.board.map { cell ->
            placed[cell.position]?.let { cell.copy(pips = it, ownerId = "p2") } ?: cell
        }
        return base.copy(
            board = board,
            players = listOf(base.players[0].copy(hand = hand), base.players[1]),
            pool = pool ?: base.pool
        )
    }

    /** p1 holds only 5-6 while the board shows a 3: no valid move. */
    private fun stuckState(pool: List<Tile>? = null) =
        stateWith(listOf(Tile(5, 6)), mapOf(Position(2, 2) to 3), pool)

    @Test
    fun `playing a tile passes the turn to the next player`() {
        val state = stateWith(listOf(Tile(1, 2)))
        val result = TurnManager.playMove(state, Move("p1", Tile(1, 2), Position(0, 2), Position(0, 3)))
        result as PlayResult.Played
        assertEquals("p2", result.state.currentPlayerId)
        assertEquals(1, result.state.round)
        assertEquals(false, result.extraTurn)
    }

    @Test
    fun `round goes up when play returns to the first player`() {
        val state = stateWith(listOf(Tile(1, 2))).copy(currentPlayerId = "p2")
        val next = TurnManager.advanceTurn(state)
        assertEquals("p1", next.currentPlayerId)
        assertEquals(2, next.round)
    }

    @Test
    fun `conquering the portal gives an extra turn`() {
        val state = stateWith(listOf(Tile(1, 2)))
        val result = TurnManager.playMove(state, Move("p1", Tile(1, 2), Position(2, 2), Position(2, 3)))
        result as PlayResult.Played
        assertTrue(result.extraTurn)
        assertEquals("p1", result.state.currentPlayerId)
        assertEquals(1, result.state.round)
    }

    @Test
    fun `invalid move is rejected and the turn does not change`() {
        val state = stateWith(listOf(Tile(1, 2)))
        val result = TurnManager.playMove(state, Move("p2", Tile(1, 2), Position(0, 2), Position(0, 3)))
        assertEquals(PlayResult.Rejected(MoveError.NOT_YOUR_TURN), result)
    }

    @Test
    fun `player with no valid move draws one tile from the pool`() {
        val state = stuckState()
        val result = TurnManager.drawTile(state, "p1") as TurnResult.Done
        assertEquals(2, result.state.playerById("p1")!!.hand.size)
        assertEquals(state.pool.size - 1, result.state.pool.size)
        assertEquals(state.pool.first(), result.state.playerById("p1")!!.hand.last())
        assertEquals("p1", result.state.currentPlayerId)
    }

    @Test
    fun `cannot draw while a valid move exists`() {
        val state = stateWith(listOf(Tile(3, 6)), mapOf(Position(2, 2) to 3))
        assertEquals(TurnResult.Rejected(TurnError.HAS_VALID_MOVE), TurnManager.drawTile(state, "p1"))
    }

    @Test
    fun `cannot draw from an empty pool`() {
        val state = stuckState(pool = emptyList())
        assertEquals(TurnResult.Rejected(TurnError.POOL_EMPTY), TurnManager.drawTile(state, "p1"))
    }

    @Test
    fun `cannot draw out of turn`() {
        val state = stuckState()
        assertEquals(TurnResult.Rejected(TurnError.NOT_YOUR_TURN), TurnManager.drawTile(state, "p2"))
    }

    @Test
    fun `pass is allowed only with no moves and an empty pool`() {
        val result = TurnManager.passTurn(stuckState(pool = emptyList()), "p1") as TurnResult.Done
        assertEquals("p2", result.state.currentPlayerId)
    }

    @Test
    fun `cannot pass while the pool still has tiles`() {
        assertEquals(TurnResult.Rejected(TurnError.POOL_NOT_EMPTY), TurnManager.passTurn(stuckState(), "p1"))
    }

    @Test
    fun `cannot pass while a valid move exists`() {
        val state = stateWith(listOf(Tile(3, 6)), mapOf(Position(2, 2) to 3), pool = emptyList())
        assertEquals(TurnResult.Rejected(TurnError.HAS_VALID_MOVE), TurnManager.passTurn(state, "p1"))
    }

    @Test
    fun `passing on a full block ends the game instead of looping`() {
        // Both hands are stuck and the pool is empty: the only action is to pass.
        // The old bug let players pass forever; now the game must finish.
        val stuck = stuckState(pool = emptyList())
            .let { s -> s.copy(players = listOf(s.players[0], s.players[1].copy(hand = listOf(Tile(4, 4))))) }
        val result = TurnManager.passTurn(stuck, "p1") as TurnResult.Done
        assertEquals(GameStatus.FINISHED, result.state.status)
    }

    @Test
    fun `playing the last tile ends the game by domino`() {
        // p1 holds a single tile; after playing it the hand is empty -> domino.
        val state = stateWith(listOf(Tile(1, 2)))
            .let { s -> s.copy(players = listOf(s.players[0].copy(hand = listOf(Tile(1, 2))), s.players[1])) }
        val result = TurnManager.playMove(state, Move("p1", Tile(1, 2), Position(0, 2), Position(0, 3)))
        result as PlayResult.Played
        assertTrue(result.state.playerById("p1")!!.hand.isEmpty())
        assertEquals(GameStatus.FINISHED, result.state.status)
    }

    @Test
    fun `a normal pass does not end the game when another player can still move`() {
        // p1 is stuck, but p2 can play 3-6 on the board 3. Pool empty so p1 may pass.
        val state = stateWith(listOf(Tile(5, 6)), mapOf(Position(2, 2) to 3), pool = emptyList())
            .let { s -> s.copy(players = listOf(s.players[0], s.players[1].copy(hand = listOf(Tile(3, 6))))) }
        val result = TurnManager.passTurn(state, "p1") as TurnResult.Done
        assertEquals(GameStatus.PLAYING, result.state.status)
        assertEquals("p2", result.state.currentPlayerId)
    }
}