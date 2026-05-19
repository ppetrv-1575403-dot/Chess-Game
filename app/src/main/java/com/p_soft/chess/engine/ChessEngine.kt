package com.p_soft.chess.engine

import com.p_soft.chess.domain.model.BoardState
import com.p_soft.chess.domain.model.GameState
import com.p_soft.chess.domain.model.GameStatus
import com.p_soft.chess.domain.model.Move
import com.p_soft.chess.domain.model.Piece
import com.p_soft.chess.domain.model.PieceType
import com.p_soft.chess.domain.model.Player
import com.p_soft.chess.domain.model.Square

class ChessEngine {

    companion object {
        /**
         * Создание начальной позиции на доске
         */
        fun createInitialBoard(): Map<Square, Piece> {
            val pieces = mutableMapOf<Square, Piece>()

            // Белые пешки
            for (col in 0..7) {
                pieces[Square(1, col)] = Piece(PieceType.PAWN, Player.WHITE)
            }

            // Черные пешки
            for (col in 0..7) {
                pieces[Square(6, col)] = Piece(PieceType.PAWN, Player.BLACK)
            }

            // Фигуры на задних рядах
            val backRowPieces = listOf(
                PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
                PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
            )

            // Белые фигуры (ряд 0)
            for (col in 0..7) {
                pieces[Square(0, col)] = Piece(backRowPieces[col], Player.WHITE)
            }

            // Черные фигуры (ряд 7)
            for (col in 0..7) {
                pieces[Square(7, col)] = Piece(backRowPieces[col], Player.BLACK)
            }

            return pieces
        }

        /**
         * Координаты клеток для обозначения
         */
        fun getSquareNotation(square: Square): String {
            val file = ('a' + square.col).toString()
            val rank = (square.row + 1).toString()
            return "$file$rank"
        }

        /**
         * Создание клетки из шахматной нотации (например "e4")
         */
        fun squareFromNotation(notation: String): Square? {
            if (notation.length != 2) return null
            val col = notation[0] - 'a'
            val row = notation[1] - '1'
            if (col !in 0..7 || row !in 0..7) return null
            return Square(row, col)
        }
    }

    /**
     * Получить все возможные ходы для фигуры на клетке (без проверки шаха)
     */
    fun getRawMoves(board: Map<Square, Piece>, square: Square): List<Move> {
        val piece = board[square] ?: return emptyList()
        return getRawMovesForPiece(board, square, piece)
    }

    /**
     * Получить легальные ходы (с проверкой шаха)
     */
    fun getLegalMoves(board: Map<Square, Piece>, square: Square, currentPlayer: Player): List<Move> {
        val piece = board[square] ?: return emptyList()
        if (piece.player != currentPlayer) return emptyList()

        val rawMoves = getRawMovesForPiece(board, square, piece)

        return rawMoves.filter { move ->
            val newBoard = applyMove(board, move)
            !isKingInCheck(newBoard, currentPlayer)
        }
    }

    /**
     * Получить легальные ходы для GameState
     */
    fun getLegalMovesForGameState(gameState: GameState, square: Square): List<Move> {
        return getLegalMoves(gameState.board, square, gameState.currentPlayer)
    }

    /**
     * Внутренний метод для получения ходов конкретной фигуры
     */
    private fun getRawMovesForPiece(board: Map<Square, Piece>, square: Square, piece: Piece): List<Move> {
        return when (piece.type) {
            PieceType.PAWN -> getPawnMoves(board, square, piece.player)
            PieceType.KNIGHT -> getKnightMoves(board, square, piece.player)
            PieceType.BISHOP -> getBishopMoves(board, square, piece.player)
            PieceType.ROOK -> getRookMoves(board, square, piece.player)
            PieceType.QUEEN -> getQueenMoves(board, square, piece.player)
            PieceType.KING -> getKingMoves(board, square, piece.player)
        }
    }

    /**
     * Ходы пешки
     */
    fun getPawnMoves(board: Map<Square, Piece>, square: Square, player: Player): List<Move> {
        val moves = mutableListOf<Move>()
        val direction = if (player == Player.WHITE) 1 else -1
        val startRow = if (player == Player.WHITE) 1 else 6
        val promotionRow = if (player == Player.WHITE) 7 else 0

        // Ход вперед на одну клетку
        val oneStep = Square(square.row + direction, square.col)
        if (oneStep.row in 0..7 && oneStep.col in 0..7 && board[oneStep] == null) {
            if (oneStep.row == promotionRow) {
                // Превращение пешки
                addPromotionMoves(moves, square, oneStep)
            } else {
                moves.add(Move(square, oneStep))
            }

            // Ход на две клетки с начальной позиции
            if (square.row == startRow) {
                val twoSteps = Square(square.row + 2 * direction, square.col)
                if (board[twoSteps] == null) {
                    moves.add(Move(square, twoSteps))
                }
            }
        }

        // Взятие
        for (colOffset in listOf(-1, 1)) {
            val captureSquare = Square(square.row + direction, square.col + colOffset)
            if (captureSquare.row in 0..7 && captureSquare.col in 0..7) {
                val targetPiece = board[captureSquare]
                if (targetPiece != null && targetPiece.player != player) {
                    if (captureSquare.row == promotionRow) {
                        addPromotionMoves(moves, square, captureSquare)
                    } else {
                        moves.add(Move(square, captureSquare))
                    }
                }
            }
        }

        return moves
    }

