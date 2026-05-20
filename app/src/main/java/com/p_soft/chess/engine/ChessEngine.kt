package com.p_soft.chess.engine

import com.p_soft.chess.domain.model.BoardState
import com.p_soft.chess.domain.model.GameState
import com.p_soft.chess.domain.model.GameStatus
import com.p_soft.chess.domain.model.Move
import com.p_soft.chess.domain.model.Piece
import com.p_soft.chess.domain.model.PieceType
import com.p_soft.chess.domain.model.Player
import com.p_soft.chess.domain.model.Square
import kotlin.collections.filter
import kotlin.collections.get
import kotlin.collections.iterator
import kotlin.text.get

class ChessEngine {

    // ==================== ПУБЛИЧНЫЕ МЕТОДЫ ====================

    /**
     * Получить все легальные ходы для фигуры на указанной клетке
     */
    fun getLegalMoves(gameState: GameState, square: Square): List<Move> {
        val piece = gameState.board[square] ?: return emptyList()
        if (piece.player != gameState.currentPlayer) return emptyList()

        val rawMoves = getRawMoves(gameState.board, square, piece, gameState.enPassantTarget)

        return rawMoves.filter { move ->
            val newBoard = applyMove(gameState.board, move)
            !isKingInCheck(newBoard, gameState.currentPlayer)
        }
    }

