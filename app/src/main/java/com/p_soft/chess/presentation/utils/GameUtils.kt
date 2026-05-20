package com.p_soft.chess.presentation.utils

import com.p_soft.chess.domain.model.Piece
import com.p_soft.chess.domain.model.PieceType
import com.p_soft.chess.domain.model.Player
import com.p_soft.chess.domain.model.Square

/**
 * Определить, каких фигур не хватает у игрока для превращения.
 * Возвращает список пар (тип фигуры, сколько можно добавить).
 */
fun getAvailablePromotions(
    player: Player,
    currentPieces: Map<Square, Piece>
): List<Pair<PieceType, Int>> {
    val result = mutableListOf<Pair<PieceType, Int>>()

    // Максимальное количество фигур каждого типа (без пешек)
    val maxCounts = mapOf(
        PieceType.QUEEN to 1,
        PieceType.ROOK to 2,
        PieceType.BISHOP to 2,
        PieceType.KNIGHT to 2
    )

    // Подсчитываем текущее количество
    val currentCounts = mutableMapOf(
        PieceType.QUEEN to 0,
        PieceType.ROOK to 0,
        PieceType.BISHOP to 0,
        PieceType.KNIGHT to 0
    )

    currentPieces.values
        .filter { it.player == player && it.type in currentCounts }
        .forEach { piece ->
            currentCounts[piece.type] = currentCounts[piece.type]!! + 1
        }

    // Проверяем, каких фигур меньше максимума
    for ((type, maxCount) in maxCounts) {
        val currentCount = currentCounts[type] ?: 0
        if (currentCount < maxCount) {
            result.add(type to (maxCount - currentCount))
        }
    }

    return result
}

fun isNoAvailablePromotions(player: Player,
                            currentPieces: Map<Square, Piece>): Boolean {
    val availablePromotions = getAvailablePromotions(player, currentPieces)
    return availablePromotions.isEmpty()
}

fun getPieceName(type: PieceType): String {
    return when (type) {
        PieceType.QUEEN -> "Ферзь"
        PieceType.ROOK -> "Ладья"
        PieceType.BISHOP -> "Слон"
        PieceType.KNIGHT -> "Конь"
        else -> ""
    }
}

fun getPieceUnicode(piece: Piece): String {
    return when (piece.type) {
        PieceType.QUEEN -> if (piece.player == Player.WHITE) "♕" else "♛"
        PieceType.ROOK -> if (piece.player == Player.WHITE) "♖" else "♜"
        PieceType.BISHOP -> if (piece.player == Player.WHITE) "♗" else "♝"
        PieceType.KNIGHT -> if (piece.player == Player.WHITE) "♘" else "♞"
        else -> ""
    }
}
