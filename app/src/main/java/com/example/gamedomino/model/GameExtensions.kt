package com.example.gamedomino.model


/** Returns the cell at [position], or null if the position is outside the board. */
fun GameState.cellAt(position: Position): Cell? {
    val inside = position.row in 0 until BoardFactory.SIZE && position.col in 0 until BoardFactory.SIZE
    return if (inside) board.getOrNull(position.row * BoardFactory.SIZE + position.col) else null
}

fun GameState.playerById(id: String): Player? = players.firstOrNull { it.id == id }

/** True when both tiles are the same domino, whichever way round they are written. */
fun Tile.sameAs(other: Tile): Boolean =
    (a == other.a && b == other.b) || (a == other.b && b == other.a)