package com.p_soft.chess.presentation.game

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.p_soft.chess.domain.model.Move
import com.p_soft.chess.domain.model.Square
import com.p_soft.chess.presentation.game.components.BottomGamePanel
import com.p_soft.chess.presentation.game.components.CapturedPiecesVertical
import com.p_soft.chess.presentation.game.components.ChessBoardCard
import com.p_soft.chess.presentation.game.components.GameControlsVertical
import com.p_soft.chess.presentation.game.components.GameTopBarContent
import com.p_soft.chess.presentation.game.dialog.PromotionDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChessBoardScreen(
    viewModel: GameViewModel = hiltViewModel()
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    var selectedSquare by remember { mutableStateOf<Square?>(null) }
    var validMoves by remember { mutableStateOf<List<Move>>(emptyList()) }

    // Определяем ориентацию экрана
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 4.dp,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
            ) {
                GameTopBarContent(
                    gameState = gameState,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        },
        bottomBar = {
            if (!isLandscape) {
                // В портретной ориентации — нижняя панель
                BottomGamePanel(
                    gameState = gameState,
                    onUndo = {
                        viewModel.undoMove()
                        selectedSquare = null
                        validMoves = emptyList()
                    },
                    onNewGame = {
                        viewModel.startNewGame()
                        selectedSquare = null
                        validMoves = emptyList()
                    }
                )
            }
        }
    ) { paddingValues ->
        if (isLandscape) {
            // ГОРИЗОНТАЛЬНАЯ ОРИЕНТАЦИЯ — доска слева, управление справа
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Шахматная доска (занимает 70% ширины)
                Box(
                    modifier = Modifier
                        .weight(0.7f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    ChessBoardCard(
                        gameState = gameState,
                        selectedSquare = selectedSquare,
                        validMoves = validMoves,
                        onSquareClick = { square ->
                            if (!gameState.status.requiresPlayerAction()) return@ChessBoardCard
                            if (gameState.pendingPromotion != null) return@ChessBoardCard

                            if (selectedSquare == null) {
                                val piece = gameState.board[square]
                                if (piece != null && piece.player == gameState.currentPlayer) {
                                    selectedSquare = square
                                    validMoves = viewModel.getValidMoves(square)
                                }
                            } else {
                                if (square == selectedSquare) {
                                    selectedSquare = null
                                    validMoves = emptyList()
                                } else {
                                    val clickedPiece = gameState.board[square]
                                    if (clickedPiece != null && clickedPiece.player == gameState.currentPlayer) {
                                        selectedSquare = square
                                        validMoves = viewModel.getValidMoves(square)
                                    } else {
                                        viewModel.onSquareClick(square, selectedSquare)
                                        selectedSquare = null
                                        validMoves = emptyList()
                                    }
                                }
                            }
                        }
                    )
                }

                // Панель управления (30% ширины)
                Column(
                    modifier = Modifier
                        .weight(0.3f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Захваченные фигуры
                    CapturedPiecesVertical(gameState = gameState)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Кнопки управления
                    GameControlsVertical(
                        onUndo = {
                            viewModel.undoMove()
                            selectedSquare = null
                            validMoves = emptyList()
                        },
                        onNewGame = {
                            viewModel.startNewGame()
                            selectedSquare = null
                            validMoves = emptyList()
                        },
                        gameStatus = gameState.status
                    )
                }
            }
        } else {
            // ПОРТРЕТНАЯ ОРИЕНТАЦИЯ — доска сверху, управление снизу
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                ChessBoardCard(
                    gameState = gameState,
                    selectedSquare = selectedSquare,
                    validMoves = validMoves,
                    onSquareClick = { square ->
                        if (!gameState.status.requiresPlayerAction()) return@ChessBoardCard
                        if (gameState.pendingPromotion != null) return@ChessBoardCard

                        if (selectedSquare == null) {
                            val piece = gameState.board[square]
                            if (piece != null && piece.player == gameState.currentPlayer) {
                                selectedSquare = square
                                validMoves = viewModel.getValidMoves(square)
                            }
                        } else {
                            if (square == selectedSquare) {
                                selectedSquare = null
                                validMoves = emptyList()
                            } else {
                                val clickedPiece = gameState.board[square]
                                if (clickedPiece != null && clickedPiece.player == gameState.currentPlayer) {
                                    selectedSquare = square
                                    validMoves = viewModel.getValidMoves(square)
                                } else {
                                    viewModel.onSquareClick(square, selectedSquare)
                                    selectedSquare = null
                                    validMoves = emptyList()
                                }
                            }
                        }
                    }
                )
            }
        }
    }

    // Диалог превращения пешки
    if (gameState.pendingPromotion != null) {
        PromotionDialog(
            player = gameState.currentPlayer,
            currentPieces = gameState.board,
            onPieceSelected = { pieceType ->
                viewModel.completePromotion(pieceType)
            },
            onDismiss = {
                viewModel.cancelPromotion()
            }
        )
    }
}
