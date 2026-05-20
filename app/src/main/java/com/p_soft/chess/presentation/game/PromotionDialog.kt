package com.p_soft.chess.presentation.game

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

@Composable
fun PromotionDialog(
    player: Player,
    onPieceSelected: (PieceType) -> Unit,
    onDismiss: () -> Unit
) {
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

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PromotionPieceOption(
                        piece = Piece(PieceType.QUEEN, player),
                        name = "Ферзь",
                        onClick = { onPieceSelected(PieceType.QUEEN) }
                    )
                    PromotionPieceOption(
                        piece = Piece(PieceType.ROOK, player),
                        name = "Ладья",
                        onClick = { onPieceSelected(PieceType.ROOK) }
                    )
                    PromotionPieceOption(
                        piece = Piece(PieceType.BISHOP, player),
                        name = "Слон",
                        onClick = { onPieceSelected(PieceType.BISHOP) }
                    )
                    PromotionPieceOption(
                        piece = Piece(PieceType.KNIGHT, player),
                        name = "Конь",
                        onClick = { onPieceSelected(PieceType.KNIGHT) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

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
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    if (piece.player == Player.WHITE) Color.White.copy(alpha = 0.15f)
                    else Color.Black.copy(alpha = 0.15f)
                )
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = getPromotionPieceUnicode(piece),
                fontSize = 30.sp,
                color = if (piece.player == Player.WHITE) Color.White else Color.Black
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center
        )
    }
}

private fun getPromotionPieceUnicode(piece: Piece): String {
    return when (piece.type) {
        PieceType.QUEEN -> if (piece.player == Player.WHITE) "♕" else "♛"
        PieceType.ROOK -> if (piece.player == Player.WHITE) "♖" else "♜"
        PieceType.BISHOP -> if (piece.player == Player.WHITE) "♗" else "♝"
        PieceType.KNIGHT -> if (piece.player == Player.WHITE) "♘" else "♞"
        else -> ""
    }
}