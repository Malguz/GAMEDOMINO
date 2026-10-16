package com.example.gamedomino.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameEngineTest {

    private fun stateWith(
        hand: List<Tile>,
        placed: Map<Position, Int> = emptyMap(),
        score: Int = 0
    ): GameState {
        val base = GameFactory.newGame(listOf("p1" to "Ana", "p2" to "Luis"), Random(1))
        val board = base.board.map { cell ->
            placed[cell.position]?.let { cell.copy(pips = it, ownerId = "p2") } ?: cell
        }
        return base.copy(
            board = board,
            players = listOf(base.players[0].copy(hand = hand, score = score), base.players[1])
        )
    }

    private fun applied(state: GameState, move: Move) =
        GameEngine.applyMove(state, move) as GameEngine.ApplyResult.Applied

    private fun cell(state: GameState, row: Int, col: Int) = state.cellAt(Position(row, col))!!

    @Test
    fun `normal cells are conquered and give one point each`() {
        val state = stateWith(listOf(Tile(1, 2)))
        val result = applied(state, Move("p1", Tile(1, 2), Position(0, 2), Position(0, 3)))
        assertEquals(2, result.pointsGained)
        assertEquals("p1", cell(result.state, 0, 2).ownerId)
        assertEquals(1, cell(result.state, 0, 2).pips)
        assertEquals(2, cell(result.state, 0, 3).pips)
    }

    @Test
    fun `bonus cell gives 3 extra points`() {
        val state = stateWith(listOf(Tile(1, 2)))
        val result = applied(state, Move("p1", Tile(1, 2), Position(0, 0), Position(0, 1)))
        assertEquals(1 + (1 + GameEngine.BONUS_POINTS), result.pointsGained)
    }

    @Test
    fun `treasure cell gives 5 extra points`() {
        val state = stateWith(listOf(Tile(1, 2)))
        val result = applied(state, Move("p1", Tile(1, 2), Position(0, 3), Position(0, 4)))
        assertEquals(1 + (1 + GameEngine.TREASURE_POINTS), result.pointsGained)
    }

    @Test
    fun `trap cell takes 2 points away`() {
        val state = stateWith(listOf(Tile(1, 2)), score = 10)
        val result = applied(state, Move("p1", Tile(1, 2), Position(2, 0), Position(2, 1)))
        assertEquals((1 - GameEngine.TRAP_PENALTY) + 1, result.pointsGained)
        assertEquals(10, result.state.playerById("p1")!!.score)
    }

    @Test
    fun `score never goes below zero`() {
        val state = stateWith(listOf(Tile(1, 2)), score = 0)
        val result = applied(state, Move("p1", Tile(1, 2), Position(2, 0), Position(2, 1)))
        assertEquals(0, result.pointsGained)
        val trapOnly = applied(stateWith(listOf(Tile(1, 2))), Move("p1", Tile(1, 2), Position(2, 0), Position(1, 0)))
        assertTrue(trapOnly.state.playerById("p1")!!.score >= 0)
    }

    @Test
    fun `fortress needs two hits - first hit does not conquer it`() {
        val state = stateWith(listOf(Tile(1, 2)))
        val result = applied(state, Move("p1", Tile(1, 2), Position(1, 2), Position(1, 3)))
        assertEquals(1, cell(result.state, 1, 2).fortressHits)
        assertNull(cell(result.state, 1, 2).ownerId)
        assertEquals(1, result.pointsGained)
    }

    @Test
    fun `fortress is conquered on the second hit`() {
        val state = stateWith(
            listOf(Tile(3, 4)),
            placed = mapOf(Position(1, 2) to 3, Position(1, 3) to 4)
        )
        val withHit = state.copy(
            board = state.board.map {
                if (it.position == Position(1, 2)) it.copy(ownerId = null, fortressHits = 1) else it
            }
        )
        val result = applied(withHit, Move("p1", Tile(3, 4), Position(0, 2), Position(0, 3)))
        assertEquals("p1", cell(result.state, 1, 2).ownerId)
        assertEquals(1 + 1 + (1 + GameEngine.FORTRESS_EXTRA_POINTS), result.pointsGained)
    }

    @Test
    fun `tile leaves the hand and score is added`() {
        val state = stateWith(listOf(Tile(1, 2), Tile(5, 5)))
        val result = applied(state, Move("p1", Tile(2, 1), Position(0, 2), Position(0, 3)))
        val player = result.state.playerById("p1")!!
        assertEquals(listOf(Tile(5, 5)), player.hand)
        assertEquals(2, player.score)
    }

    @Test
    fun `invalid move is rejected and state is untouched`() {
        val state = stateWith(listOf(Tile(1, 2)))
        val result = GameEngine.applyMove(state, Move("p2", Tile(1, 2), Position(0, 2), Position(0, 3)))
        assertEquals(GameEngine.ApplyResult.Rejected(MoveError.NOT_YOUR_TURN), result)
        assertTrue(state.board.all { it.pips == null })
    }
}