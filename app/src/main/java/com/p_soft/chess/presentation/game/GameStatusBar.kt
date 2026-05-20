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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            if (gameState.currentPlayer == Player.WHITE) Color.White
                            else Color.Black
                        )
                        .border(1.dp, Color.Gray, CircleShape)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = if (gameState.currentPlayer == Player.WHITE) "Ход белых" else "Ход чёрных",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = gameState.status.getDescription(gameState.getWinner()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End
                )

                if (gameState.status.isGameOver()) {
                    Text(
                        text = gameState.status.getEmoji(),
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}