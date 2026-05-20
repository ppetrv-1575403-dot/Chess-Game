package com.p_soft.chess.engine

import com.p_soft.chess.domain.model.BoardState
import com.p_soft.chess.domain.model.GameState
import com.p_soft.chess.domain.model.GameStatus
import com.p_soft.chess.domain.model.Move
import com.p_soft.chess.domain.model.Piece
import com.p_soft.chess.domain.model.PieceType
import com.p_soft.chess.domain.model.Player
import com.p_soft.chess.domain.model.Square
import kotlin.collections.get
import kotlin.collections.iterator
import kotlin.text.get

class ChessEngine {

    fun getLegalMoves(gameState: GameState, square: Square): List<Move> {
        val piece = gameState.board[square] ?: return emptyList()
        if (piece.player != gameState.currentPlayer) return emptyList()

        val rawMoves = getRawMoves(gameState.board, square, piece, gameState.enPassantTarget)

        return rawMoves.filter { move ->
            val newBoard = applyMove(gameState.board, move)
            !isKingInCheck(newBoard, gameState.currentPlayer)
        }
    }

    fun executeMove(state: GameState, move: Move): GameState {
        val piece = state.board[move.from] ?: return state
        val capturedPiece = if (move.isEnPassant) {
            val capturedRow = if (piece.player == Player.WHITE) move.to.row - 1 else move.to.row + 1
            state.board[Square(capturedRow, move.to.col)]
        } else {
            state.board[move.to]
        }

        val newBoard = applyMove(state.board, move)
        val newPlayer = if (state.currentPlayer == Player.WHITE) Player.BLACK else Player.WHITE

        val isCheck = isKingInCheck(newBoard, newPlayer)
        val hasLegalMoves = hasAnyLegalMove(newBoard, newPlayer)

        val newStatus = when {
            isCheck && !hasLegalMoves -> GameStatus.CHECKMATE
            !isCheck && !hasLegalMoves -> GameStatus.STALEMATE
            isCheck -> GameStatus.CHECK
            else -> GameStatus.ACTIVE
        }

        val newEnPassant = if (piece.type == PieceType.PAWN &&
            kotlin.math.abs(move.from.row - move.to.row) == 2) {
            Square((move.from.row + move.to.row) / 2, move.from.col)
        } else null

        val newHalfMoveClock = if (piece.type == PieceType.PAWN || capturedPiece != null) {
            0
        } else {
            state.halfMoveClock + 1
        }

        return state.copy(
            board = newBoard,
            currentPlayer = newPlayer,
            moveHistory = state.moveHistory + move,
            status = newStatus,
            capturedPieces = if (capturedPiece != null) state.capturedPieces + capturedPiece else state.capturedPieces,
            enPassantTarget = newEnPassant,
            pendingPromotion = null,
            moveCount = state.moveCount + 1,
            halfMoveClock = newHalfMoveClock,
            fullMoveNumber = if (state.currentPlayer == Player.BLACK) state.fullMoveNumber + 1 else state.fullMoveNumber
        )
    }

    fun applyMove(board: Map<Square, Piece>, move: Move): Map<Square, Piece> {
        val newBoard = board.toMutableMap()
        val piece = newBoard[move.from] ?: return newBoard

        when {
            move.isCastling -> executeCastling(newBoard, piece, move)
            move.isEnPassant -> executeEnPassant(newBoard, piece, move)
            move.promotion != null -> executePromotion(newBoard, piece, move)
            else -> executeNormalMove(newBoard, piece, move)
        }

        return newBoard
    }

    fun isKingInCheck(board: Map<Square, Piece>, player: Player): Boolean {
        val kingSquare = board.entries.find {
            it.value.type == PieceType.KING && it.value.player == player
        }?.key ?: return true

        val opponent = if (player == Player.WHITE) Player.BLACK else Player.WHITE
        return isSquareAttacked(board, kingSquare, opponent)
    }

    fun hasAnyLegalMove(board: Map<Square, Piece>, player: Player): Boolean {
        for ((square, piece) in board) {
            if (piece.player == player) {
                val moves = getRawMoves(board, square, piece, null)
                if (moves.any { !isKingInCheck(applyMove(board, it), player) }) {
                    return true
                }
            }
        }
        return false
    }

