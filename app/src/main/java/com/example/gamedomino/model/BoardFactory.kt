package com.example.gamedomino.model

/** Builds the fixed 5x5 board. Legend: N normal, B bonus, T treasure, X trap, F fortress, P portal. */
object BoardFactory {
    const val SIZE = 5

    private val LAYOUT = listOf(
        "NBNNT",
        "NNFNN",
        "XNPNX",
        "NNFNN",
        "TNNBN"
    )

    fun createInitialBoard(): List<Cell> =
        LAYOUT.flatMapIndexed { row, line ->
            line.mapIndexed { col, symbol ->
                Cell(position = Position(row, col), type = symbolToType(symbol))
            }
        }

    private fun symbolToType(symbol: Char): CellType = when (symbol) {
        'B' -> CellType.BONUS
        'T' -> CellType.TREASURE
        'X' -> CellType.TRAP
        'F' -> CellType.FORTRESS
        'P' -> CellType.PORTAL
        else -> CellType.NORMAL
    }
}