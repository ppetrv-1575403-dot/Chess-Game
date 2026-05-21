package com.p_soft.chess.presentation.game.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.p_soft.chess.domain.model.GameState
import com.p_soft.chess.domain.model.Player
import com.p_soft.chess.presentation.utils.getPieceUnicode

@Composable
fun CapturedPiecesBar(gameState: GameState) {
    val whiteCaptured = gameState.capturedPieces.filter { it.player == Player.BLACK }
    val blackCaptured = gameState.capturedPieces.filter { it.player == Player.WHITE }

    if (whiteCaptured.isEmpty() && blackCaptured.isEmpty()) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(1.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("♟", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            whiteCaptured.forEach { piece ->
                Text(
                    text = getPieceUnicode(piece),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.alpha(0.8f)
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(1.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            blackCaptured.forEach { piece ->
                Text(
                    text = getPieceUnicode(piece),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.alpha(0.8f)
                )
            }
            Text("♙", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        }
    }
}