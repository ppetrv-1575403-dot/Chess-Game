package com.p_soft.chess.presentation.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.p_soft.chess.domain.model.GameState
import com.p_soft.chess.domain.model.Player

@Composable
fun GameStatusIndicator(
    gameState: GameState,
    modifier: Modifier = Modifier
) {
    val statusColor = Color(gameState.status.getColor())

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Индикатор текущего игрока
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            if (gameState.currentPlayer == Player.WHITE) Color.White
                            else Color.Black,
                            CircleShape
                        )
                        .border(1.dp, Color.Gray, CircleShape)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = if (gameState.currentPlayer == Player.WHITE) "Белые" else "Чёрные",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Статус игры
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

        // Дополнительная информация
        if (gameState.status.requiresPlayerAction()) {
            HorizontalDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Ход ${gameState.fullMoveNumber}",
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "Счётчик: ${gameState.halfMoveClock}/50",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}