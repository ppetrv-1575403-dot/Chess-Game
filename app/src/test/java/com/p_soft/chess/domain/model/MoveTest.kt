package com.p_soft.chess.domain.model

import org.junit.Assert
import org.junit.Test

class MoveTest {

    @Test
    fun `move isCapture returns true for en passant`() {
        val move = Move(
            from = Square(4, 0),
            to = Square(5, 1),
            isEnPassant = true
        )
        Assert.assertTrue(move.isCapture(targetPiece = null))
    }

    @Test
    fun `move isCapture returns true when target piece exists`() {
        val move = Move(Square(1, 0), Square(2, 0))
        val targetPiece = Piece(PieceType.PAWN, Player.BLACK)
        Assert.assertTrue(move.isCapture(targetPiece))
    }

    @Test
    fun `move isCapture returns false for normal move`() {
        val move = Move(Square(1, 0), Square(2, 0))
        Assert.assertFalse(move.isCapture(targetPiece = null))
    }

    @Test
    fun `isPromotion returns true when promotion is set`() {
        val move = Move(
            from = Square(6, 0),
            to = Square(7, 0),
            promotion = PieceType.QUEEN
        )
        Assert.assertTrue(move.isPromotion())
    }

    @Test
    fun `isPromotion returns false when promotion is null`() {
        val move = Move(Square(1, 0), Square(2, 0))
        Assert.assertFalse(move.isPromotion())
    }

    @Test
    fun `algebraic notation for simple pawn move`() {
        val move = Move(Square(1, 0), Square(2, 0))
        val notation = move.toAlgebraicNotation(
            piece = Piece(PieceType.PAWN, Player.WHITE)
        )
        Assert.assertEquals("a3", notation)
    }

    @Test
    fun `algebraic notation for pawn capture`() {
        val move = Move(
            from = Square(1, 0),
            to = Square(2, 1),
            isEnPassant = false
        )
        val notation = move.toAlgebraicNotation(
            piece = Piece(PieceType.PAWN, Player.WHITE)
        )
        Assert.assertEquals("axb3", notation) // Пешка с a2 бьёт на b3
    }

    @Test
    fun `algebraic notation for knight move`() {
        val move = Move(Square(0, 1), Square(2, 2))
        val notation = move.toAlgebraicNotation(
            piece = Piece(PieceType.KNIGHT, Player.WHITE)
        )
        Assert.assertEquals("Nc3", notation)
    }

    @Test
    fun `algebraic notation with check`() {
        val move = Move(Square(1, 0), Square(2, 0))
        val notation = move.toAlgebraicNotation(
            piece = Piece(PieceType.PAWN, Player.WHITE),
            isCheck = true
        )
        Assert.assertEquals("a3+", notation)
    }

    @Test
    fun `algebraic notation with checkmate`() {
        val move = Move(Square(1, 0), Square(2, 0))
        val notation = move.toAlgebraicNotation(
            piece = Piece(PieceType.PAWN, Player.WHITE),
            isCheckmate = true
        )
        Assert.assertEquals("a3#", notation)
    }

    @Test
    fun `algebraic notation for promotion`() {
        val move = Move(
            from = Square(6, 0),
            to = Square(7, 0),
            promotion = PieceType.QUEEN
        )
        val notation = move.toAlgebraicNotation(
            piece = Piece(PieceType.PAWN, Player.WHITE)
        )
        Assert.assertEquals("a8=Q", notation)
    }
}