    /**
     * Ходы коня
     */
    fun getKnightMoves(board: Map<Square, Piece>, square: Square, player: Player): List<Move> {
        val moves = mutableListOf<Move>()
        val knightOffsets = listOf(
            -2 to -1, -2 to 1,
            -1 to -2, -1 to 2,
            1 to -2, 1 to 2,
            2 to -1, 2 to 1
        )

        for ((rowOffset, colOffset) in knightOffsets) {
            val target = Square(square.row + rowOffset, square.col + colOffset)
            if (target.row in 0..7 && target.col in 0..7) {
                val targetPiece = board[target]
                if (targetPiece == null || targetPiece.player != player) {
                    moves.add(Move(square, target))
                }
            }
        }

        return moves
    }

    /**
     * Ходы слона
     */
    fun getBishopMoves(board: Map<Square, Piece>, square: Square, player: Player): List<Move> {
        return getSlidingMoves(board, square, player, listOf(
            -1 to -1, -1 to 1,
            1 to -1, 1 to 1
        ))
    }

    /**
     * Ходы ладьи
     */
    fun getRookMoves(board: Map<Square, Piece>, square: Square, player: Player): List<Move> {
        return getSlidingMoves(board, square, player, listOf(
            -1 to 0, 1 to 0,
            0 to -1, 0 to 1
        ))
    }

    /**
     * Ходы ферзя
     */
    fun getQueenMoves(board: Map<Square, Piece>, square: Square, player: Player): List<Move> {
        return getSlidingMoves(board, square, player, listOf(
            -1 to -1, -1 to 0, -1 to 1,
            0 to -1, 0 to 1,
            1 to -1, 1 to 0, 1 to 1
        ))
    }

