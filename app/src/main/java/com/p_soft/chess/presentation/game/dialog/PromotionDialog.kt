package com.p_soft.chess.presentation.game.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.window.Dialog
import com.p_soft.chess.domain.model.Piece
import com.p_soft.chess.domain.model.PieceType
import com.p_soft.chess.domain.model.Player
import com.p_soft.chess.domain.model.Square
import com.p_soft.chess.presentation.utils.getAvailablePromotions
import com.p_soft.chess.presentation.utils.getPieceName
import com.p_soft.chess.presentation.utils.getPieceUnicode

/**
 * Диалог выбора фигуры для превращения пешки.
 * Показывает только те фигуры, которых у игрока меньше обычного количества.
 * Если все фигуры на месте — показывает диалог с единственной доступной опцией (ферзь заблокирован).
 */
@Composable
fun PromotionDialog(
    player: Player,
    currentPieces: Map<Square, Piece>,
    onPieceSelected: (PieceType) -> Unit,
    onDismiss: () -> Unit
) {
    val availablePromotions = getAvailablePromotions(player, currentPieces)

    // Если доступна только 1 фигура или ни одной — не показываем диалог
    if (availablePromotions.size <= 1) {
        // Автоматический выбор или отмена
        if (availablePromotions.size == 1) {
            onPieceSelected(availablePromotions.first().first)
        } else {
            onDismiss()
        }
        return
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Выберите фигуру",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Пешка достигла последней горизонтали",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Показываем все фигуры, но недоступные заблокированы
                val allTypes = listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    allTypes.forEach { pieceType ->
                        val isAvailable = availablePromotions.any { it.first == pieceType }

                        PromotionPieceOption(
                            piece = Piece(pieceType, player),
                            name = getPieceName(pieceType),
                            available = isAvailable,
                            onClick = {
                                if (isAvailable) {
                                    onPieceSelected(pieceType)
                                }
                            }
                        )
                    }
                }

                // Пояснение, если какие-то фигуры недоступны
                if (availablePromotions.size < allTypes.size) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Серые фигуры недоступны — они уже есть на доске",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onDismiss) {
                    Text("Отмена")
                }
            }
        }
    }
}

@Composable
private fun PromotionPieceOption(
    piece: Piece,
    name: String,
    available: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (available) Modifier.clickable(onClick = onClick)
                else Modifier
            )
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    if (available) {
                        if (piece.player == Player.WHITE) Color.White.copy(alpha = 0.15f)
                        else Color.Black.copy(alpha = 0.15f)
                    } else {
                        Color.Gray.copy(alpha = 0.1f)
                    }
                )
                .border(
                    width = 2.dp,
                    color = if (available) MaterialTheme.colorScheme.primary
                    else Color.Gray.copy(alpha = 0.3f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = getPieceUnicode(piece),
                fontSize = 30.sp,
                color = if (available) {
                    if (piece.player == Player.WHITE) Color.White else Color.Black
                } else {
                    Color.Gray.copy(alpha = 0.4f)
                }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            color = if (available) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )

        // Подпись о доступности
        if (!available) {
            Text(
                text = "есть",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray.copy(alpha = 0.5f),
                fontSize = 8.sp
            )
        }
    }
}