package com.example.gamedomino.model

/**
 * Applies a validated move to the game: places the tile, conquers cells and updates the score.
 * Pure Kotlin: it never changes the state it receives, it returns a new one.
 */
object GameEngine {
    const val CONQUEST_POINTS = 1
    const val BONUS_POINTS = 3
    const val TREASURE_POINTS = 5
    const val TRAP_PENALTY = 2
    const val FORTRESS_EXTRA_POINTS = 2
    const val FORTRESS_HITS_NEEDED = 2

    sealed class ApplyResult {
        data class Applied(
            val state: GameState,
            val pointsGained: Int,
            val conquered: List<Position>
        ) : ApplyResult()

        data class Rejected(val error: MoveError) : ApplyResult()
    }

    fun applyMove(state: GameState, move: Move): ApplyResult {
        val validation = MoveValidator.validate(state, move)
        if (validation is MoveResult.Invalid) return ApplyResult.Rejected(validation.error)

        val cells = state.board.toMutableList()
        val conquered = mutableListOf<Position>()
        val halves = listOf(move.first to move.tile.a, move.second to move.tile.b)

        // 1) Put the pips on both cells; ordinary cells are conquered right away.
        for ((position, pips) in halves) {
            val index = indexOf(position)
            val cell = cells[index].copy(pips = pips)
            if (cell.type == CellType.FORTRESS) {
                cells[index] = cell
            } else {
                cells[index] = cell.copy(ownerId = move.playerId)
                conquered.add(position)
            }
        }

        // 2) A fortress gets ONE hit per move if a half lands on it or next to it.
        val touched = halves
            .flatMap { (position, _) -> listOf(position) + neighborsOf(position) }
            .filter { it.row in 0 until BoardFactory.SIZE && it.col in 0 until BoardFactory.SIZE }
            .distinct()
        for (position in touched) {
            val index = indexOf(position)
            val cell = cells[index]
            if (cell.type != CellType.FORTRESS || cell.ownerId != null) continue
            val hits = cell.fortressHits + 1
            if (hits >= FORTRESS_HITS_NEEDED) {
                cells[index] = cell.copy(fortressHits = hits, ownerId = move.playerId)
                conquered.add(position)
            } else {
                cells[index] = cell.copy(fortressHits = hits)
            }
        }

        // 3) Score: base point per conquered cell plus the special-cell effect.
        val points = conquered.sumOf { pointsFor(cells[indexOf(it)].type) }

        val players = state.players.map { player ->
            if (player.id != move.playerId) player
            else player.copy(
                hand = removeOne(player.hand, move.tile),
                score = (player.score + points).coerceAtLeast(0)
            )
        }
        return ApplyResult.Applied(state.copy(board = cells, players = players), points, conquered)
    }

    private fun pointsFor(type: CellType): Int = CONQUEST_POINTS + when (type) {
        CellType.BONUS -> BONUS_POINTS
        CellType.TREASURE -> TREASURE_POINTS
        CellType.TRAP -> -TRAP_PENALTY
        CellType.FORTRESS -> FORTRESS_EXTRA_POINTS
        else -> 0
    }

    private fun removeOne(hand: List<Tile>, tile: Tile): List<Tile> {
        val index = hand.indexOfFirst { it.sameAs(tile) }
        return if (index < 0) hand else hand.filterIndexed { i, _ -> i != index }
    }

    private fun indexOf(p: Position) = p.row * BoardFactory.SIZE + p.col

    private fun neighborsOf(p: Position): List<Position> = listOf(
        Position(p.row - 1, p.col), Position(p.row + 1, p.col),
        Position(p.row, p.col - 1), Position(p.row, p.col + 1)
    )
}