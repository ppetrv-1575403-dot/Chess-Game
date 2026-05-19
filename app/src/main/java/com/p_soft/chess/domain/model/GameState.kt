package com.p_soft.chess.domain.model

import com.p_soft.chess.engine.ChessEngine

data class GameState(
    val board: Map<Square, Piece>,
    val currentPlayer: Player,
    val moveHistory: List<Move> = emptyList(),
    val status: GameStatus = GameStatus.NOT_STARTED,
    val capturedPieces: List<Piece> = emptyList(),
    val enPassantTarget: Square? = null,
    val pendingPromotion: Move? = null,
    val moveCount: Int = 0,
    val halfMoveClock: Int = 0, // Для правила 50 ходов
    val fullMoveNumber: Int = 1
) {
    companion object {
        fun initial(): GameState {
            return GameState(
                board = ChessEngine.createInitialBoard(),
                currentPlayer = Player.WHITE,
                status = GameStatus.NOT_STARTED
            )
        }
    }

    /**
     * Начать игру
     */
    fun start(): GameState {
        return this.copy(
            status = if (this.status == GameStatus.NOT_STARTED)
                GameStatus.ACTIVE
            else
                this.status
        )
    }

    /**
     * Проверить, закончена ли игра
     */
    fun isGameOver(): Boolean {
        return status.isGameOver()
    }

    /**
     * Получить победителя, если игра закончена матом
     */
    fun getWinner(): Player? {
        return if (status == GameStatus.CHECKMATE) {
            // Победитель - противоположный игрок
            if (currentPlayer == Player.WHITE) Player.BLACK else Player.WHITE
        } else {
            null
        }
    }

    /**
     * Получить описание текущего состояния
     */
    fun getStatusDescription(): String {
        return status.getDescription(getWinner())
    }
}