    fun gameStateToBoardState(gameState: GameState): BoardState {
        return BoardState(
            pieces = gameState.board,
            currentPlayer = gameState.currentPlayer,
            moveHistory = gameState.moveHistory,
            capturedPieces = gameState.capturedPieces,
            isCheck = gameState.status == GameStatus.CHECK || gameState.status == GameStatus.CHECKMATE,
            isCheckmate = gameState.status == GameStatus.CHECKMATE,
            isStalemate = gameState.status == GameStatus.STALEMATE,
            enPassantTarget = gameState.enPassantTarget,
            pendingPromotion = gameState.pendingPromotion,
            moveCount = gameState.moveCount
        )
    }

    fun boardStateToGameState(boardState: BoardState): GameState {
        return GameState(
            board = boardState.pieces,
            currentPlayer = boardState.currentPlayer,
            moveHistory = boardState.moveHistory,
            status = boardState.toGameStatus(),
            capturedPieces = boardState.capturedPieces,
            enPassantTarget = boardState.enPassantTarget,
            pendingPromotion = boardState.pendingPromotion,
            moveCount = boardState.moveCount
        )
    }

    // Приватные методы

    private fun getRawMoves(
        board: Map<Square, Piece>,
        square: Square,
        piece: Piece,
        enPassantTarget: Square?
    ): List<Move> {
        return when (piece.type) {
            PieceType.PAWN -> getPawnMoves(board, square, piece.player, enPassantTarget)
            PieceType.KNIGHT -> getKnightMoves(board, square, piece.player)
            PieceType.BISHOP -> getBishopMoves(board, square, piece.player)
            PieceType.ROOK -> getRookMoves(board, square, piece.player)
            PieceType.QUEEN -> getQueenMoves(board, square, piece.player)
            PieceType.KING -> getKingMoves(board, square, piece.player)
        }
    }

    private fun getPawnMoves(
        board: Map<Square, Piece>,
        square: Square,
        player: Player,
        enPassantTarget: Square?
    ): List<Move> {
        val moves = mutableListOf<Move>()
        val direction = if (player == Player.WHITE) 1 else -1
        val startRow = if (player == Player.WHITE) 1 else 6
        val promotionRow = if (player == Player.WHITE) 7 else 0

        // Ход вперед
        val oneStep = Square(square.row + direction, square.col)
        if (oneStep.isValid() && board[oneStep] == null) {
            addMovesForSquare(moves, square, oneStep, promotionRow)

            val twoStep = Square(square.row + 2 * direction, square.col)
            if (square.row == startRow && board[twoStep] == null) {
                moves.add(Move(square, twoStep))
            }
        }

        // Взятие
        for (colOffset in listOf(-1, 1)) {
            val captureSquare = Square(square.row + direction, square.col + colOffset)
            if (captureSquare.isValid()) {
                val targetPiece = board[captureSquare]
                if (targetPiece != null && targetPiece.player != player) {
                    addMovesForSquare(moves, square, captureSquare, promotionRow)
                }
                if (enPassantTarget == captureSquare) {
                    moves.add(Move(square, captureSquare, isEnPassant = true))
                }
            }
        }

        return moves
    }

    private fun getKnightMoves(board: Map<Square, Piece>, square: Square, player: Player): List<Move> {
        val offsets = listOf(-2 to -1, -2 to 1, -1 to -2, -1 to 2, 1 to -2, 1 to 2, 2 to -1, 2 to 1)
        return getJumpMoves(board, square, player, offsets)
    }

