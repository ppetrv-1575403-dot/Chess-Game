package com.p_soft.chess.domain.usecase

import com.p_soft.chess.domain.model.GameState
import com.p_soft.chess.domain.model.Move
import com.p_soft.chess.domain.model.PieceType
import com.p_soft.chess.domain.model.Player
import com.p_soft.chess.domain.model.Square
import com.p_soft.chess.domain.repository.GameRepository
import com.p_soft.chess.engine.ChessEngine
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

    fun startNewGame() {
        _gameState.value = GameState.initial().start()
        saveGameAsync()
    }

    fun getValidMoves(square: Square): List<Move> {
        return chessEngine.getLegalMoves(_gameState.value, square)
    }

    fun makeMove(move: Move): Boolean {
        val currentState = _gameState.value

        if (!currentState.status.requiresPlayerAction()) return false

        val legalMoves = chessEngine.getLegalMoves(currentState, move.from)
        if (legalMoves.none { it.to == move.to && it.promotion == move.promotion }) {
            return false
        }

        val piece = currentState.board[move.from]
        if (piece?.type == PieceType.PAWN &&
            move.to.row == (if (piece.player == Player.WHITE) 7 else 0) &&
            move.promotion == null) {
            _gameState.value = currentState.copy(pendingPromotion = move)
            return true
        }

        _gameState.value = chessEngine.executeMove(currentState, move)
        saveGameAsync()
        return true
    }

    fun completePromotion(pieceType: PieceType) {
        val currentState = _gameState.value
        val pendingMove = currentState.pendingPromotion ?: return
        makeMove(pendingMove.copy(promotion = pieceType))
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