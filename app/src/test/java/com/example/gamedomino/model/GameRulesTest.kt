package com.example.gamedomino.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameRulesTest {

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

    // ---- end-game detection ----

    @Test
    fun `domino when a hand is empty`() {
        val state = stateWith(listOf(Tile(5, 6))).copy(
            players = listOf(
                Player("p1", "Ana", hand = emptyList()),
                Player("p2", "Luis", hand = listOf(Tile(0, 0)))
            )
        )
        assertTrue(GameRules.isDomino(state))
        assertEquals(GameStatus.FINISHED, GameRules.checkEndGame(state).status)
    }

    @Test
    fun `block when nobody can move and the pool is empty`() {
        // p1 holds only 5-6, p2 holds only 4-4; board shows a 3; pool empty: nobody can play.
        val state = stateWith(listOf(Tile(5, 6)), mapOf(Position(2, 2) to 3), pool = emptyList())
            .let { s ->
                s.copy(players = listOf(s.players[0], s.players[1].copy(hand = listOf(Tile(4, 4)))))
            }
        assertTrue(GameRules.isBlocked(state))
        assertEquals(GameStatus.FINISHED, GameRules.checkEndGame(state).status)
    }

    @Test
    fun `not blocked while the pool still has tiles`() {
        val state = stateWith(listOf(Tile(5, 6)), mapOf(Position(2, 2) to 3))
        assertFalse(GameRules.isBlocked(state))
        assertEquals(GameStatus.PLAYING, GameRules.checkEndGame(state).status)
    }

    @Test
    fun `still playing when a player has a valid move`() {
        val state = stateWith(listOf(Tile(3, 6)), mapOf(Position(2, 2) to 3), pool = emptyList())
        assertFalse(GameRules.isBlocked(state))
        assertFalse(GameRules.isDomino(state))
        assertEquals(GameStatus.PLAYING, GameRules.checkEndGame(state).status)
    }

    @Test
    fun `checkEndGame leaves an already finished game as is`() {
        val state = stateWith(listOf(Tile(1, 2))).copy(status = GameStatus.FINISHED)
        assertEquals(GameStatus.FINISHED, GameRules.checkEndGame(state).status)
    }

    // ---- winner + tiebreak ----

    @Test
    fun `winner is the player with the highest score`() {
        val state = stateWith(listOf(Tile(1, 2))).copy(
            players = listOf(
                Player("p1", "Ana", hand = listOf(Tile(6, 6)), score = 10),
                Player("p2", "Luis", hand = listOf(Tile(0, 0)), score = 4)
            )
        )
        val result = GameRules.winnerOf(state)
        assertNotNull(result)
        assertEquals("p1", result!!.winner.id)
        assertEquals(10, result.points)
        assertFalse(result.wonByTiebreak)
    }

    @Test
    fun `tie on score is broken by fewer pips in hand`() {
        val state = stateWith(listOf(Tile(1, 2))).copy(
            players = listOf(
                Player("p1", "Ana", hand = listOf(Tile(6, 6)), score = 7),  // 12 pips
                Player("p2", "Luis", hand = listOf(Tile(1, 0)), score = 7)   // 1 pip -> wins
            )
        )
        val result = GameRules.winnerOf(state)!!
        assertEquals("p2", result.winner.id)
        assertEquals(7, result.points)
        assertTrue(result.wonByTiebreak)
    }

    @Test
    fun `territories count the cells owned by the winner`() {
        // Put 3 cells owned by p1 on the board, then make p1 the clear winner.
        val base = stateWith(listOf(Tile(1, 2)))
        val board = base.board.mapIndexed { index, cell ->
            if (index < 3) cell.copy(ownerId = "p1", pips = 0) else cell
        }
        val state = base.copy(
            board = board,
            players = listOf(
                Player("p1", "Ana", hand = emptyList(), score = 10),
                Player("p2", "Luis", hand = listOf(Tile(0, 0)), score = 4)
            )
        )
        val result = GameRules.winnerOf(state)!!
        assertEquals("p1", result.winner.id)
        assertEquals(3, result.territories)
    }
}
