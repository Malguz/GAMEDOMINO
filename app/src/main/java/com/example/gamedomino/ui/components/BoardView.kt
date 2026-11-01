package com.example.gamedomino.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.gamedomino.model.BoardFactory
import com.example.gamedomino.model.Cell
import com.example.gamedomino.model.Player
import com.example.gamedomino.model.Position

private val playerPalette = listOf(
    Color(0xFF42A5F5), // blue
    Color(0xFFEF5350), // red
    Color(0xFF66BB6A), // green
    Color(0xFFFFCA28)  // amber
)

/** Colour assigned to a player (by seat order), or null if there is no such player. */
fun playerColor(players: List<Player>, playerId: String?): Color? {
    val index = players.indexOfFirst { it.id == playerId }
    return if (index < 0) null else playerPalette[index % playerPalette.size]
}

/** The 5x5 board. Always square: give it a width and it sizes its own height. */
@Composable
fun BoardView(
    board: List<Cell>,
    players: List<Player>,
    highlighted: Set<Position>,
    firstCell: Position?,
    modifier: Modifier = Modifier,
    onCellClick: (Position) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth().aspectRatio(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (row in 0 until BoardFactory.SIZE) {
            Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (col in 0 until BoardFactory.SIZE) {
                    val position = Position(row, col)
                    val cell = board[row * BoardFactory.SIZE + col]
                    CellView(
                        cell = cell,
                        ownerColor = playerColor(players, cell.ownerId),
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        highlighted = position in highlighted,
                        isFirstSelected = position == firstCell,
                        onClick = { onCellClick(position) }
                    )
                }
            }
        }
    }
}