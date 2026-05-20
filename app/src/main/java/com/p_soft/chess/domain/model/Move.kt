package com.p_soft.chess.domain.model

data class Move(
    val from: Square,
    val to: Square,
    val promotion: PieceType? = null,
    val isCastling: Boolean = false,
    val isEnPassant: Boolean = false
) {
    fun isCapture(targetPiece: Piece?): Boolean {
        return targetPiece != null || isEnPassant
    }

    fun isPromotion(): Boolean = promotion != null

    fun toAlgebraicNotation(piece: Piece?, isCheck: Boolean = false, isCheckmate: Boolean = false): String {
        val pieceSymbol = when (piece?.type) {
            PieceType.KING -> "K"
            PieceType.QUEEN -> "Q"
            PieceType.ROOK -> "R"
            PieceType.BISHOP -> "B"
            PieceType.KNIGHT -> "N"
            else -> ""
        }

        val captureSymbol = if (isEnPassant) "x" else if (piece != null && piece.type == PieceType.PAWN) "${'a' + from.col}x" else ""
        val destination = "${'a' + to.col}${to.row + 1}"
        val promotionText = if (promotion != null) "=${promotion.name.first()}" else ""

        val suffix = when {
            isCheckmate -> "#"
            isCheck -> "+"
            else -> ""
        }

        return "$pieceSymbol$captureSymbol$destination$promotionText$suffix"
    }
}