package com.p_soft.chess.domain.model

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
            val pieces = mutableMapOf<Square, Piece>()

            // Пешки
            for (col in 0..7) {
                pieces[Square(1, col)] = Piece(PieceType.PAWN, Player.WHITE)
                pieces[Square(6, col)] = Piece(PieceType.PAWN, Player.BLACK)
            }

            // Фигуры
            val backRowPieces = listOf(
                PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
                PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
            )

            for (col in 0..7) {
                pieces[Square(0, col)] = Piece(backRowPieces[col], Player.WHITE)
                pieces[Square(7, col)] = Piece(backRowPieces[col], Player.BLACK)
            }

            return BoardState(
                pieces = pieces,
                currentPlayer = Player.WHITE
            )
        }
    }

    fun toGameStatus(): GameStatus {
        return when {
            isCheckmate -> GameStatus.CHECKMATE
            isStalemate -> GameStatus.STALEMATE
            isCheck -> GameStatus.CHECK
            moveHistory.isEmpty() -> GameStatus.NOT_STARTED
            else -> GameStatus.ACTIVE
        }
    }
}