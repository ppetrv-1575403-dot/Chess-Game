package com.p_soft.chess.presentation.game

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.p_soft.chess.domain.model.GameStatus

@Composable
fun GameControls(
    onUndo: () -> Unit,
    onNewGame: () -> Unit,
    gameStatus: GameStatus
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onUndo,
            enabled = gameStatus.requiresPlayerAction(),
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
        ) {
            Icon(
                Icons.Default.Undo,
                contentDescription = "Отменить ход",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Отменить")
        }

        Button(
            onClick = onNewGame,
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
        ) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = "Новая игра",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Новая игра")
        }
    }
}