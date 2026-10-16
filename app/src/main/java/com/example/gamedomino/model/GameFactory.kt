package com.example.gamedomino.model

import com.example.gamedomino.model.BoardFactory.createInitialBoard
import kotlin.random.Random

/** Creates the double-six set and deals a new game. */
object GameFactory {
    const val HAND_SIZE = 7

    /** The 28 tiles of a double-six set. */
    fun fullSet(): List<Tile> =
        (0..6).flatMap { a -> (a..6).map { b -> Tile(a, b) } }

    /** [players] is a list of (id, name). A fixed [random] makes the deal reproducible in tests. */
    fun newGame(players: List<Pair<String, String>>, random: Random = Random.Default): GameState {
        val shuffled = fullSet().shuffled(random)
        val dealt = players.mapIndexed { index, (id, name) ->
            Player(id = id, name = name, hand = shuffled.drop(index * HAND_SIZE).take(HAND_SIZE))
        }
        return GameState(
            board = createInitialBoard(),
            players = dealt,
            pool = shuffled.drop(players.size * HAND_SIZE),
            currentPlayerId = dealt.first().id
        )
    }
}