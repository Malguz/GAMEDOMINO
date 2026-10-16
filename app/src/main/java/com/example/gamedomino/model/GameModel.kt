package com.example.gamedomino.model

enum class CellType { NORMAL, BONUS, TREASURE, TRAP, FORTRESS, PORTAL }

data class Position(val row: Int, val col: Int)

/** A domino tile; each half shows 0..6 pips. */
data class Tile(val a: Int, val b: Int)

data class Cell(
    val position: Position,
    val type: CellType = CellType.NORMAL,
    val ownerId: String? = null,   // null = not conquered yet
    val fortressHits: Int = 0,     // a FORTRESS needs 2 conquests
    val pips: Int? = null          // pip value of the half-tile placed here
)

/** Places [tile] on two adjacent cells: [first] gets tile.a and [second] gets tile.b. */
data class Move(val playerId: String, val tile: Tile, val first: Position, val second: Position)

data class Player(
    val id: String,
    val name: String,
    val hand: List<Tile> = emptyList(),
    val score: Int = 0
)

enum class GameStatus { WAITING, PLAYING, FINISHED }

data class GameState(
    val board: List<Cell>,         // flat list, 5x5 = 25 cells
    val players: List<Player>,
    val pool: List<Tile>,
    val currentPlayerId: String,
    val round: Int = 1,
    val maxRounds: Int = 20,
    val status: GameStatus = GameStatus.PLAYING
)