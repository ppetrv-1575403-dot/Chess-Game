package com.p_soft.chess.domain.model

import com.p_soft.chess.engine.ChessEngine

data class BoardState(
    val pieces: Map<Square, Piece>,
    val currentPlayer: Player,
    val moveHistory: List<Move> = emptyList(),
    val capturedPieces: List<Piece> = emptyList(),
    val isCheck: Boolean = false,
    val isCheckmate: Boolean = false,
    val isStalemate: Boolean = false,
    val enPassantTarget: Square? = null,
    val pendingPromotion: Move? = null,
    val moveCount: Int = 0
) {
    companion object {
        fun initial(): BoardState {
            return BoardState(
                pieces = ChessEngine.createInitialBoard(),
                currentPlayer = Player.WHITE
            )
        }
    }

    /**
     * Конвертировать в GameStatus
     */
    fun toGameStatus(): GameStatus {
        return when {
            isCheckmate -> GameStatus.CHECKMATE
            isStalemate -> GameStatus.STALEMATE
            isCheck -> GameStatus.CHECK
            moveHistory.isEmpty() -> GameStatus.NOT_STARTED
            else -> GameStatus.ACTIVE
        }
    }

    /**
     * Получить статус игры
     */
    fun getStatus(): GameStatus {
        return toGameStatus()
    }
}