package com.example.gamedomino.data

import com.example.gamedomino.model.GameFactory
import com.example.gamedomino.model.GameStatus
import com.example.gamedomino.model.Move
import com.example.gamedomino.model.Position
import com.example.gamedomino.model.Tile
import com.example.gamedomino.model.TurnManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.random.Random

class GameStateCodecTest {

    @Test
    fun `encode then decode returns an equal game state`() {
        val state = GameFactory.newGame(listOf("uidA" to "Ana", "uidB" to "Luis"), Random(1))
        val json = GameStateCodec.encode(state)
        val back = GameStateCodec.decode(json)
        assertEquals(state, back)
    }

    @Test
    fun `round trip keeps a played move`() {
        val start = GameFactory.newGame(listOf("uidA" to "Ana", "uidB" to "Luis"), Random(1))
        // Play the first tile of player A on the empty board (any adjacent pair is valid).
        val tile = start.players.first().hand.first()
        val result = TurnManager.playMove(start, Move("uidA", tile, Position(0, 0), Position(0, 1)))
        val state = (result as com.example.gamedomino.model.PlayResult.Played).state
        val back = GameStateCodec.decode(GameStateCodec.encode(state))
        assertEquals(state, back)
        assertEquals(GameStatus.PLAYING, back!!.status)
    }

    @Test
    fun `decode of empty or blank text is null`() {
        assertNull(GameStateCodec.decode(null))
        assertNull(GameStateCodec.decode(""))
        assertNull(GameStateCodec.decode("   "))
    }

    @Test
    fun `decode of corrupt text is null`() {
        assertNull(GameStateCodec.decode("{not valid json"))
        assertNull(GameStateCodec.decode("12345"))
    }
}
