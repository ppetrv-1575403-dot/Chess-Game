package com.p_soft.chess.domain.model

data class Piece(
    val type: PieceType,
    val player: Player,
    val hasMoved: Boolean = false // Важно для рокировки и пешек
)