    private fun getBishopMoves(board: Map<Square, Piece>, square: Square, player: Player): List<Move> {
        return getSlidingMoves(board, square, player, listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1))
    }

    private fun getRookMoves(board: Map<Square, Piece>, square: Square, player: Player): List<Move> {
        return getSlidingMoves(board, square, player, listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1))
    }

    private fun getQueenMoves(board: Map<Square, Piece>, square: Square, player: Player): List<Move> {
        return getSlidingMoves(board, square, player,
            listOf(-1 to -1, -1 to 0, -1 to 1, 0 to -1, 0 to 1, 1 to -1, 1 to 0, 1 to 1))
    }

    private fun getKingMoves(board: Map<Square, Piece>, square: Square, player: Player): List<Move> {
        val moves = getJumpMoves(board, square, player,
            listOf(-1 to -1, -1 to 0, -1 to 1, 0 to -1, 0 to 1, 1 to -1, 1 to 0, 1 to 1))

        // Рокировка
        val piece = board[square] ?: return moves
        if (!piece.hasMoved && !isKingInCheck(board, player)) {
            val backRank = if (player == Player.WHITE) 0 else 7

            // Короткая рокировка
            if (canCastle(board, player, backRank, 7, listOf(5, 6))) {
                (moves as MutableList).add(Move(square, Square(backRank, 6), isCastling = true))
            }

            // Длинная рокировка
            if (canCastle(board, player, backRank, 0, listOf(1, 2, 3))) {
                (moves as MutableList).add(Move(square, Square(backRank, 2), isCastling = true))
            }
        }

        return moves
    }

    private fun getJumpMoves(
        board: Map<Square, Piece>,
        square: Square,
        player: Player,
        offsets: List<Pair<Int, Int>>
    ): List<Move> {
        return offsets.mapNotNull { (rowOffset, colOffset) ->
            val target = Square(square.row + rowOffset, square.col + colOffset)
            if (target.isValid()) {
                val targetPiece = board[target]
                if (targetPiece == null || targetPiece.player != player) {
                    Move(square, target)
                } else null
            } else null
        }
    }

    private fun getSlidingMoves(
        board: Map<Square, Piece>,
        square: Square,
        player: Player,
        directions: List<Pair<Int, Int>>
    ): List<Move> {
        val moves = mutableListOf<Move>()

        for ((rowDir, colDir) in directions) {
            var currentRow = square.row + rowDir
            var currentCol = square.col + colDir

            while (currentRow in 0..7 && currentCol in 0..7) {
                val target = Square(currentRow, currentCol)
                val targetPiece = board[target]

                when {
                    targetPiece == null -> moves.add(Move(square, target))
                    targetPiece.player != player -> {
                        moves.add(Move(square, target))
                        break
                    }
                    else -> break
                }

                currentRow += rowDir
                currentCol += colDir
            }
        }

        return moves
    }

    private fun canCastle(
        board: Map<Square, Piece>,
        player: Player,
        rank: Int,
        rookCol: Int,
        emptyCols: List<Int>
    ): Boolean {
        val rook = board[Square(rank, rookCol)] ?: return false
        if (rook.type != PieceType.ROOK || rook.hasMoved) return false

        val opponent = if (player == Player.WHITE) Player.BLACK else Player.WHITE

        return emptyCols.all { col ->
            board[Square(rank, col)] == null && !isSquareAttacked(board, Square(rank, col), opponent)
        }
    }

    private fun executeCastling(board: MutableMap<Square, Piece>, piece: Piece, move: Move) {
        board[move.to] = piece.copy(hasMoved = true)
        board.remove(move.from)

        val (rookFrom, rookTo) = if (move.to.col == 6) {
            Square(move.from.row, 7) to Square(move.from.row, 5)
        } else {
            Square(move.from.row, 0) to Square(move.from.row, 3)
        }

        board[rookTo] = board[rookFrom]!!.copy(hasMoved = true)
        board.remove(rookFrom)
    }

    private fun executeEnPassant(board: MutableMap<Square, Piece>, piece: Piece, move: Move) {
        val capturedRow = if (piece.player == Player.WHITE) move.to.row - 1 else move.to.row + 1
        board.remove(Square(capturedRow, move.to.col))
        board[move.to] = piece.copy(hasMoved = true)
        board.remove(move.from)
    }

    private fun executePromotion(board: MutableMap<Square, Piece>, piece: Piece, move: Move) {
        board[move.to] = Piece(move.promotion!!, piece.player, hasMoved = true)
        board.remove(move.from)
    }

    private fun executeNormalMove(board: MutableMap<Square, Piece>, piece: Piece, move: Move) {
        board[move.to] = piece.copy(hasMoved = true)
        board.remove(move.from)
    }

    private fun isSquareAttacked(board: Map<Square, Piece>, square: Square, attackerColor: Player): Boolean {
        for ((attackerSquare, piece) in board) {
            if (piece.player == attackerColor) {
                val moves = when (piece.type) {
                    PieceType.PAWN -> getPawnAttacks(attackerSquare, attackerColor)
                    else -> getRawMoves(board, attackerSquare, piece, null)
                }
                if (moves.any { it.to == square }) return true
            }
        }
        return false
    }

    private fun getPawnAttacks(square: Square, player: Player): List<Move> {
        val direction = if (player == Player.WHITE) 1 else -1
        return listOf(-1, 1).mapNotNull { colOffset ->
            val target = Square(square.row + direction, square.col + colOffset)
            if (target.isValid()) Move(square, target) else null
        }
    }

    private fun addMovesForSquare(
        moves: MutableList<Move>,
        from: Square,
        to: Square,
        promotionRow: Int
    ) {
        if (to.row == promotionRow) {
            PieceType.entries.filter { it != PieceType.KING }.forEach { type ->
                moves.add(Move(from, to, promotion = type))
            }
        } else {
            moves.add(Move(from, to))
        }
    }

    private fun Square.isValid(): Boolean = row in 0..7 && col in 0..7
}