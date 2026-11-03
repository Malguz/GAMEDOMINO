package com.example.gamedomino.model

import kotlinx.serialization.Serializable

@Serializable
enum class CellType { NORMAL, BONUS, TREASURE, TRAP, FORTRESS, PORTAL }

@Serializable
data class Position(val row: Int, val col: Int)

/** A domino tile; each half shows 0..6 pips. */
@Serializable
data class Tile(val a: Int, val b: Int)

@Serializable
data class Cell(
    val position: Position,
    val type: CellType = CellType.NORMAL,
    val ownerId: String? = null,   // null = not conquered yet
    val fortressHits: Int = 0,     // a FORTRESS needs 2 conquests
    val pips: Int? = null          // pip value of the half-tile placed here
)

/** Places [tile] on two adjacent cells: [first] gets tile.a and [second] gets tile.b. */
@Serializable
data class Move(val playerId: String, val tile: Tile, val first: Position, val second: Position)

@Serializable
data class Player(
    val id: String,
    val name: String,
    val hand: List<Tile> = emptyList(),
    val score: Int = 0
)

@Serializable
enum class GameStatus { WAITING, PLAYING, FINISHED }

@Serializable
data class GameState(
    val board: List<Cell>,         // flat list, 5x5 = 25 cells
    val players: List<Player>,
    val pool: List<Tile>,
    val currentPlayerId: String,
    val round: Int = 1,
    val maxRounds: Int = 20,
    val status: GameStatus = GameStatus.PLAYING
)