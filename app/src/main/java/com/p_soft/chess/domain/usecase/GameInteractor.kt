package com.p_soft.chess.domain.usecase

import com.p_soft.chess.domain.model.BoardState
import com.p_soft.chess.domain.model.GameState
import com.p_soft.chess.domain.model.GameStatus
import com.p_soft.chess.domain.model.Move
import com.p_soft.chess.domain.model.PieceType
import com.p_soft.chess.domain.model.Player
import com.p_soft.chess.domain.model.Square
import com.p_soft.chess.domain.repository.GameRepository
import com.p_soft.chess.engine.ChessEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import javax.inject.Inject
import javax.inject.Singleton

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Singleton
class GameInteractor @Inject constructor(
    private val gameRepository: GameRepository,
    private val chessEngine: ChessEngine
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _gameState = MutableStateFlow(GameState.initial())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    fun startNewGame() {
        _gameState.value = GameState.initial().start()
        scope.launch {
            try {
                gameRepository.clearGame()
            } catch (e: Exception) {
                // Логирование ошибки
                e.printStackTrace()
            }
        }
    }

    fun getValidMoves(square: Square): List<Move> {
        return chessEngine.getLegalMovesForGameState(_gameState.value, square)
    }

    fun makeMove(move: Move): Boolean {
        val currentState = _gameState.value

        // Проверяем, что игра активна
        if (!currentState.status.requiresPlayerAction()) {
            return false
        }

        // Проверяем легальность хода
        val legalMoves = chessEngine.getLegalMovesForGameState(currentState, move.from)
        if (legalMoves.none { it.to == move.to && it.promotion == move.promotion }) {
            return false
        }

        // Если нужен выбор фигуры для превращения
        val piece = currentState.board[move.from]
        if (piece?.type == PieceType.PAWN &&
            move.to.row == (if (piece.player == Player.WHITE) 7 else 0) &&
            move.promotion == null) {
            _gameState.value = currentState.copy(pendingPromotion = move)
            return true
        }

        // Выполняем ход
        val newState = executeMove(currentState, move)
        _gameState.value = newState

        // Асинхронно сохраняем игру
        scope.launch {
            try {
                val boardState = chessEngine.gameStateToBoardState(newState)
                gameRepository.saveGame(boardState)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return true
    }

    private fun executeMove(state: GameState, move: Move): GameState {
        val newBoard = chessEngine.applyMove(state.board, move)
        val newPlayer = if (state.currentPlayer == Player.WHITE) Player.BLACK else Player.WHITE

        // Определяем статус после хода
        val isCheck = chessEngine.isKingInCheck(newBoard, newPlayer)
        val hasLegalMoves = chessEngine.hasLegalMoves(newBoard, newPlayer)

        val newStatus = when {
            isCheck && !hasLegalMoves -> GameStatus.CHECKMATE
            !isCheck && !hasLegalMoves -> GameStatus.STALEMATE
            isCheck -> GameStatus.CHECK
            else -> GameStatus.ACTIVE
        }

        // Обновляем счётчики
        val newHalfMoveClock = if (move.isCapture() ||
            state.board[move.from]?.type == PieceType.PAWN) {
            0
        } else {
            state.halfMoveClock + 1
        }

        val newFullMoveNumber = if (state.currentPlayer == Player.BLACK) {
            state.fullMoveNumber + 1
        } else {
            state.fullMoveNumber
        }

        return state.copy(
            board = newBoard,
            currentPlayer = newPlayer,
            moveHistory = state.moveHistory + move,
            status = newStatus,
            capturedPieces = state.capturedPieces +
                    (state.board[move.to]?.let { listOf(it) } ?: emptyList()),
            moveCount = state.moveCount + 1,
            halfMoveClock = newHalfMoveClock,
            fullMoveNumber = newFullMoveNumber,
            pendingPromotion = null,
            enPassantTarget = if (state.board[move.from]?.type == PieceType.PAWN &&
                kotlin.math.abs(move.from.row - move.to.row) == 2) {
                Square((move.from.row + move.to.row) / 2, move.from.col)
            } else null
        )
    }

    fun completePromotion(pieceType: PieceType) {
        val currentState = _gameState.value
        val pendingMove = currentState.pendingPromotion ?: return

        val completedMove = pendingMove.copy(promotion = pieceType)
        makeMove(completedMove)
    }

    fun cancelPromotion() {
        _gameState.value = _gameState.value.copy(pendingPromotion = null)
    }

    fun undoMove() {
        val currentState = _gameState.value
        if (currentState.moveHistory.isEmpty() || !currentState.status.requiresPlayerAction()) {
            return
        }

        val newHistory = currentState.moveHistory.dropLast(1)
        var state = GameState.initial().start()

        for (move in newHistory) {
            state = executeMove(state, move)
        }

        _gameState.value = state
    }

    fun resetGame() {
        _gameState.value = GameState.initial()
        scope.launch {
            try {
                gameRepository.clearGame()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadGame() {
        scope.launch {
            try {
                val boardState = gameRepository.loadGame()
                if (boardState != null) {
                    _gameState.value = chessEngine.boardStateToGameState(boardState)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}