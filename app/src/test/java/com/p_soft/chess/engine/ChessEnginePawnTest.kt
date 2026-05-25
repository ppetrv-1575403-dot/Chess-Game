package com.p_soft.chess.engine

import com.p_soft.chess.domain.model.GameState
import com.p_soft.chess.domain.model.Piece
import com.p_soft.chess.domain.model.PieceType
import com.p_soft.chess.domain.model.Player
import com.p_soft.chess.domain.model.Square
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ChessEnginePawnTest {

    private lateinit var engine: ChessEngine

    @Before
    fun setUp() {
        engine = ChessEngine()
    }

    @Test
    fun `white pawn moves up`() {
        val board = createBoardWithKing(
            Square(1, 4) to Piece(PieceType.PAWN, Player.WHITE)
        )
        val state = GameState.initial().start().copy(
            board = board,
            currentPlayer = Player.WHITE
        )

        val moves = engine.getLegalMoves(state, Square(1, 4))
        assertTrue(moves.any { it.to == Square(2, 4) })
    }

    @Test
    fun `black pawn moves down`() {
        val board = createBoardWithKing(
            Square(6, 4) to Piece(PieceType.PAWN, Player.BLACK)
        )
        val state = GameState.initial().start().copy(
            board = board,
            currentPlayer = Player.BLACK
        )

        val moves = engine.getLegalMoves(state, Square(6, 4))
        assertTrue(moves.any { it.to == Square(5, 4) })
    }

    @Test
    fun `pawn cannot move backwards`() {
        val board = createBoardWithKing(
            Square(3, 4) to Piece(PieceType.PAWN, Player.WHITE)
        )
        val state = GameState.initial().start().copy(
            board = board,
            currentPlayer = Player.WHITE
        )

        val moves = engine.getLegalMoves(state, Square(3, 4))
        assertFalse(moves.any { it.to == Square(2, 4) })
    }

    @Test
    fun `pawn cannot move off the board`() {
        // Пешка на последней горизонтали
        val board = createBoardWithKing(
            Square(7, 4) to Piece(PieceType.PAWN, Player.WHITE)
        )
        val state = GameState.initial().start().copy(
            board = board,
            currentPlayer = Player.WHITE
        )

        val moves = engine.getLegalMoves(state, Square(7, 4))
        assertTrue(moves.all { it.to.row in 0..7 })
    }

    @Test
    fun `pawn promotion at correct row for white`() {
        val board = createBoardWithKing(
            Square(6, 4) to Piece(PieceType.PAWN, Player.WHITE)
        )
        val state = GameState.initial().start().copy(
            board = board,
            currentPlayer = Player.WHITE
        )

        val moves = engine.getLegalMoves(state, Square(6, 4))
        val toLastRank = moves.filter { it.to.row == 7 }
        assertTrue(toLastRank.all { it.promotion != null })
    }

    @Test
    fun `pawn promotion at correct row for black`() {
        val board = createBoardWithKing(
            Square(1, 4) to Piece(PieceType.PAWN, Player.BLACK)
        )
        val state = GameState.initial().start().copy(
            board = board,
            currentPlayer = Player.BLACK
        )

        val moves = engine.getLegalMoves(state, Square(1, 4))
        val toLastRank = moves.filter { it.to.row == 0 }
        assertTrue(toLastRank.all { it.promotion != null })
    }

    private fun createBoardWithKing(vararg pieces: Pair<Square, Piece>): Map<Square, Piece> {
        val board = mutableMapOf(*pieces)
        board[Square(0, 4)] = Piece(PieceType.KING, Player.WHITE)
        board[Square(7, 4)] = Piece(PieceType.KING, Player.BLACK)
        return board
    }
}