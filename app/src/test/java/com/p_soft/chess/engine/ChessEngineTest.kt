package com.p_soft.chess.engine

import com.p_soft.chess.domain.model.GameState
import com.p_soft.chess.domain.model.GameStatus
import com.p_soft.chess.domain.model.Move
import com.p_soft.chess.domain.model.Piece
import com.p_soft.chess.domain.model.PieceType
import com.p_soft.chess.domain.model.Player
import com.p_soft.chess.domain.model.Square
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ChessEngineTest {

    private lateinit var engine: ChessEngine
    private var initialState: GameState = GameState.initial()

    @Before
    fun setUp() {
        engine = ChessEngine()
        initialState = GameState.initial().start()
    }

    // ==================== ТЕСТЫ НАЧАЛЬНОЙ ПОЗИЦИИ ====================

    @Test
    fun `initial board has 32 pieces`() {
        assertEquals(32, initialState.board.size)
    }

    @Test
    fun `initial board has correct piece distribution`() {
        val whitePawns = initialState?.board?.values?.count {
            it.type == PieceType.PAWN && it.player == Player.WHITE
        }
        val blackPawns = initialState?.board?.values?.count {
            it.type == PieceType.PAWN && it.player == Player.BLACK
        }

        assertEquals(8, whitePawns)
        assertEquals(8, blackPawns)
    }

    @Test
    fun `initial board has correct back row pieces`() {
        val backRowPieces = listOf(
            PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
            PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
        )

        for (col in 0..7) {
            val whitePiece = initialState?.board[Square(0, col)]
            val blackPiece = initialState?.board[Square(7, col)]

            assertNotNull("White piece missing at col $col", whitePiece)
            assertNotNull("Black piece missing at col $col", blackPiece)
            assertEquals(backRowPieces[col], whitePiece?.type)
            assertEquals(backRowPieces[col], blackPiece?.type)
        }
    }

    @Test
    fun `white moves first`() {
        assertEquals(Player.WHITE, initialState?.currentPlayer)
    }

    @Test
    fun `game starts as NOT_STARTED then becomes ACTIVE`() {
        val notStarted = GameState.initial()
        assertEquals(GameStatus.NOT_STARTED, notStarted.status)

        val active = notStarted.start()
        assertEquals(GameStatus.ACTIVE, active.status)
    }

    // ==================== ТЕСТЫ ХОДОВ ПЕШКИ ====================

    @Test
    fun `pawn can move one step forward`() {
        val moves = engine.getLegalMoves(initialState, Square(1, 0)) // a2
        assertTrue(moves.any { it.to == Square(2, 0) })
    }

    @Test
    fun `pawn can move two steps from starting position`() {
        val moves = engine.getLegalMoves(initialState, Square(1, 0)) // a2
        assertTrue(moves.any { it.to == Square(3, 0) })
    }

    @Test
    fun `pawn cannot move two steps if blocked`() {
        // Создаём позицию с заблокированной пешкой
        val blockedState = initialState.copy(
            board = initialState.board + (Square(2, 0) to Piece(PieceType.PAWN, Player.WHITE))
        )
        val moves = engine.getLegalMoves(blockedState, Square(1, 0))
        assertFalse(moves.any { it.to == Square(3, 0) })
    }

    @Test
    fun `pawn cannot move forward if blocked`() {
        val blockedState = initialState.copy(
            board = initialState.board + (Square(2, 0) to Piece(PieceType.PAWN, Player.BLACK))
        )
        val moves = engine.getLegalMoves(blockedState, Square(1, 0))
        assertFalse(moves.any { it.to == Square(2, 0) })
    }

    @Test
    fun `pawn can capture diagonally`() {
        // Ставим чёрную пешку на b3
        val state = initialState.copy(
            board = initialState.board +
                    (Square(2, 1) to Piece(PieceType.PAWN, Player.BLACK))
        )
        val moves = engine.getLegalMoves(state, Square(1, 0)) // a2
        assertTrue(moves.any { it.to == Square(2, 1) })
    }

    @Test
    fun `pawn cannot capture forward`() {
        val state = initialState.copy(
            board = initialState.board +
                    (Square(2, 0) to Piece(PieceType.PAWN, Player.BLACK))
        )
        val moves = engine.getLegalMoves(state, Square(1, 0))
        assertFalse(moves.any { it.to == Square(2, 0) }) // Не может бить вперёд
    }

    @Test
    fun `pawn cannot capture own piece`() {
        val state = initialState.copy(
            board = initialState.board +
                    (Square(2, 1) to Piece(PieceType.PAWN, Player.WHITE))
        )
        val moves = engine.getLegalMoves(state, Square(1, 0))
        assertFalse(moves.any { it.to == Square(2, 1) })
    }

    // ==================== ТЕСТЫ ПРЕВРАЩЕНИЯ ПЕШКИ ====================

    @Test
    fun `pawn promotion generates four options`() {
        // Ставим белую пешку на a7 (предпоследний ряд)
        val state = initialState.copy(
            board = mapOf(
                Square(6, 0) to Piece(PieceType.PAWN, Player.WHITE),
                Square(0, 4) to Piece(PieceType.KING, Player.WHITE),
                Square(7, 4) to Piece(PieceType.KING, Player.BLACK)
            ),
            currentPlayer = Player.WHITE
        )
        val moves = engine.getLegalMoves(state, Square(6, 0))

        val promotionMoves = moves.filter { it.promotion != null }
        assertEquals(4, promotionMoves.size) // Q, R, B, N
    }

    @Test
    fun `pawn promotion includes queen option`() {
        val state = createPromotionPosition(Player.WHITE)
        val moves = engine.getLegalMoves(state, Square(6, 0))
        assertTrue(moves.any { it.promotion == PieceType.QUEEN })
    }

    @Test
    fun `pawn promotion includes knight option`() {
        val state = createPromotionPosition(Player.WHITE)
        val moves = engine.getLegalMoves(state, Square(6, 0))
        assertTrue(moves.any { it.promotion == PieceType.KNIGHT })
    }

    // ==================== ТЕСТЫ ХОДОВ КОНЯ ====================

    @Test
    fun `knight has up to eight moves from center`() {
        // Ставим коня в центр доски
        val state = initialState.copy(
            board = mapOf(
                Square(4, 4) to Piece(PieceType.KNIGHT, Player.WHITE),
                Square(0, 4) to Piece(PieceType.KING, Player.WHITE),
                Square(7, 4) to Piece(PieceType.KING, Player.BLACK)
            ),
            currentPlayer = Player.WHITE
        )
        val moves = engine.getLegalMoves(state, Square(4, 4))
        assertEquals(8, moves.size)
    }

    @Test
    fun `knight can jump over pieces`() {
        // Создаём позицию с фигурами вокруг коня
        val board = mutableMapOf<Square, Piece>()
        board[Square(3, 3)] = Piece(PieceType.KNIGHT, Player.WHITE)
        board[Square(2, 2)] = Piece(PieceType.PAWN, Player.WHITE) // Блокирует
        board[Square(2, 4)] = Piece(PieceType.PAWN, Player.WHITE)
        board[Square(4, 2)] = Piece(PieceType.PAWN, Player.WHITE)
        board[Square(4, 4)] = Piece(PieceType.PAWN, Player.WHITE)
        board[Square(1, 2)] = Piece(PieceType.PAWN, Player.BLACK) // Цель
        board[Square(1, 4)] = Piece(PieceType.PAWN, Player.BLACK)
        board[Square(5, 2)] = Piece(PieceType.PAWN, Player.BLACK)
        board[Square(5, 4)] = Piece(PieceType.PAWN, Player.BLACK)
        board[Square(0, 0)] = Piece(PieceType.KING, Player.WHITE)
        board[Square(7, 7)] = Piece(PieceType.KING, Player.BLACK)

        val state = initialState.copy(board = board, currentPlayer = Player.WHITE)
        val moves = engine.getLegalMoves(state, Square(3, 3))

        // Конь должен иметь 8 возможных ходов
        assertEquals(8, moves.size)
    }

    // ==================== ТЕСТЫ СКОЛЬЗЯЩИХ ФИГУР ====================

    @Test
    fun `bishop moves diagonally only`() {
        val state = initialState.copy(
            board = mapOf(
                Square(4, 4) to Piece(PieceType.BISHOP, Player.WHITE),
                Square(0, 4) to Piece(PieceType.KING, Player.WHITE),
                Square(7, 4) to Piece(PieceType.KING, Player.BLACK)
            ),
            currentPlayer = Player.WHITE
        )
        val moves = engine.getLegalMoves(state, Square(4, 4))

        // Все ходы должны быть диагональными
        moves.forEach { move ->
            val rowDiff = kotlin.math.abs(move.to.row - move.from.row)
            val colDiff = kotlin.math.abs(move.to.col - move.from.col)
            assertEquals(rowDiff, colDiff)
        }
    }

    @Test
    fun `rook moves horizontally and vertically only`() {
        val state = initialState.copy(
            board = mapOf(
                Square(4, 4) to Piece(PieceType.ROOK, Player.WHITE),
                Square(0, 4) to Piece(PieceType.KING, Player.WHITE),
                Square(7, 4) to Piece(PieceType.KING, Player.BLACK)
            ),
            currentPlayer = Player.WHITE
        )
        val moves = engine.getLegalMoves(state, Square(4, 4))

        // Все ходы должны быть горизонтальными или вертикальными
        moves.forEach { move ->
            val rowDiff = kotlin.math.abs(move.to.row - move.from.row)
            val colDiff = kotlin.math.abs(move.to.col - move.from.col)
            assertTrue(rowDiff == 0 || colDiff == 0)
        }
    }

    @Test
    fun `queen has both diagonal and straight moves`() {
        val state = initialState.copy(
            board = mapOf(
                Square(3, 3) to Piece(PieceType.QUEEN, Player.WHITE),
                Square(0, 4) to Piece(PieceType.KING, Player.WHITE),
                Square(7, 4) to Piece(PieceType.KING, Player.BLACK)
            ),
            currentPlayer = Player.WHITE
        )
        val moves = engine.getLegalMoves(state, Square(3, 3))

        val hasDiagonal = moves.any {
            val rd = kotlin.math.abs(it.to.row - it.from.row)
            val cd = kotlin.math.abs(it.to.col - it.from.col)
            rd == cd && rd > 0
        }
        val hasStraight = moves.any {
            it.to.row == it.from.row || it.to.col == it.from.col
        }

        assertTrue(hasDiagonal)
        assertTrue(hasStraight)
    }

    // ==================== ТЕСТЫ ХОДОВ КОРОЛЯ ====================

    @Test
    fun `king moves one square in any direction`() {
        val state = initialState.copy(
            board = mapOf(
                Square(4, 4) to Piece(PieceType.KING, Player.WHITE),
                Square(7, 4) to Piece(PieceType.KING, Player.BLACK)
            ),
            currentPlayer = Player.WHITE
        )
        val moves = engine.getLegalMoves(state, Square(4, 4))

        moves.forEach { move ->
            val rowDiff = kotlin.math.abs(move.to.row - move.from.row)
            val colDiff = kotlin.math.abs(move.to.col - move.from.col)
            assertTrue(rowDiff <= 1 && colDiff <= 1)
        }
    }

    @Test
    fun `king cannot move into check`() {
        // Ставим короля напротив вражеской ладьи
        val board = mapOf(
            Square(4, 4) to Piece(PieceType.KING, Player.WHITE),
            Square(4, 0) to Piece(PieceType.ROOK, Player.BLACK), // Ладья атакует ряд
            Square(7, 7) to Piece(PieceType.KING, Player.BLACK)
        )
        val state = initialState.copy(board = board, currentPlayer = Player.WHITE)
        val moves = engine.getLegalMoves(state, Square(4, 4))

        // Король не может остаться на 4-м ряду (кроме d4)
        moves.forEach { move ->
            if (move.to.row == 4) {
                assertEquals(4, move.to.col) // Только исходная позиция
            }
        }
    }

    // ==================== ТЕСТЫ ВЗЯТИЯ НА ПРОХОДЕ ====================

    @Test
    fun `en passant is possible after double pawn move`() {
        // Делаем двойной ход чёрной пешкой
        val state = initialState.copy(
            board = initialState.board +
                    (Square(4, 1) to Piece(PieceType.PAWN, Player.BLACK, hasMoved = true)),
            enPassantTarget = Square(5, 1),
            currentPlayer = Player.WHITE,
            moveHistory = listOf(Move(Square(6, 1), Square(4, 1)))
        )

        // Белая пешка на b5
        val whitePawnState = state.copy(
            board = state.board + (Square(4, 0) to Piece(PieceType.PAWN, Player.WHITE))
        )

        val moves = engine.getLegalMoves(whitePawnState, Square(4, 0))
        assertTrue(moves.any { it.isEnPassant && it.to == Square(5, 1) })
    }

    @Test
    fun `en passant captures the correct pawn`() {
        val state = initialState.copy(
            board = mapOf(
                Square(4, 0) to Piece(PieceType.PAWN, Player.WHITE),
                Square(4, 1) to Piece(PieceType.PAWN, Player.BLACK, hasMoved = true),
                Square(0, 4) to Piece(PieceType.KING, Player.WHITE),
                Square(7, 4) to Piece(PieceType.KING, Player.BLACK)
            ),
            enPassantTarget = Square(5, 1),
            currentPlayer = Player.WHITE
        )

        val enPassantMove = Move(Square(4, 0), Square(5, 1), isEnPassant = true)
        val newBoard = engine.applyMove(state.board, enPassantMove)

        // Пешка на b5 должна быть удалена
        assertNull(newBoard[Square(4, 1)])
        // Белая пешка должна быть на b6
        assertNotNull(newBoard[Square(5, 1)])
        assertEquals(PieceType.PAWN, newBoard[Square(5, 1)]?.type)
        assertEquals(Player.WHITE, newBoard[Square(5, 1)]?.player)
    }

    // ==================== ТЕСТЫ ШАХА И МАТА ====================

    @Test
    fun `isKingInCheck detects check by rook`() {
        val board = mapOf(
            Square(4, 4) to Piece(PieceType.KING, Player.WHITE),
            Square(4, 0) to Piece(PieceType.ROOK, Player.BLACK),
            Square(7, 7) to Piece(PieceType.KING, Player.BLACK)
        )

        assertTrue(engine.isKingInCheck(board, Player.WHITE))
    }

    @Test
    fun `isKingInCheck returns false when not in check`() {
        val board = mapOf(
            Square(4, 4) to Piece(PieceType.KING, Player.WHITE),
            Square(4, 0) to Piece(PieceType.ROOK, Player.BLACK),
            Square(3, 4) to Piece(PieceType.PAWN, Player.WHITE), // Блокирует
            Square(7, 7) to Piece(PieceType.KING, Player.BLACK)
        )

        assertFalse(engine.isKingInCheck(board, Player.WHITE))
    }

    @Test
    fun `checkmate is detected correctly`() {
        // Классический мат: король в углу, атакован ферзём и защищён королём
        val board = mapOf(
            Square(0, 0) to Piece(PieceType.KING, Player.BLACK),
            Square(1, 1) to Piece(PieceType.KING, Player.WHITE),
            Square(0, 2) to Piece(PieceType.QUEEN, Player.WHITE)
        )
        val state = initialState.copy(board = board, currentPlayer = Player.BLACK)

        val result = engine.executeMove(state, Move(Square(1, 1), Square(1, 2))) // Ход белых
        assertEquals(GameStatus.CHECKMATE, result.status)
    }

    @Test
    fun `stalemate is detected correctly`() {
        // Пат: король заблокирован, но не под шахом
        val board = mapOf(
            Square(0, 0) to Piece(PieceType.KING, Player.BLACK),
            Square(2, 1) to Piece(PieceType.KING, Player.WHITE),
            Square(1, 2) to Piece(PieceType.QUEEN, Player.WHITE)
        )
        val state = initialState.copy(board = board, currentPlayer = Player.BLACK)

        // Проверяем, что нет легальных ходов
        assertFalse(engine.hasAnyLegalMove(board, Player.BLACK))
    }

    // ==================== ТЕСТЫ ПРИМЕНЕНИЯ ХОДОВ ====================

    @Test
    fun `applyMove updates piece position`() {
        val move = Move(Square(1, 0), Square(3, 0)) // a2-a4
        val newBoard = engine.applyMove(initialState.board, move)

        assertNull(newBoard[Square(1, 0)]) // Исходная клетка пуста
        assertNotNull(newBoard[Square(3, 0)]) // Целевая клетка занята
        assertEquals(PieceType.PAWN, newBoard[Square(3, 0)]?.type)
    }

    @Test
    fun `applyMove marks piece as moved`() {
        val move = Move(Square(1, 0), Square(3, 0))
        val newBoard = engine.applyMove(initialState.board, move)

        assertTrue(newBoard[Square(3, 0)]?.hasMoved ?: false)
    }

    @Test
    fun `applyMove captures enemy piece`() {
        val board = initialState.board +
                (Square(3, 1) to Piece(PieceType.PAWN, Player.BLACK))
        val move = Move(Square(2, 0), Square(3, 1)) // Белая пешка бьёт чёрную
        val newBoard = engine.applyMove(board, move)

        assertNotNull(newBoard[Square(3, 1)])
        assertEquals(Player.WHITE, newBoard[Square(3, 1)]?.player)
    }

    // ==================== ТЕСТЫ РОКИРОВКИ ====================

    @Test
    fun `king can castle kingside`() {
        val board = mapOf(
            Square(0, 4) to Piece(PieceType.KING, Player.WHITE),
            Square(0, 7) to Piece(PieceType.ROOK, Player.WHITE),
            Square(7, 4) to Piece(PieceType.KING, Player.BLACK)
        )
        val state = initialState.copy(board = board, currentPlayer = Player.WHITE)

        val kingMoves = engine.getAllKingMoves(state, Square(0, 4))
        assertTrue(kingMoves.any { it.isCastling && it.to == Square(0, 6) })
    }

    @Test
    fun `king can castle queenside`() {
        val board = mapOf(
            Square(0, 4) to Piece(PieceType.KING, Player.WHITE),
            Square(0, 0) to Piece(PieceType.ROOK, Player.WHITE),
            Square(7, 4) to Piece(PieceType.KING, Player.BLACK)
        )
        val state = initialState.copy(board = board, currentPlayer = Player.WHITE)

        val kingMoves = engine.getAllKingMoves(state, Square(0, 4))
        assertTrue(kingMoves.any { it.isCastling && it.to == Square(0, 2) })
    }

    @Test
    fun `cannot castle through check`() {
        val board = mapOf(
            Square(0, 4) to Piece(PieceType.KING, Player.WHITE),
            Square(0, 7) to Piece(PieceType.ROOK, Player.WHITE),
            Square(1, 5) to Piece(PieceType.ROOK, Player.BLACK), // Атакует f1
            Square(7, 4) to Piece(PieceType.KING, Player.BLACK)
        )
        val state = initialState.copy(board = board, currentPlayer = Player.WHITE)

        val kingMoves = engine.getAllKingMoves(state, Square(0, 4))
        assertFalse(kingMoves.any { it.isCastling })
    }

    @Test
    fun `cannot castle if king has moved`() {
        val board = mapOf(
            Square(0, 4) to Piece(PieceType.KING, Player.WHITE, hasMoved = true),
            Square(0, 7) to Piece(PieceType.ROOK, Player.WHITE),
            Square(7, 4) to Piece(PieceType.KING, Player.BLACK)
        )
        val state = initialState.copy(board = board, currentPlayer = Player.WHITE)

        val kingMoves = engine.getAllKingMoves(state, Square(0, 4))
        assertFalse(kingMoves.any { it.isCastling })
    }

    @Test
    fun `cannot castle if rook has moved`() {
        val board = mapOf(
            Square(0, 4) to Piece(PieceType.KING, Player.WHITE),
            Square(0, 7) to Piece(PieceType.ROOK, Player.WHITE, hasMoved = true),
            Square(7, 4) to Piece(PieceType.KING, Player.BLACK)
        )
        val state = initialState.copy(board = board, currentPlayer = Player.WHITE)

        val kingMoves = engine.getAllKingMoves(state, Square(0, 4))
        assertFalse(kingMoves.any { it.isCastling })
    }

    // ==================== ТЕСТЫ ВЫПОЛНЕНИЯ ХОДА ====================

    @Test
    fun `executeMove switches player`() {
        val move = Move(Square(1, 0), Square(2, 0)) // a2-a3
        val result = engine.executeMove(initialState, move)

        assertEquals(Player.BLACK, result.currentPlayer)
    }

    @Test
    fun `executeMove increments move count`() {
        val move = Move(Square(1, 0), Square(2, 0))
        val result = engine.executeMove(initialState, move)

        assertEquals(1, result.moveCount)
    }

    @Test
    fun `executeMove adds to move history`() {
        val move = Move(Square(1, 0), Square(2, 0))
        val result = engine.executeMove(initialState, move)

        assertEquals(1, result.moveHistory.size)
        assertEquals(move, result.moveHistory.first())
    }

    @Test
    fun `executeMove captures piece correctly`() {
        val board = initialState.board +
                (Square(3, 1) to Piece(PieceType.PAWN, Player.BLACK))
        val state = initialState.copy(
            board = board + (Square(2, 0) to Piece(PieceType.PAWN, Player.WHITE)),
            currentPlayer = Player.WHITE
        )

        val move = Move(Square(2, 0), Square(3, 1))
        val result = engine.executeMove(state, move)

        assertEquals(1, result.capturedPieces.size)
        assertEquals(PieceType.PAWN, result.capturedPieces.first().type)
        assertEquals(Player.BLACK, result.capturedPieces.first().player)
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    private fun createPromotionPosition(player: Player): GameState {
        val pawnRow = if (player == Player.WHITE) 6 else 1
        val kingRow = if (player == Player.WHITE) 0 else 7
        val opponentKingRow = if (player == Player.WHITE) 7 else 0

        return initialState.copy(
            board = mapOf(
                Square(pawnRow, 0) to Piece(PieceType.PAWN, player),
                Square(kingRow, 4) to Piece(PieceType.KING, player),
                Square(opponentKingRow, 4) to Piece(PieceType.KING,
                    if (player == Player.WHITE) Player.BLACK else Player.WHITE)
            ),
            currentPlayer = player
        )
    }
}