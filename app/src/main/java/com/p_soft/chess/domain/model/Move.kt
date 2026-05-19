package com.p_soft.chess.domain.model

data class Move(
    val from: Square,
    val to: Square,
    val promotion: PieceType? = null,
    val isCastling: Boolean = false,
    val isEnPassant: Boolean = false
) {
    /**
     * Проверяет, является ли ход взятием
     */
    fun isCapture(): Boolean {
        return isEnPassant
    }

    /**
     * Проверяет, является ли ход превращением пешки
     */
    fun isPromotion(): Boolean {
        return promotion != null
    }

    /**
     * Получить описание хода в шахматной нотации
     */
    fun toAlgebraicNotation(piece: Piece?, isCheck: Boolean = false, isCheckmate: Boolean = false): String {
        val pieceSymbol = when (piece?.type) {
            PieceType.KING -> "K"
            PieceType.QUEEN -> "Q"
            PieceType.ROOK -> "R"
            PieceType.BISHOP -> "B"
            PieceType.KNIGHT -> "N"
            else -> ""
        }

        val captureSymbol = if (isCapture()) "x" else ""
        val destination = "${('a' + to.col)}${to.row + 1}"
        val promotionText = if (promotion != null) "=${promotion.name.first()}" else ""

        val suffix = when {
            isCheckmate -> "#"
            isCheck -> "+"
            else -> ""
        }

        return if (piece?.type == PieceType.PAWN && isCapture()) {
            "${'a' + from.col}x$destination$promotionText$suffix"
        } else {
            "$pieceSymbol$captureSymbol$destination$promotionText$suffix"
        }
    }
}