    /**
     * Общий метод для скользящих фигур (слон, ладья, ферзь)
     */
    fun getSlidingMoves(
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

                if (targetPiece == null) {
                    moves.add(Move(square, target))
                } else {
                    if (targetPiece.player != player) {
                        moves.add(Move(square, target))
                    }
                    break // Дальше идти нельзя - фигура мешает
                }

                currentRow += rowDir
                currentCol += colDir
            }
        }

        return moves
    }

    /**
     * Ходы короля
     */
    fun getKingMoves(board: Map<Square, Piece>, square: Square, player: Player): List<Move> {
        val moves = mutableListOf<Move>()
        val kingOffsets = listOf(
            -1 to -1, -1 to 0, -1 to 1,
            0 to -1, 0 to 1,
            1 to -1, 1 to 0, 1 to 1
        )

        for ((rowOffset, colOffset) in kingOffsets) {
            val target = Square(square.row + rowOffset, square.col + colOffset)
            if (target.row in 0..7 && target.col in 0..7) {
                val targetPiece = board[target]
                if (targetPiece == null || targetPiece.player != player) {
                    moves.add(Move(square, target))
                }
            }
        }

        // Рокировка
        val piece = board[square] ?: return moves
        if (!piece.hasMoved && !isKingInCheck(board, player)) {
            val backRank = if (player == Player.WHITE) 0 else 7

            // Короткая рокировка (Kingside)
            val kingsideRook = board[Square(backRank, 7)]
            if (kingsideRook?.type == PieceType.ROOK &&
                kingsideRook.player == player &&
                !kingsideRook.hasMoved) {

                if (board[Square(backRank, 5)] == null &&
                    board[Square(backRank, 6)] == null) {

                    // Проверяем, что король не проходит через битое поле
                    if (!isSquareAttacked(board, Square(backRank, 5), player) &&
                        !isSquareAttacked(board, Square(backRank, 6), player)) {
                        moves.add(Move(square, Square(backRank, 6), isCastling = true))
                    }
                }
            }

            // Длинная рокировка (Queenside)
            val queensideRook = board[Square(backRank, 0)]
            if (queensideRook?.type == PieceType.ROOK &&
                queensideRook.player == player &&
                !queensideRook.hasMoved) {

                if (board[Square(backRank, 1)] == null &&
                    board[Square(backRank, 2)] == null &&
                    board[Square(backRank, 3)] == null) {

                    if (!isSquareAttacked(board, Square(backRank, 2), player) &&
                        !isSquareAttacked(board, Square(backRank, 3), player)) {
                        moves.add(Move(square, Square(backRank, 2), isCastling = true))
                    }
                }
            }
        }

        return moves
    }

    /**
     * Применить ход к доске
     */
    fun applyMove(board: Map<Square, Piece>, move: Move): Map<Square, Piece> {
        val newBoard = board.toMutableMap()
        val piece = newBoard[move.from] ?: return newBoard

        // Обработка рокировки
        if (move.isCastling) {
            // Перемещаем короля
            newBoard[move.to] = piece.copy(hasMoved = true)
            newBoard.remove(move.from)

            // Перемещаем ладью
            if (move.to.col == 6) { // Короткая рокировка
                val rookSquare = Square(move.from.row, 7)
                val rook = newBoard[rookSquare]
                newBoard[Square(move.from.row, 5)] = rook!!.copy(hasMoved = true)
                newBoard.remove(rookSquare)
            } else { // Длинная рокировка
                val rookSquare = Square(move.from.row, 0)
                val rook = newBoard[rookSquare]
                newBoard[Square(move.from.row, 3)] = rook!!.copy(hasMoved = true)
                newBoard.remove(rookSquare)
            }
            return newBoard
        }

        // Обработка взятия на проходе
        if (move.isEnPassant) {
            val capturedPawnRow = if (piece.player == Player.WHITE) move.to.row - 1 else move.to.row + 1
            newBoard.remove(Square(capturedPawnRow, move.to.col))
        }

        // Обработка превращения пешки
        if (move.promotion != null) {
            newBoard[move.to] = Piece(move.promotion, piece.player, hasMoved = true)
        } else {
            newBoard[move.to] = piece.copy(hasMoved = true)
        }

        newBoard.remove(move.from)
        return newBoard
    }

    /**
     * Проверка, находится ли король под шахом
     */
    fun isKingInCheck(board: Map<Square, Piece>, player: Player): Boolean {
        val kingSquare = board.entries.find {
            it.value.type == PieceType.KING && it.value.player == player
        }?.key ?: return true // Король не найден - считаем что шах

        val opponent = if (player == Player.WHITE) Player.BLACK else Player.WHITE
        return isSquareAttacked(board, kingSquare, player)
    }

    /**
     * Находится ли клетка под атакой противника
     */
    fun isSquareAttacked(board: Map<Square, Piece>, square: Square, defenderColor: Player): Boolean {
        val attackerColor = if (defenderColor == Player.WHITE) Player.BLACK else Player.WHITE

        for ((attackerSquare, piece) in board) {
            if (piece.player == attackerColor) {
                val moves = when (piece.type) {
                    PieceType.PAWN -> getPawnAttackSquares(attackerSquare, attackerColor)
                    else -> getRawMovesForPiece(board, attackerSquare, piece)
                }

                if (moves.any { it.to == square }) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Клетки, которые атакует пешка
     */
    private fun getPawnAttackSquares(square: Square, player: Player): List<Move> {
        val direction = if (player == Player.WHITE) 1 else -1
        val attacks = mutableListOf<Move>()

        for (colOffset in listOf(-1, 1)) {
            val target = Square(square.row + direction, square.col + colOffset)
            if (target.row in 0..7 && target.col in 0..7) {
                attacks.add(Move(square, target))
            }
        }

        return attacks
    }

    /**
     * Проверить наличие легальных ходов у игрока
     */
    fun hasLegalMoves(board: Map<Square, Piece>, player: Player): Boolean {
        for ((square, piece) in board) {
            if (piece.player == player) {
                val rawMoves = getRawMovesForPiece(board, square, piece)
                for (move in rawMoves) {
                    val newBoard = applyMove(board, move)
                    if (!isKingInCheck(newBoard, player)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * Проверка на мат
     */
    fun isCheckmate(board: Map<Square, Piece>, player: Player): Boolean {
        return isKingInCheck(board, player) && !hasLegalMoves(board, player)
    }

    /**
     * Проверка на пат
     */
    fun isStalemate(board: Map<Square, Piece>, player: Player): Boolean {
        return !isKingInCheck(board, player) && !hasLegalMoves(board, player)
    }

    /**
     * Добавление ходов с превращением пешки
     */
    private fun addPromotionMoves(moves: MutableList<Move>, from: Square, to: Square) {
        for (pieceType in listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)) {
            moves.add(Move(from, to, promotion = pieceType))
        }
    }

    /**
     * Конвертация GameState в BoardState
     */
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

    /**
     * Конвертация BoardState в GameState
     */
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
}