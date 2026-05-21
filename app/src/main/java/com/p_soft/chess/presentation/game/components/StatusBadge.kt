package com.p_soft.chess.presentation.game.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.p_soft.chess.domain.model.GameStatus
import com.p_soft.chess.domain.model.Player

@Composable
fun StatusBadge(status: GameStatus, winner: Player?) {
    val (bgColor, text, textColor) = when (status) {
        GameStatus.ACTIVE -> Triple(Color(0xFF4CAF50).copy(alpha = 0.15f), null, Color(0xFF4CAF50))
        GameStatus.CHECK -> Triple(Color(0xFFFF9800).copy(alpha = 0.2f), "ШАХ", Color(0xFFFF9800))
        GameStatus.CHECKMATE -> Triple(Color(0xFFF44336).copy(alpha = 0.2f), "МАТ", Color(0xFFF44336))
        GameStatus.STALEMATE -> Triple(Color(0xFF2196F3).copy(alpha = 0.2f), "ПАТ", Color(0xFF2196F3))
        else -> Triple(Color(0xFF9E9E9E).copy(alpha = 0.15f), null, Color(0xFF9E9E9E))
    }

    text?.let {
        Surface(color = bgColor, shape = RoundedCornerShape(12.dp)) {
            Text(
                text = it,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
        }
    }
}