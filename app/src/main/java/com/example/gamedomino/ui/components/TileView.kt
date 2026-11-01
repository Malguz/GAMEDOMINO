package com.example.gamedomino.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.gamedomino.model.Tile

/** Draws 0..6 dots like a die face. */
@Composable
fun PipsView(count: Int, modifier: Modifier = Modifier, color: Color = Color.Black) {
    Canvas(modifier = modifier) {
        val radius = minOf(size.width, size.height) * 0.1f
        pipPositions(count).forEach { (x, y) ->
            drawCircle(color = color, radius = radius, center = Offset(size.width * x, size.height * y))
        }
    }
}

/** Dot positions as fractions (0..1) of the half-tile width and height. */
private fun pipPositions(count: Int): List<Pair<Float, Float>> {
    val low = 0.25f
    val mid = 0.5f
    val high = 0.75f
    return when (count) {
        1 -> listOf(mid to mid)
        2 -> listOf(low to low, high to high)
        3 -> listOf(low to low, mid to mid, high to high)
        4 -> listOf(low to low, high to low, low to high, high to high)
        5 -> listOf(low to low, high to low, mid to mid, low to high, high to high)
        6 -> listOf(low to low, high to low, low to mid, high to mid, low to high, high to high)
        else -> emptyList()
    }
}

/** A domino tile. [vertical] stacks the halves; [selected] draws a thick border. */
@Composable
fun TileView(
    tile: Tile,
    modifier: Modifier = Modifier,
    halfSize: Dp = 40.dp,
    selected: Boolean = false,
    vertical: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(8.dp)
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.DarkGray
    var frame = modifier
        .clip(shape)
        .background(Color(0xFFFFFBF0))
        .border(if (selected) 3.dp else 1.dp, borderColor, shape)
    if (onClick != null) frame = frame.clickable(onClick = onClick)

    if (vertical) {
        Column(modifier = frame) {
            PipsView(tile.a, Modifier.size(halfSize).padding(4.dp))
            Box(Modifier.width(halfSize).height(1.dp).background(Color.DarkGray))
            PipsView(tile.b, Modifier.size(halfSize).padding(4.dp))
        }
    } else {
        Row(modifier = frame) {
            PipsView(tile.a, Modifier.size(halfSize).padding(4.dp))
            Box(Modifier.height(halfSize).width(1.dp).background(Color.DarkGray))
            PipsView(tile.b, Modifier.size(halfSize).padding(4.dp))
        }
    }
}