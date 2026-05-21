package com.p_soft.chess.presentation.game.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.p_soft.chess.domain.model.GameState
import com.p_soft.chess.domain.model.Player
import com.p_soft.chess.presentation.utils.getPieceUnicode

@Composable
fun CapturedPiecesVertical(gameState: GameState) {
    val whiteCaptured = gameState.capturedPieces.filter { it.player == Player.BLACK }
    val blackCaptured = gameState.capturedPieces.filter { it.player == Player.WHITE }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Взятые белые фигуры
        Text("Взято белыми:", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (whiteCaptured.isEmpty()) {
            Text("—", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                whiteCaptured.forEach { piece ->
                    Text(getPieceUnicode(piece), fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Взятые чёрные фигуры
        Text("Взято чёрными:", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (blackCaptured.isEmpty()) {
            Text("—", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                blackCaptured.forEach { piece ->
                    Text(getPieceUnicode(piece), fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}