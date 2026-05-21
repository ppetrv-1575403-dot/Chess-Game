package com.p_soft.chess.presentation.game.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.p_soft.chess.domain.model.GameState

@Composable
fun BottomGamePanel(
    gameState: GameState,
    onUndo: () -> Unit,
    onNewGame: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            CapturedPiecesBar(gameState = gameState)
            Spacer(modifier = Modifier.height(8.dp))
            GameControls(onUndo = onUndo, onNewGame = onNewGame, gameStatus = gameState.status)
        }
    }
}