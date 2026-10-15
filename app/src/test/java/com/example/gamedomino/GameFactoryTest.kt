package com.example.gamedomino

import com.example.gamedomino.model.GameFactory
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.random.Random

class GameFactoryTest {

    private val players = listOf("p1" to "Ana", "p2" to "Luis")

    @Test
    fun `full set has 28 distinct tiles`() {
        assertEquals(28, GameFactory.fullSet().toSet().size)
    }

    @Test
    fun `each player gets 7 tiles and the pool keeps 14`() {
        val game = GameFactory.newGame(players, Random(1))
        assertEquals(7, game.players[0].hand.size)
        assertEquals(7, game.players[1].hand.size)
        assertEquals(14, game.pool.size)
    }

    @Test
    fun `board has 25 cells and starts without owners`() {
        val game = GameFactory.newGame(players, Random(1))
        assertEquals(25, game.board.size)
        assertEquals(true, game.board.all { it.ownerId == null })
    }
}