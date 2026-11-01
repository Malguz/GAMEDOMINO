package com.example.gamedomino.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gamedomino.model.Player

/** Round, pool size and one card per player; the player on turn is highlighted. */
@Composable
fun ScoreBar(
    players: List<Player>,
    currentPlayerId: String,
    round: Int,
    maxRounds: Int,
    poolSize: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Ronda $round/$maxRounds  ·  Pozo: $poolSize fichas",
            style = MaterialTheme.typography.labelLarge
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            players.forEach { player ->
                val active = player.id == currentPlayerId
                val base = playerColor(players, player.id) ?: Color.Gray
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = base.copy(alpha = if (active) 0.85f else 0.3f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = player.name,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                            color = Color.Black
                        )
                        Text(
                            text = "${player.score} pts · ${player.hand.size} fichas",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}