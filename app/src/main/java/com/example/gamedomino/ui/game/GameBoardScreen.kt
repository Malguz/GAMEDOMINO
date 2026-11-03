package com.example.gamedomino.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gamedomino.model.GameFactory
import com.example.gamedomino.model.MoveError
import com.example.gamedomino.model.Position
import com.example.gamedomino.model.Tile
import com.example.gamedomino.model.TurnError
import com.example.gamedomino.ui.components.BoardView
import com.example.gamedomino.ui.components.HandView
import com.example.gamedomino.ui.components.ScoreBar
import com.example.gamedomino.ui.theme.GameDominoTheme
import com.example.gamedomino.viewmodel.GameEvent
import com.example.gamedomino.viewmodel.GameUiReducer
import com.example.gamedomino.viewmodel.GameUiState
import com.example.gamedomino.viewmodel.GameViewModel

/** Connects the screen to the ViewModel. All the drawing lives in [GameBoardContent]. */
@Composable
fun GameBoardScreen(viewModel: GameViewModel = viewModel()) {
    val ui by viewModel.uiState.collectAsState()
    GameBoardContent(
        ui = ui,
        onTileSelected = { viewModel.onTileSelected(it) },
        onFlip = { viewModel.onFlip() },
        onCellTapped = { viewModel.onCellTapped(it) },
        onDrawTile = { viewModel.onDrawTile() },
        onPassTurn = { viewModel.onPassTurn() },
        onNewGame = { viewModel.onNewGame() }
    )
}

/** Stateless screen: it only draws [ui] and reports what the user did. */
@Composable
fun GameBoardContent(
    ui: GameUiState,
    onTileSelected: (Tile) -> Unit,
    onFlip: () -> Unit,
    onCellTapped: (Position) -> Unit,
    onDrawTile: () -> Unit,
    onPassTurn: () -> Unit,
    onNewGame: () -> Unit
) {
    val game = ui.game
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScoreBar(
            players = game.players,
            currentPlayerId = game.currentPlayerId,
            round = game.round,
            maxRounds = game.maxRounds,
            poolSize = game.pool.size
        )
        Text(text = turnText(ui), style = MaterialTheme.typography.titleMedium)
        BoardView(
            board = game.board,
            players = game.players,
            highlighted = ui.highlighted,
            firstCell = ui.firstCell,
            onCellClick = onCellTapped
        )
        Text(text = "Mano de ${ui.currentPlayer.name}", style = MaterialTheme.typography.labelLarge)
        HandView(hand = ui.currentPlayer.hand, selected = ui.selectedTile, onTileClick = onTileSelected)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val finished = game.status == com.example.gamedomino.model.GameStatus.FINISHED
            OutlinedButton(onClick = onFlip, enabled = !finished && ui.selectedTile != null) { Text("Voltear") }
            Button(onClick = onDrawTile, enabled = !finished && !ui.canPlay && game.pool.isNotEmpty()) { Text("Robar") }
            OutlinedButton(onClick = onPassTurn, enabled = !finished && !ui.canPlay && game.pool.isEmpty()) { Text("Pasar") }
        }
        ui.lastEvent?.let { Text(text = eventText(it), style = MaterialTheme.typography.bodyLarge) }
        TextButton(onClick = onNewGame) { Text("Nueva partida") }
    }

    ui.result?.let { result ->
        WinnerDialog(result = result, onNewGame = onNewGame)
    }
}


@Composable
private fun WinnerDialog(
    result: com.example.gamedomino.model.GameResult,
    onNewGame: () -> Unit
) {
    Dialog(onDismissRequest = { /* keep it open until a new game is started */ }) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Fin de la partida",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Ganador",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = result.winner.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                ResultRow(label = "Puntos", value = "${result.points}")
                Spacer(Modifier.height(6.dp))
                ResultRow(label = "Territorios conquistados", value = "${result.territories}")
                if (result.wonByTiebreak) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Ganó por desempate: menos fichas en la mano.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(Modifier.height(20.dp))
                Button(onClick = onNewGame, modifier = Modifier.fillMaxWidth()) {
                    Text("Nueva partida")
                }
            }
        }
    }
}

/** One labelled value inside the winner card (label on the left, value on the right). */
@Composable
private fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun turnText(ui: GameUiState): String {
    val hint = when {
        ui.firstCell != null -> "elige la segunda casilla"
        ui.selectedTile != null -> "elige la primera casilla"
        ui.canPlay -> "elige una ficha"
        ui.game.pool.isNotEmpty() -> "no tienes jugada: roba del pozo"
        else -> "no tienes jugada: pasa el turno"
    }
    return "Turno de ${ui.currentPlayer.name} · $hint"
}

private fun eventText(event: GameEvent): String = when (event) {
    is GameEvent.Scored ->
        "+${event.points} puntos" + if (event.extraTurn) " · ¡turno extra por el portal!" else ""
    GameEvent.TileDrawn -> "Robaste una ficha"
    GameEvent.Passed -> "Pasaste el turno"
    is GameEvent.MoveRejected -> moveErrorText(event.error)
    is GameEvent.TurnRejected -> turnErrorText(event.error)
}

private fun moveErrorText(error: MoveError): String = when (error) {
    MoveError.GAME_NOT_ACTIVE -> "La partida no está activa"
    MoveError.NOT_YOUR_TURN -> "No es tu turno"
    MoveError.TILE_NOT_IN_HAND -> "Esa ficha no está en tu mano"
    MoveError.OUT_OF_BOARD -> "Fuera del tablero"
    MoveError.CELLS_NOT_ADJACENT -> "Las casillas deben ser vecinas"
    MoveError.CELL_OCCUPIED -> "Casilla ocupada"
    MoveError.MUST_TOUCH_PLACED_TILE -> "Debes tocar una ficha ya puesta"
    MoveError.PIPS_DO_NOT_MATCH -> "Los puntos que se tocan deben coincidir"
}

private fun turnErrorText(error: TurnError): String = when (error) {
    TurnError.GAME_NOT_ACTIVE -> "La partida no está activa"
    TurnError.NOT_YOUR_TURN -> "No es tu turno"
    TurnError.HAS_VALID_MOVE -> "Aún tienes una jugada válida"
    TurnError.POOL_EMPTY -> "El pozo está vacío"
    TurnError.POOL_NOT_EMPTY -> "Debes robar antes de pasar"
}

/** Interactive preview: it plays with the real reducer, no Firebase needed. */
@Preview(showBackground = true, widthDp = 380, heightDp = 780)
@Composable
private fun GameBoardPreview() {
    GameDominoTheme(darkTheme = false) {
        var ui by remember {
            mutableStateOf(GameUiState(game = GameFactory.newGame(listOf("p1" to "Ana", "p2" to "Luis"))))
        }
        GameBoardContent(
            ui = ui,
            onTileSelected = { ui = GameUiReducer.selectTile(ui, it) },
            onFlip = { ui = GameUiReducer.flip(ui) },
            onCellTapped = { ui = GameUiReducer.tapCell(ui, it) },
            onDrawTile = { ui = GameUiReducer.drawTile(ui) },
            onPassTurn = { ui = GameUiReducer.passTurn(ui) },
            onNewGame = {
                ui = GameUiState(game = GameFactory.newGame(listOf("p1" to "Ana", "p2" to "Luis")))
            }
        )
    }
}