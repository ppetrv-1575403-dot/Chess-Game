package com.p_soft.chess.presentation.game

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

import com.p_soft.chess.domain.model.PieceType
import com.p_soft.chess.domain.usecase.GameInteractor
import com.p_soft.chess.domain.model.GameState
import com.p_soft.chess.domain.model.Move
import com.p_soft.chess.domain.model.Square

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameInteractor: GameInteractor
) : ViewModel() {

    val gameState: StateFlow<GameState> = gameInteractor.gameState
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            GameState.initial()
        )

    init {
        loadSavedGame()
    }

    private fun loadSavedGame() {
        gameInteractor.loadGame()
    }

    fun onSquareClick(square: Square, selectedSquare: Square?) {
        val currentState = gameState.value

        // Если игра не активна, игнорируем клики
        if (!currentState.status.requiresPlayerAction()) return

        // Если ожидается выбор фигуры для превращения
        if (currentState.pendingPromotion != null) return

        if (selectedSquare == null) return

        val clickedPiece = currentState.board[square]

        // Если кликнули по своей фигуре
        if (clickedPiece != null && clickedPiece.player == currentState.currentPlayer) {
            if (square == selectedSquare) {
                // Повторный клик по выбранной фигуре - отмена выбора
                return
            }
        }

        // Пытаемся сделать ход
        val move = Move(selectedSquare, square)
        gameInteractor.makeMove(move)
    }

    fun getValidMoves(square: Square): List<Move> {
        return gameInteractor.getValidMoves(square)
    }

    fun undoMove() = gameInteractor.undoMove()
    fun startNewGame() = gameInteractor.startNewGame()
    fun completePromotion(pieceType: PieceType) = gameInteractor.completePromotion(pieceType)
    fun cancelPromotion() = gameInteractor.cancelPromotion()
}