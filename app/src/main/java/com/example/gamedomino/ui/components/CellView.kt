package com.example.gamedomino.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gamedomino.model.Cell
import com.example.gamedomino.model.CellType
import com.example.gamedomino.model.GameEngine

/**
 * One board cell. Its colour shows the type, the tint shows the owner,
 * the dots show the half-tile placed on it.
 * Green border = valid choice, orange border = first cell already chosen.
 */
@Composable
fun CellView(
    cell: Cell,
    ownerColor: Color?,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    isFirstSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    val shape = RoundedCornerShape(6.dp)
    val border = when {
        isFirstSelected -> BorderStroke(3.dp, Color(0xFFFF6F00))
        highlighted -> BorderStroke(3.dp, Color(0xFF00C853))
        else -> BorderStroke(1.dp, Color(0x33000000))
    }
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundFor(cell.type))
            .border(border, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (ownerColor != null) {
            Box(Modifier.fillMaxSize().background(ownerColor.copy(alpha = 0.45f)))
        }
        val pips = cell.pips
        if (pips == null) {
            Text(text = iconFor(cell.type), fontSize = 20.sp)
        } else {
            PipsView(count = pips, modifier = Modifier.fillMaxSize().padding(6.dp))
        }
        if (cell.type == CellType.FORTRESS && cell.ownerId == null && cell.fortressHits > 0) {
            Text(
                text = "${cell.fortressHits}/${GameEngine.FORTRESS_HITS_NEEDED}",
                fontSize = 10.sp,
                color = Color.Black,
                modifier = Modifier.align(Alignment.TopEnd).padding(2.dp)
            )
        }
    }
}

private fun backgroundFor(type: CellType): Color = when (type) {
    CellType.NORMAL -> Color(0xFFEEEEEE)
    CellType.BONUS -> Color(0xFFFFF59D)
    CellType.TREASURE -> Color(0xFFFFE082)
    CellType.TRAP -> Color(0xFFEF9A9A)
    CellType.FORTRESS -> Color(0xFFB0BEC5)
    CellType.PORTAL -> Color(0xFFCE93D8)
}

private fun iconFor(type: CellType): String = when (type) {
    CellType.NORMAL -> ""
    CellType.BONUS -> "⭐"
    CellType.TREASURE -> "💰"
    CellType.TRAP -> "☠️"
    CellType.FORTRESS -> "🏰"
    CellType.PORTAL -> "🌀"
}