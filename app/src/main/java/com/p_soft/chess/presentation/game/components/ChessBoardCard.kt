package com.p_soft.chess.presentation.game.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.p_soft.chess.domain.model.GameState
import com.p_soft.chess.domain.model.Move
import com.p_soft.chess.domain.model.Square
import com.p_soft.chess.presentation.game.ChessBoard

@Composable
fun ChessBoardCard(
    gameState: GameState,
    selectedSquare: Square?,
    validMoves: List<Move>,
    onSquareClick: (Square) -> Unit
) {
    // Адаптивный размер доски — занимает максимум доступного пространства
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val maxSize = minOf(maxWidth, maxHeight)

        Card(
            modifier = Modifier.size(maxSize),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            ChessBoard(
                gameState = gameState,
                selectedSquare = selectedSquare,
                validMoves = validMoves,
                onSquareClick = onSquareClick
            )
        }
    }
}
