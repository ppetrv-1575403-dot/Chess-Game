package com.p_soft.chess.presentation.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.p_soft.chess.domain.model.GameState
import com.p_soft.chess.domain.model.GameStatus
import com.p_soft.chess.domain.model.Player

@Composable

fun GameStatusBar(gameState: GameState) {
    val statusColor = when (gameState.status) {
        GameStatus.CHECK, GameStatus.CHECKMATE -> Color(0xFFF44336)
        GameStatus.STALEMATE -> Color(0xFF2196F3)
        GameStatus.ACTIVE -> Color(0xFF4CAF50)
        else -> Color(0xFF9E9E9E)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Статус игры
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Индикатор текущего игрока
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(
                                if (gameState.currentPlayer == Player.WHITE) Color.White
                                else Color.Black
                            )
                            .border(2.dp, Color.Gray, CircleShape)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "Ход ${gameState.fullMoveNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (gameState.currentPlayer == Player.WHITE) "Ходят белые" else "Ходят чёрные",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Статус
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = gameState.status.getDescription(gameState.getWinner()),
                        style = MaterialTheme.typography.bodyLarge,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End
                    )

                    if (gameState.status.isGameOver()) {
                        Text(
                            text = gameState.status.getEmoji(),
                            fontSize = 24.sp
                        )
                    }
                }
            }

            // Дополнительная информация во время игры
            if (gameState.status.requiresPlayerAction()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Ходов сделано: ${gameState.moveCount}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "Взято фигур: ${gameState.capturedPieces.size}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}