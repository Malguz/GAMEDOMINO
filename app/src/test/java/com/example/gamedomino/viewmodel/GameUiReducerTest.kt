package com.example.gamedomino.model

import com.example.gamedomino.model.GameFactory
import com.example.gamedomino.model.Position
import com.example.gamedomino.model.Tile
import com.example.gamedomino.model.TurnError
import com.example.gamedomino.model.cellAt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameUiReducerTest {

    private fun uiWith(
        hand: List<Tile>,
        placed: Map<Position, Int> = emptyMap(),
        myPlayerId: String? = null
    ): GameUiState {
        val base = GameFactory.newGame(listOf("p1" to "Ana", "p2" to "Luis"), Random(1))
        val board = base.board.map { cell ->
            placed[cell.position]?.let { cell.copy(pips = it, ownerId = "p2") } ?: cell
        }
        val game = base.copy(
            board = board,
            players = listOf(base.players[0].copy(hand = hand), base.players[1])
        )
        return GameUiState(game = game, myPlayerId = myPlayerId)
    }

    private fun GameUiState.pipsAt(row: Int, col: Int) = game.cellAt(Position(row, col))!!.pips

    @Test
    fun `selecting a tile highlights every cell on an empty board`() {
        val ui = GameUiReducer.selectTile(uiWith(listOf(Tile(1, 2))), Tile(1, 2))
        assertEquals(Tile(1, 2), ui.selectedTile)
        assertEquals(25, ui.highlighted.size)
    }

    @Test
    fun `tile that is not in the hand is ignored`() {
        val start = uiWith(listOf(Tile(1, 2)))
        assertEquals(start, GameUiReducer.selectTile(start, Tile(5, 5)))
    }

    @Test
    fun `selecting the same tile again deselects it`() {
        val selected = GameUiReducer.selectTile(uiWith(listOf(Tile(1, 2))), Tile(1, 2))
        val ui = GameUiReducer.selectTile(selected, Tile(2, 1))
        assertNull(ui.selectedTile)
        assertTrue(ui.highlighted.isEmpty())
    }

    @Test
    fun `first tap keeps only the neighbours as partners`() {
        val selected = GameUiReducer.selectTile(uiWith(listOf(Tile(1, 2))), Tile(1, 2))
        val ui = GameUiReducer.tapCell(selected, Position(0, 0))
        assertEquals(Position(0, 0), ui.firstCell)
        assertEquals(setOf(Position(0, 1), Position(1, 0)), ui.highlighted)
    }

    @Test
    fun `second tap plays the tile and passes the turn`() {
        var ui = GameUiReducer.selectTile(uiWith(listOf(Tile(1, 2))), Tile(1, 2))
        ui = GameUiReducer.tapCell(ui, Position(0, 2))
        ui = GameUiReducer.tapCell(ui, Position(0, 3))
        assertEquals(1, ui.pipsAt(0, 2))
        assertEquals(2, ui.pipsAt(0, 3))
        assertEquals("p2", ui.game.currentPlayerId)
        assertNull(ui.selectedTile)
        assertEquals(GameEvent.Scored(2, false), ui.lastEvent)
    }

    @Test
    fun `flip puts the other half on the first tapped cell`() {
        var ui = GameUiReducer.selectTile(uiWith(listOf(Tile(1, 2))), Tile(1, 2))
        ui = GameUiReducer.flip(ui)
        ui = GameUiReducer.tapCell(ui, Position(0, 2))
        ui = GameUiReducer.tapCell(ui, Position(0, 3))
        assertEquals(2, ui.pipsAt(0, 2))
        assertEquals(1, ui.pipsAt(0, 3))
    }

    @Test
    fun `second cell can be to the left of the first one`() {
        var ui = GameUiReducer.selectTile(uiWith(listOf(Tile(3, 6)), mapOf(Position(2, 2) to 3)), Tile(3, 6))
        ui = GameUiReducer.tapCell(ui, Position(2, 1))
        ui = GameUiReducer.tapCell(ui, Position(2, 0))
        assertEquals(3, ui.pipsAt(2, 1))
        assertEquals(6, ui.pipsAt(2, 0))
    }

    @Test
    fun `tapping a cell that is not highlighted does nothing`() {
        val selected = GameUiReducer.selectTile(uiWith(listOf(Tile(3, 6)), mapOf(Position(2, 2) to 3)), Tile(3, 6))
        val ui = GameUiReducer.tapCell(selected, Position(0, 0))
        assertNull(ui.firstCell)
    }

    @Test
    fun `tapping the first cell again cancels it`() {
        var ui = GameUiReducer.selectTile(uiWith(listOf(Tile(1, 2))), Tile(1, 2))
        ui = GameUiReducer.tapCell(ui, Position(0, 0))
        ui = GameUiReducer.tapCell(ui, Position(0, 0))
        assertNull(ui.firstCell)
        assertEquals(25, ui.highlighted.size)
    }

    @Test
    fun `a player cannot act when it is not their turn`() {
        val start = uiWith(listOf(Tile(1, 2)), myPlayerId = "p2")
        assertEquals(start, GameUiReducer.selectTile(start, Tile(1, 2)))
        assertEquals(start, GameUiReducer.drawTile(start))
    }

    @Test
    fun `draw adds a tile when there is no valid move`() {
        val start = uiWith(listOf(Tile(5, 6)), mapOf(Position(2, 2) to 3))
        assertTrue(!start.canPlay)
        val ui = GameUiReducer.drawTile(start)
        assertEquals(2, ui.currentPlayer.hand.size)
        assertEquals(GameEvent.TileDrawn, ui.lastEvent)
    }

    @Test
    fun `draw is rejected while a valid move exists`() {
        val start = uiWith(listOf(Tile(3, 6)), mapOf(Position(2, 2) to 3))
        val ui = GameUiReducer.drawTile(start)
        assertEquals(GameEvent.TurnRejected(TurnError.HAS_VALID_MOVE), ui.lastEvent)
        assertEquals(1, ui.currentPlayer.hand.size)
    }

    @Test
    fun `pass moves to the next player when stuck with an empty pool`() {
        val start = uiWith(listOf(Tile(5, 6)), mapOf(Position(2, 2) to 3))
        val stuck = start.copy(game = start.game.copy(pool = emptyList()))
        val ui = GameUiReducer.passTurn(stuck)
        assertEquals("p2", ui.game.currentPlayerId)
        assertEquals(GameEvent.Passed, ui.lastEvent)
    }
}