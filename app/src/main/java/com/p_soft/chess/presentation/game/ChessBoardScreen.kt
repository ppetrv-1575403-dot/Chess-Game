package com.p_soft.chess.presentation.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.p_soft.chess.domain.model.GameState
import com.p_soft.chess.domain.model.Move
import com.p_soft.chess.domain.model.Piece
import com.p_soft.chess.domain.model.Player
import com.p_soft.chess.domain.model.Square
import com.p_soft.chess.presentation.utils.getPieceUnicode
import kotlin.text.get

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChessBoardScreen(
    viewModel: GameViewModel = hiltViewModel()
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    var selectedSquare by remember { mutableStateOf<Square?>(null) }
    var validMoves by remember { mutableStateOf<List<Move>>(emptyList()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Chess Master",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Статус игры
            GameStatusBar(gameState = gameState)

            Spacer(modifier = Modifier.height(12.dp))

            // Шахматная доска
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                ChessBoard(
                    gameState = gameState,
                    selectedSquare = selectedSquare,
                    validMoves = validMoves,
                    onSquareClick = { square ->
                        if (!gameState.status.requiresPlayerAction()) return@ChessBoard

                        if (selectedSquare == null) {
                            // Выбор фигуры
                            val piece = gameState.board[square]
                            if (piece != null && piece.player == gameState.currentPlayer) {
                                selectedSquare = square
                                validMoves = viewModel.getValidMoves(square)
                            }
                        } else {
                            // Попытка хода
                            if (square == selectedSquare) {
                                // Отмена выбора
                                selectedSquare = null
                                validMoves = emptyList()
                            } else {
                                val clickedPiece = gameState.board[square]
                                if (clickedPiece != null && clickedPiece.player == gameState.currentPlayer) {
                                    // Выбор другой своей фигуры
                                    selectedSquare = square
                                    validMoves = viewModel.getValidMoves(square)
                                } else {
                                    // Ход
                                    viewModel.onSquareClick(square, selectedSquare)
                                    selectedSquare = null
                                    validMoves = emptyList()
                                }
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Кнопки управления
            GameControls(
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

    // Диалог выбора фигуры для превращения пешки
    if (gameState.pendingPromotion != null) {
        PromotionDialog(
            player = gameState.currentPlayer,
            onPieceSelected = { pieceType ->
                viewModel.completePromotion(pieceType)
            },
            onDismiss = {
                viewModel.cancelPromotion()
            }
        )
    }
}

@Composable
private fun ChessBoard(
    gameState: GameState,
    selectedSquare: Square?,
    validMoves: List<Move>,
    onSquareClick: (Square) -> Unit
) {
    Column {
        // Координаты сверху (буквы)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Spacer(modifier = Modifier.width(20.dp))
            for (col in 0..7) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ('a' + col).toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(20.dp))
        }

        // Доска
        for (row in 7 downTo 0) {
            Row(modifier = Modifier.fillMaxWidth()) {
                // Координата слева (цифра)
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (row + 1).toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Клетки
                for (col in 0..7) {
                    val square = Square(row, col)
                    val isLight = (row + col) % 2 == 0
                    val isSelected = square == selectedSquare
                    val isValidMove = validMoves.any { it.to == square }
                    val isLastMoveFrom = gameState.moveHistory.lastOrNull()?.from == square
                    val isLastMoveTo = gameState.moveHistory.lastOrNull()?.to == square

                    ChessSquare(
                        isLight = isLight,
                        isSelected = isSelected,
                        isValidMove = isValidMove,
                        isLastMove = isLastMoveFrom || isLastMoveTo,
                        piece = gameState.board[square],
                        onClick = { onSquareClick(square) }
                    )
                }

                // Координата справа (цифра)
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (row + 1).toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Координаты снизу (буквы)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Spacer(modifier = Modifier.width(20.dp))
            for (col in 0..7) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ('a' + col).toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(20.dp))
        }
    }
}

@Composable
private fun ChessSquare(
    isLight: Boolean,
    isSelected: Boolean,
    isValidMove: Boolean,
    isLastMove: Boolean,
    piece: Piece?,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> Color(0xFF7CB342)
        isLastMove -> if (isLight) Color(0xFFF7F769) else Color(0xFFB5B53B)
        isLight -> Color(0xFFF0D9B5)
        else -> Color(0xFFB58863)
    }

    Box(
        modifier = Modifier
            //.weight(1.dp)
            .aspectRatio(1f)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Индикатор возможного хода
        if (isValidMove && piece == null) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.2f))
            )
        }

        // Индикатор возможного взятия
        if (isValidMove && piece != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize(0.85f)
                    .border(3.dp, Color.Red.copy(alpha = 0.5f), CircleShape)
            )
        }

        // Фигура
        piece?.let {
            Text(
                text = getPieceUnicode(it),
                fontSize = 36.sp,
                color = if (it.player == Player.WHITE) Color.White else Color.Black,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}