package com.p_soft.chess.domain.usecase

import com.p_soft.chess.domain.model.GameState
import com.p_soft.chess.domain.model.Move
import com.p_soft.chess.domain.model.PieceType
import com.p_soft.chess.domain.model.Player
import com.p_soft.chess.domain.model.Square
import com.p_soft.chess.domain.repository.GameRepository
import com.p_soft.chess.engine.ChessEngine
import com.p_soft.chess.presentation.utils.getAvailablePromotions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.text.get

@Singleton
class GameInteractor @Inject constructor(
    private val gameRepository: GameRepository,
    private val chessEngine: ChessEngine
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _gameState = MutableStateFlow(GameState.initial())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    val noHistory: Boolean
        get() {
            val currentState = _gameState.value
            return currentState.moveHistory.isEmpty()
        }

    fun startNewGame() {
        _gameState.value = GameState.initial().start()
        saveGameAsync()
    }

    fun getValidMoves(square: Square): List<Move> {
        val currentState = _gameState.value
        val piece = currentState.board[square] ?: return emptyList()

        return if (piece.type == PieceType.KING) {
            chessEngine.getAllKingMoves(currentState, square)
        } else {
            chessEngine.getLegalMoves(currentState, square)
        }
    }

    /**
     * Сделать ход. Автоматически обрабатывает превращение пешки.
     */
    fun makeMove(move: Move): Boolean {
        val currentState = _gameState.value

        if (!currentState.status.requiresPlayerAction()) return false

        val piece = currentState.board[move.from] ?: return false
        val legalMoves = if (piece.type == PieceType.KING) {
            chessEngine.getAllKingMoves(currentState, move.from)
        } else {
            chessEngine.getLegalMoves(currentState, move.from)
        }

        // Проверяем, есть ли такой ход в легальных
        val matchingMove = legalMoves.find { it.to == move.to && it.promotion == move.promotion }

        // Если ход пешки на последнюю горизонталь
        if (piece.type == PieceType.PAWN && move.to.row == (if (piece.player == Player.WHITE) 7 else 0)) {
            // Получаем доступные фигуры для превращения
            val availablePromotions = getAvailablePromotions(piece.player, currentState.board)

            if (availablePromotions.isEmpty()) {
                // Нет доступных фигур — пешка остаётся пешкой (не превращается)
                val simpleMove = move.copy(promotion = null)
                val simpleMatchingMove = legalMoves.find { it.to == move.to && it.promotion == null }

                if (simpleMatchingMove != null) {
                    _gameState.value = chessEngine.executeMove(currentState, simpleMatchingMove)
                    saveGameAsync()
                    return true
                }
                return false
            }

            // Есть доступные фигуры
            if (availablePromotions.size == 1) {
                // Только одна фигура доступна — превращаем автоматически
                val autoPromotionType = availablePromotions.first().first
                val autoMove = move.copy(promotion = autoPromotionType)
                val autoMatchingMove = legalMoves.find { it.to == move.to && it.promotion == autoPromotionType }

                if (autoMatchingMove != null) {
                    _gameState.value = chessEngine.executeMove(currentState, autoMatchingMove)
                    saveGameAsync()
                    return true
                }
                return false
            }

            // Несколько фигур доступны — показываем диалог
            _gameState.value = currentState.copy(pendingPromotion = move.copy(promotion = null))
            return true
        }

        // Обычный ход
        if (matchingMove != null) {
            _gameState.value = chessEngine.executeMove(currentState, matchingMove)
            saveGameAsync()
            return true
        }

        return false
    }

    fun completePromotion(pieceType: PieceType) {
        val currentState = _gameState.value
        val pendingMove = currentState.pendingPromotion ?: return

        // Проверяем доступность фигуры
        val availablePromotions = getAvailablePromotions(
            currentState.currentPlayer,
            currentState.board
        )

        val isAllowed = availablePromotions.any { it.first == pieceType }
        if (!isAllowed) return

        val completeMove = pendingMove.copy(promotion = pieceType)

        val piece = currentState.board[pendingMove.from] ?: return
        val legalMoves = if (piece.type == PieceType.KING) {
            chessEngine.getAllKingMoves(currentState, pendingMove.from)
        } else {
            chessEngine.getLegalMoves(currentState, pendingMove.from)
        }

        val matchingMove = legalMoves.find { it.to == pendingMove.to && it.promotion == pieceType }
        if (matchingMove != null) {
            _gameState.value = chessEngine.executeMove(currentState, matchingMove)
            saveGameAsync()
        }
    }

    fun cancelPromotion() {
        _gameState.value = _gameState.value.copy(pendingPromotion = null)
    }

    fun undoMove() {
        val currentState = _gameState.value
        if (currentState.moveHistory.isEmpty() || !currentState.status.requiresPlayerAction()) return

        val newHistory = currentState.moveHistory.dropLast(1)
        var state = GameState.initial().start()

        for (move in newHistory) {
            state = chessEngine.executeMove(state, move)
        }

        _gameState.value = state
    }

    fun resetGame() {
        _gameState.value = GameState.initial()
        saveGameAsync()
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

    private fun saveGameAsync() {
        scope.launch {
            try {
                val boardState = chessEngine.gameStateToBoardState(_gameState.value)
                gameRepository.saveGame(boardState)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}