    /**
     * Выполнить ход и вернуть новое состояние игры
     */
    fun executeMove(state: GameState, move: Move): GameState {
        val piece = state.board[move.from] ?: return state

        // Определяем взятую фигуру
        val capturedPiece = when {
            move.isEnPassant -> {
                val capturedRow = if (piece.player == Player.WHITE) move.to.row - 1 else move.to.row + 1
                state.board[Square(capturedRow, move.to.col)]
            }
            else -> state.board[move.to]
        }

        val newBoard = applyMove(state.board, move)
        val newPlayer = if (state.currentPlayer == Player.WHITE) Player.BLACK else Player.WHITE

        // Проверяем шах и наличие легальных ходов
        val isCheck = isKingInCheck(newBoard, newPlayer)
        val hasLegalMoves = hasAnyLegalMove(newBoard, newPlayer)

        val newStatus = when {
            isCheck && !hasLegalMoves -> GameStatus.CHECKMATE
            !isCheck && !hasLegalMoves -> GameStatus.STALEMATE
            isCheck -> GameStatus.CHECK
            else -> GameStatus.ACTIVE
        }

        // Определяем клетку для взятия на проходе
        val newEnPassant = if (piece.type == PieceType.PAWN &&
            kotlin.math.abs(move.from.row - move.to.row) == 2) {
            Square((move.from.row + move.to.row) / 2, move.from.col)
        } else null

        // Счётчик полуходов (для правила 50 ходов)
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

    /**
     * Применить ход к доске (без проверки правил)
     */
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

    /**
     * Проверить, находится ли король под шахом
     */
    fun isKingInCheck(board: Map<Square, Piece>, player: Player): Boolean {
        val kingSquare = board.entries.find {
            it.value.type == PieceType.KING && it.value.player == player
        }?.key ?: return true

        val opponent = if (player == Player.WHITE) Player.BLACK else Player.WHITE
        return isSquareAttacked(board, kingSquare, opponent)
    }

    /**
     * Проверить, есть ли хотя бы один легальный ход
     */
    fun hasAnyLegalMove(board: Map<Square, Piece>, player: Player): Boolean {
        for ((square, piece) in board) {
            if (piece.player == player) {
                val moves = getRawMoves(board, square, piece, null)
                for (move in moves) {
                    val newBoard = applyMove(board, move)
                    if (!isKingInCheck(newBoard, player)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    // ==================== КОНВЕРТАЦИЯ МОДЕЛЕЙ ====================

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

    // ==================== ГЕНЕРАЦИЯ ХОДОВ ====================

    /**
     * Получить все возможные ходы для фигуры (без проверки шаха)
     */
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

    /**
     * Ходы пешки
     */
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

        // Ход вперёд на одну клетку
        val oneStep = Square(square.row + direction, square.col)
        if (isValidSquare(oneStep) && board[oneStep] == null) {
            addMovesForSquare(moves, square, oneStep, promotionRow)

            // Ход на две клетки с начальной позиции
            if (square.row == startRow) {
                val twoStep = Square(square.row + 2 * direction, square.col)
                if (board[twoStep] == null) {
                    moves.add(Move(square, twoStep))
                }
            }
        }

        // Взятие
        for (colOffset in listOf(-1, 1)) {
            val captureSquare = Square(square.row + direction, square.col + colOffset)
            if (isValidSquare(captureSquare)) {
                val targetPiece = board[captureSquare]
                if (targetPiece != null && targetPiece.player != player) {
                    addMovesForSquare(moves, square, captureSquare, promotionRow)
                }
                // Взятие на проходе
                if (enPassantTarget == captureSquare) {
                    moves.add(Move(square, captureSquare, isEnPassant = true))
                }
            }
        }

        return moves
    }

    /**
     * Ходы коня
     */
    private fun getKnightMoves(board: Map<Square, Piece>, square: Square, player: Player): List<Move> {
        val offsets = listOf(
            -2 to -1, -2 to 1, -1 to -2, -1 to 2,
            1 to -2, 1 to 2, 2 to -1, 2 to 1
        )
        return getJumpMoves(board, square, player, offsets)
    }

    /**
     * Ходы слона
     */
    private fun getBishopMoves(board: Map<Square, Piece>, square: Square, player: Player): List<Move> {
        return getSlidingMoves(board, square, player, listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1))
    }

    /**
     * Ходы ладьи
     */
    private fun getRookMoves(board: Map<Square, Piece>, square: Square, player: Player): List<Move> {
        return getSlidingMoves(board, square, player, listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1))
    }

    /**
     * Ходы ферзя
     */
    private fun getQueenMoves(board: Map<Square, Piece>, square: Square, player: Player): List<Move> {
        return getSlidingMoves(board, square, player,
            listOf(-1 to -1, -1 to 0, -1 to 1, 0 to -1, 0 to 1, 1 to -1, 1 to 0, 1 to 1))
    }

    /**
     * Ходы короля (БЕЗ проверки рокировки для избежания рекурсии)
     */
    private fun getKingMoves(board: Map<Square, Piece>, square: Square, player: Player): List<Move> {
        // Только обычные ходы короля (на одну клетку)
        val offsets = listOf(
            -1 to -1, -1 to 0, -1 to 1,
            0 to -1, 0 to 1,
            1 to -1, 1 to 0, 1 to 1
        )
        return getJumpMoves(board, square, player, offsets)
    }

    /**
     * Проверить возможность рокировки (вызывается отдельно, не из getKingMoves)
     */
    fun getCastlingMoves(board: Map<Square, Piece>, kingSquare: Square, player: Player): List<Move> {
        val moves = mutableListOf<Move>()
        val piece = board[kingSquare] ?: return moves

        if (piece.hasMoved) return moves

        // Проверяем, что король не под шахом (ВАЖНО: эта проверка не вызывает getKingMoves)
        if (isSquareAttackedSimple(board, kingSquare, if (player == Player.WHITE) Player.BLACK else Player.WHITE)) {
            return moves
        }

        val backRank = if (player == Player.WHITE) 0 else 7

        // Короткая рокировка
        if (canCastle(board, player, backRank, 7, listOf(5, 6))) {
            moves.add(Move(kingSquare, Square(backRank, 6), isCastling = true))
        }

        // Длинная рокировка
        if (canCastle(board, player, backRank, 0, listOf(1, 2, 3))) {
            moves.add(Move(kingSquare, Square(backRank, 2), isCastling = true))
        }

        return moves
    }

    /**
     * Объединённый метод для получения всех ходов короля (включая рокировку)
     */
    fun getAllKingMoves(gameState: GameState, square: Square): List<Move> {
        val piece = gameState.board[square] ?: return emptyList()
        if (piece.type != PieceType.KING) return emptyList()

        val normalMoves = getKingMoves(gameState.board, square, piece.player)
        val castlingMoves = getCastlingMoves(gameState.board, square, piece.player)

        return (normalMoves + castlingMoves).filter { move ->
            val newBoard = applyMove(gameState.board, move)
            !isKingInCheck(newBoard, piece.player)
        }
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    /**
     * Ходы для фигур, которые прыгают (конь, король)
     */
    private fun getJumpMoves(
        board: Map<Square, Piece>,
        square: Square,
        player: Player,
        offsets: List<Pair<Int, Int>>
    ): List<Move> {
        val moves = mutableListOf<Move>()

        for ((rowOffset, colOffset) in offsets) {
            val target = Square(square.row + rowOffset, square.col + colOffset)
            if (isValidSquare(target)) {
                val targetPiece = board[target]
                if (targetPiece == null || targetPiece.player != player) {
                    moves.add(Move(square, target))
                }
            }
        }

        return moves
    }

    /**
     * Ходы для скользящих фигур (слон, ладья, ферзь)
     */
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
                    else -> break // Своя фигура
                }

                currentRow += rowDir
                currentCol += colDir
            }
        }

        return moves
    }

