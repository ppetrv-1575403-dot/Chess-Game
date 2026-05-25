package com.p_soft.chess.presentation.game.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.p_soft.chess.domain.model.GameStatus
import com.p_soft.chess.presentation.theme.darkCancelColor
import com.p_soft.chess.presentation.theme.darkCancelColorDisabled
import com.p_soft.chess.presentation.theme.darkSquareColor
import com.p_soft.chess.presentation.theme.darkSquareColor2

@Composable
fun GameControls(
    onUndo: () -> Unit,
    onNewGame: () -> Unit,
    gameStatus: GameStatus,
    undoAvailable: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FilledTonalButton(
            onClick = onUndo,
            enabled = gameStatus.requiresPlayerAction() && undoAvailable,
            modifier = Modifier.weight(1f).height(44.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonColors(
                containerColor = darkCancelColor,
                contentColor = Color.White,
                disabledContainerColor = darkCancelColorDisabled,
                disabledContentColor = Color.Gray
            )
        ) {
            Icon(Icons.Default.Undo, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Отменить", fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }

        Button(
            onClick = onNewGame,
            modifier = Modifier.weight(1f).height(44.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonColors(
                containerColor = darkSquareColor,
                contentColor = Color.White,
                disabledContainerColor = darkCancelColorDisabled,
                disabledContentColor = Color.Gray
            )
        ) {
            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Новая игра", fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
    }
}