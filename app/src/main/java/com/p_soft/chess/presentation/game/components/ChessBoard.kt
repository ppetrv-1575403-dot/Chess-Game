package com.p_soft.chess.presentation.game.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.p_soft.chess.domain.model.GameState
import com.p_soft.chess.domain.model.GameStatus
import com.p_soft.chess.domain.model.Move
import com.p_soft.chess.domain.model.PieceType
import com.p_soft.chess.domain.model.Square
import com.p_soft.chess.presentation.game.ChessSquare
import com.p_soft.chess.presentation.game.CoordinateRow

@Composable
fun ChessBoard(
    gameState: GameState,
    selectedSquare: Square?,
    validMoves: List<Move>,
    onSquareClick: (Square) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        CoordinateRow(labels = ('a'..'h').map { it.toString() })

        for (row in 7 downTo 0) {
            Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                CoordinateLabel(text = (row + 1).toString())

                for (col in 0..7) {
                    val square = Square(row, col)
                    val isLight = (row + col) % 2 == 0
                    val isSelected = square == selectedSquare
                    val isValidMove = validMoves.any { it.to == square }
                    val isLastMove = gameState.moveHistory.lastOrNull()?.let {
                        it.from == square || it.to == square
                    } ?: false
                    val isKingInCheck = gameState.status == GameStatus.CHECK &&
                            gameState.board[square]?.type == PieceType.KING &&
                            gameState.board[square]?.player == gameState.currentPlayer

                    ChessSquare(
                        modifier = Modifier.weight(1f),
                        isLight = isLight,
                        isSelected = isSelected,
                        isValidMove = isValidMove,
                        isLastMove = isLastMove,
                        isKingInCheck = isKingInCheck,
                        piece = gameState.board[square],
                        onClick = { onSquareClick(square) }
                    )
                }

                CoordinateLabel(text = (row + 1).toString())
            }
        }

        CoordinateRow(labels = ('a'..'h').map { it.toString() })
    }
}