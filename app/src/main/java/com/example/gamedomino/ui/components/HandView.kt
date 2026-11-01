package com.example.gamedomino.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gamedomino.model.Tile
import com.example.gamedomino.model.sameAs

/** The player's tiles in a horizontal scrolling row. The selected tile gets a thick border. */
@Composable
fun HandView(
    hand: List<Tile>,
    selected: Tile?,
    modifier: Modifier = Modifier,
    onTileClick: (Tile) -> Unit
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
    ) {
        items(hand) { tile ->
            TileView(
                tile = tile,
                selected = selected != null && selected.sameAs(tile),
                onClick = { onTileClick(tile) }
            )
        }
    }
}
