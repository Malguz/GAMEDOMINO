package com.example.gamedomino.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gamedomino.model.GameFactory
import com.example.gamedomino.model.Move
import com.example.gamedomino.model.PlayResult
import com.example.gamedomino.model.Position
import com.example.gamedomino.model.Tile
import com.example.gamedomino.model.TurnManager
import com.example.gamedomino.ui.theme.GameDominoTheme
import kotlin.random.Random

@Preview(showBackground = true, widthDp = 380, heightDp = 640)
@Composable
private fun BoardPreview() {
    GameDominoTheme(darkTheme = false) {
        val start = GameFactory.newGame(listOf("p1" to "Ana", "p2" to "Luis"), Random(1))
        val tile = start.players[0].hand.first()
        val played = (TurnManager.playMove(
            start, Move("p1", tile, Position(1, 1), Position(1, 2))
        ) as PlayResult.Played).state

        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TileView(Tile(6, 3))
                TileView(Tile(0, 5), selected = true)
                TileView(Tile(4, 4), vertical = true)
            }
            BoardView(
                board = played.board,
                players = played.players,
                highlighted = setOf(Position(0, 1), Position(2, 1)),
                firstCell = Position(1, 1),
                onCellClick = {}
            )
        }
    }
}