    /**
     * Проверка возможности рокировки
     */
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

        // Проверяем, что все клетки между королём и ладьёй пусты и не атакованы
        return emptyCols.all { col ->
            val sq = Square(rank, col)
            board[sq] == null && !isSquareAttackedSimple(board, sq, opponent)
        }
    }

    /**
     * Проверка, атакована ли клетка (БЕЗ проверки рокировки)
     * Используется внутри getCastlingMoves для избежания рекурсии
     */
    private fun isSquareAttackedSimple(board: Map<Square, Piece>, square: Square, attackerColor: Player): Boolean {
        for ((attackerSquare, piece) in board) {
            if (piece.player == attackerColor) {
                val moves = when (piece.type) {
                    PieceType.PAWN -> getPawnAttacks(attackerSquare, attackerColor)
                    PieceType.KNIGHT -> getKnightMoves(board, attackerSquare, attackerColor)
                    PieceType.BISHOP -> getBishopMoves(board, attackerSquare, attackerColor)
                    PieceType.ROOK -> getRookMoves(board, attackerSquare, attackerColor)
                    PieceType.QUEEN -> getQueenMoves(board, attackerSquare, attackerColor)
                    PieceType.KING -> getKingMoves(board, attackerSquare, attackerColor) // Король без рокировки
                }
                if (moves.any { it.to == square }) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Полная проверка атаки на клетку (может использоваться для проверки шаха)
     */
    private fun isSquareAttacked(board: Map<Square, Piece>, square: Square, attackerColor: Player): Boolean {
        return isSquareAttackedSimple(board, square, attackerColor)
    }

    /**
     * Атаки пешки (только по диагонали)
     */
    private fun getPawnAttacks(square: Square, player: Player): List<Move> {
        val direction = if (player == Player.WHITE) 1 else -1
        val moves = mutableListOf<Move>()

        for (colOffset in listOf(-1, 1)) {
            val target = Square(square.row + direction, square.col + colOffset)
            if (isValidSquare(target)) {
                moves.add(Move(square, target))
            }
        }

        return moves
    }

    // ==================== ИСПОЛНЕНИЕ ХОДОВ ====================

    /**
     * Выполнить рокировку
     */
    private fun executeCastling(board: MutableMap<Square, Piece>, piece: Piece, move: Move) {
        // Перемещаем короля
        board[move.to] = piece.copy(hasMoved = true)
        board.remove(move.from)

        // Перемещаем ладью
        val (rookFrom, rookTo) = if (move.to.col == 6) {
            Square(move.from.row, 7) to Square(move.from.row, 5)
        } else {
            Square(move.from.row, 0) to Square(move.from.row, 3)
        }

        val rook = board[rookFrom]
        if (rook != null) {
            board[rookTo] = rook.copy(hasMoved = true)
            board.remove(rookFrom)
        }
    }

    /**
     * Выполнить взятие на проходе
     */
    private fun executeEnPassant(board: MutableMap<Square, Piece>, piece: Piece, move: Move) {
        val capturedRow = if (piece.player == Player.WHITE) move.to.row - 1 else move.to.row + 1
        board.remove(Square(capturedRow, move.to.col))
        board[move.to] = piece.copy(hasMoved = true)
        board.remove(move.from)
    }

    /**
     * Выполнить превращение пешки
     */
    private fun executePromotion(board: MutableMap<Square, Piece>, piece: Piece, move: Move) {
        board[move.to] = Piece(move.promotion!!, piece.player, hasMoved = true)
        board.remove(move.from)
    }

    /**
     * Выполнить обычный ход
     */
    private fun executeNormalMove(board: MutableMap<Square, Piece>, piece: Piece, move: Move) {
        board[move.to] = piece.copy(hasMoved = true)
        board.remove(move.from)
    }

    // ==================== УТИЛИТЫ ====================

    /**
     * Добавить ходы с учётом превращения пешки
     */
    private fun addMovesForSquare(
        moves: MutableList<Move>,
        from: Square,
        to: Square,
        promotionRow: Int
    ) {
        if (to.row == promotionRow) {
            // Все возможные превращения
            for (type in listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)) {
                moves.add(Move(from, to, promotion = type))
            }
        } else {
            moves.add(Move(from, to))
        }
    }

    /**
     * Проверить, что клетка находится на доске
     */
    private fun isValidSquare(square: Square): Boolean {
        return square.row in 0..7 && square.col in 0..7
    }
}