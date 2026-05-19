package com.p_soft.chess.presentation.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.p_soft.chess.domain.model.BoardState
import com.p_soft.chess.domain.model.GameState
import com.p_soft.chess.domain.model.GameStatus
import com.p_soft.chess.domain.model.Player

@Composable
fun GameStatusBar(gameState: GameState) {
    val statusColor = when (gameState.status) {
        GameStatus.CHECK, GameStatus.CHECKMATE -> Color(0xFFF44336) // Красный
        GameStatus.STALEMATE -> Color(0xFF2196F3) // Синий
        GameStatus.ACTIVE -> Color(0xFF4CAF50) // Зеленый
        else -> Color(0xFF9E9E9E) // Серый
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = gameState.status.getDescription(gameState.getWinner()),
                    style = MaterialTheme.typography.titleMedium,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )

                if (gameState.status.isGameOver()) {
                    Text(
                        text = gameState.status.getEmoji(),
                        fontSize = 24.sp
                    )
                }
            }

            if (gameState.status.requiresPlayerAction()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (gameState.currentPlayer == Player.WHITE) "Ход белых" else "Ход чёрных",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}