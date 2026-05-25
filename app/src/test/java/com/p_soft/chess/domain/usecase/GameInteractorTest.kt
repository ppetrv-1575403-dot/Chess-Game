package com.p_soft.chess.domain.usecase

import com.p_soft.chess.domain.model.GameState
import com.p_soft.chess.domain.model.GameStatus
import com.p_soft.chess.domain.model.Move
import com.p_soft.chess.domain.model.Piece
import com.p_soft.chess.domain.model.PieceType
import com.p_soft.chess.domain.model.Player
import com.p_soft.chess.domain.model.Square
import com.p_soft.chess.domain.repository.GameRepository
import com.p_soft.chess.engine.ChessEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import io.mockk.*

@OptIn(ExperimentalCoroutinesApi::class)
class GameInteractorTest {

    private lateinit var gameRepository: GameRepository
    private lateinit var chessEngine: ChessEngine
    private lateinit var gameInteractor: GameInteractor
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        gameRepository = mockk(relaxed = true)
        chessEngine = ChessEngine()
        gameInteractor = GameInteractor(gameRepository, chessEngine)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startNewGame resets state to initial`() = runTest {
        gameInteractor.startNewGame()
        val state = gameInteractor.gameState.first()

        assertEquals(GameStatus.ACTIVE, state.status)
        assertEquals(Player.WHITE, state.currentPlayer)
    }

    @Test
    fun `makeMove returns true for valid move`() = runTest {
        gameInteractor.startNewGame()
        val move = Move(Square(1, 0), Square(2, 0))

        val result = gameInteractor.makeMove(move)
        assertTrue(result)
    }

    @Test
    fun `makeMove returns false for invalid move`() = runTest {
        gameInteractor.startNewGame()
        val move = Move(Square(1, 0), Square(5, 0)) // Слишком далеко

        val result = gameInteractor.makeMove(move)
        assertFalse(result)
    }

    @Test
    fun `makeMove updates game state`() = runTest {
        gameInteractor.startNewGame()
        val move = Move(Square(1, 0), Square(3, 0)) // a2-a4

        gameInteractor.makeMove(move)
        val state = gameInteractor.gameState.first()

        assertEquals(Player.BLACK, state.currentPlayer)
        assertEquals(1, state.moveHistory.size)
        assertNotNull(state.board[Square(3, 0)])
    }

    @Test
    fun `undoMove restores previous state`() = runTest {
        gameInteractor.startNewGame()
        val initialState = gameInteractor.gameState.first()
        val move = Move(Square(1, 0), Square(3, 0))

        gameInteractor.makeMove(move)
        gameInteractor.undoMove()

        val restoredState = gameInteractor.gameState.first()
        assertEquals(initialState.currentPlayer, restoredState.currentPlayer)
        assertEquals(0, restoredState.moveHistory.size)
    }

    @Test
    fun `resetGame clears everything`() = runTest {
        gameInteractor.startNewGame()
        gameInteractor.makeMove(Move(Square(1, 0), Square(3, 0)))
        gameInteractor.resetGame()

        val state = gameInteractor.gameState.first()
        assertEquals(GameStatus.NOT_STARTED, state.status)
        assertEquals(0, state.moveHistory.size)
    }

    @Test
    fun `makeMove with pawn promotion sets pendingPromotion`() = runTest {
        // Создаём позицию с пешкой на предпоследней горизонтали
        val board = mapOf(
            Square(6, 0) to Piece(PieceType.PAWN, Player.WHITE),
            Square(0, 4) to Piece(PieceType.KING, Player.WHITE),
            Square(7, 4) to Piece(PieceType.KING, Player.BLACK)
        )
        val state = GameState.initial().start().copy(
            board = board,
            currentPlayer = Player.WHITE
        )

        // Устанавливаем состояние напрямую
        val field = GameInteractor::class.java.getDeclaredField("_gameState")
        field.isAccessible = true
        field.set(gameInteractor, kotlinx.coroutines.flow.MutableStateFlow(state))

        val move = Move(Square(6, 0), Square(7, 0))
        gameInteractor.makeMove(move)

        val resultState = gameInteractor.gameState.first()
        assertNotNull(resultState.pendingPromotion)
    }
}