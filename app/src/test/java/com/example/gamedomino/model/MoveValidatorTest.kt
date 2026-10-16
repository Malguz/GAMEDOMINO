package com.example.gamedomino.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MoveValidatorTest {

    /** Player "p1" (current turn) holds [hand]; [placed] maps a position to the pips already on it. */
    private fun stateWith(hand: List<Tile>, placed: Map<Position, Int> = emptyMap()): GameState {
        val base = GameFactory.newGame(listOf("p1" to "Ana", "p2" to "Luis"), Random(1))
        val board = base.board.map { cell ->
            placed[cell.position]?.let { cell.copy(pips = it, ownerId = "p2") } ?: cell
        }
        return base.copy(
            board = board,
            players = listOf(base.players[0].copy(hand = hand), base.players[1])
        )
    }

    private fun move(tile: Tile, first: Position, second: Position, playerId: String = "p1") =
        Move(playerId, tile, first, second)

    private fun invalid(error: MoveError) = MoveResult.Invalid(error)

    @Test
    fun `first tile can go anywhere`() {
        val state = stateWith(listOf(Tile(3, 4)))
        val result = MoveValidator.validate(state, move(Tile(3, 4), Position(0, 0), Position(0, 1)))
        assertEquals(MoveResult.Valid, result)
    }

    @Test
    fun `tile can be placed flipped`() {
        val state = stateWith(listOf(Tile(3, 4)))
        val result = MoveValidator.validate(state, move(Tile(4, 3), Position(0, 0), Position(0, 1)))
        assertEquals(MoveResult.Valid, result)
    }

    @Test
    fun `finished game rejects moves`() {
        val state = stateWith(listOf(Tile(3, 4))).copy(status = GameStatus.FINISHED)
        val result = MoveValidator.validate(state, move(Tile(3, 4), Position(0, 0), Position(0, 1)))
        assertEquals(invalid(MoveError.GAME_NOT_ACTIVE), result)
    }

    @Test
    fun `other player cannot move out of turn`() {
        val state = stateWith(listOf(Tile(3, 4)))
        val result = MoveValidator.validate(state, move(Tile(3, 4), Position(0, 0), Position(0, 1), "p2"))
        assertEquals(invalid(MoveError.NOT_YOUR_TURN), result)
    }

    @Test
    fun `tile that is not in hand is rejected`() {
        val state = stateWith(listOf(Tile(3, 4)))
        val result = MoveValidator.validate(state, move(Tile(5, 6), Position(0, 0), Position(0, 1)))
        assertEquals(invalid(MoveError.TILE_NOT_IN_HAND), result)
    }

    @Test
    fun `cells outside the board are rejected`() {
        val state = stateWith(listOf(Tile(3, 4)))
        val result = MoveValidator.validate(state, move(Tile(3, 4), Position(0, 4), Position(0, 5)))
        assertEquals(invalid(MoveError.OUT_OF_BOARD), result)
    }

    @Test
    fun `diagonal cells are not adjacent`() {
        val state = stateWith(listOf(Tile(3, 4)))
        val result = MoveValidator.validate(state, move(Tile(3, 4), Position(0, 0), Position(1, 1)))
        assertEquals(invalid(MoveError.CELLS_NOT_ADJACENT), result)
    }

    @Test
    fun `occupied cell is rejected`() {
        val state = stateWith(listOf(Tile(3, 4)), mapOf(Position(2, 2) to 3))
        val result = MoveValidator.validate(state, move(Tile(3, 4), Position(2, 2), Position(2, 3)))
        assertEquals(invalid(MoveError.CELL_OCCUPIED), result)
    }

    @Test
    fun `tile must touch a placed tile after the first move`() {
        val state = stateWith(listOf(Tile(3, 4)), mapOf(Position(0, 0) to 3, Position(0, 1) to 4))
        val result = MoveValidator.validate(state, move(Tile(3, 4), Position(4, 3), Position(4, 4)))
        assertEquals(invalid(MoveError.MUST_TOUCH_PLACED_TILE), result)
    }

    @Test
    fun `touching numbers must match`() {
        val state = stateWith(listOf(Tile(5, 6)), mapOf(Position(2, 2) to 3))
        val result = MoveValidator.validate(state, move(Tile(5, 6), Position(2, 3), Position(2, 4)))
        assertEquals(invalid(MoveError.PIPS_DO_NOT_MATCH), result)
    }

    @Test
    fun `matching numbers are accepted`() {
        val state = stateWith(listOf(Tile(3, 6)), mapOf(Position(2, 2) to 3))
        val result = MoveValidator.validate(state, move(Tile(3, 6), Position(2, 3), Position(2, 4)))
        assertEquals(MoveResult.Valid, result)
    }

    @Test
    fun `hasValidMove is false when nothing matches`() {
        val state = stateWith(listOf(Tile(5, 6)), mapOf(Position(2, 2) to 3))
        assertFalse(MoveValidator.hasValidMove(state, "p1"))
    }

    @Test
    fun `hasValidMove is true when a tile matches`() {
        val state = stateWith(listOf(Tile(3, 6)), mapOf(Position(2, 2) to 3))
        assertTrue(MoveValidator.hasValidMove(state, "p1"))
    }

    @Test
    fun `every move returned by validMoves passes validate`() {
        val state = stateWith(listOf(Tile(3, 6), Tile(6, 6)), mapOf(Position(2, 2) to 3, Position(2, 1) to 6))
        val moves = MoveValidator.validMoves(state, "p1")
        assertTrue(moves.isNotEmpty())
        assertTrue(moves.all { MoveValidator.validate(state, it) == MoveResult.Valid })
    }
}