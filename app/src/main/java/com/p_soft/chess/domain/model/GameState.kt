package com.p_soft.chess.domain.model

data class GameState(
    val board: Map<Square, Piece>,
    val currentPlayer: Player,
    val moveHistory: List<Move> = emptyList(),
    val status: GameStatus = GameStatus.NOT_STARTED,
    val capturedPieces: List<Piece> = emptyList(),
    val enPassantTarget: Square? = null,
    val pendingPromotion: Move? = null,
    val moveCount: Int = 0,
    val halfMoveClock: Int = 0,
    val fullMoveNumber: Int = 1
) {
    companion object {
        fun initial(): GameState {
            val pieces = mutableMapOf<Square, Piece>()

            for (col in 0..7) {
                pieces[Square(1, col)] = Piece(PieceType.PAWN, Player.WHITE)
                pieces[Square(6, col)] = Piece(PieceType.PAWN, Player.BLACK)
            }

            val backRowPieces = listOf(
                PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
                PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
            )

            for (col in 0..7) {
                pieces[Square(0, col)] = Piece(backRowPieces[col], Player.WHITE)
                pieces[Square(7, col)] = Piece(backRowPieces[col], Player.BLACK)
            }

            return GameState(
                board = pieces,
                currentPlayer = Player.WHITE,
                status = GameStatus.NOT_STARTED
            )
        }
    }

    fun start(): GameState = copy(status = GameStatus.ACTIVE)

    fun isGameOver(): Boolean = status.isGameOver()

    fun getWinner(): Player? {
        return if (status == GameStatus.CHECKMATE) {
            if (currentPlayer == Player.WHITE) Player.BLACK else Player.WHITE
        } else null
    }
}