package com.p_soft.chess.presentation.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.p_soft.chess.domain.model.GameState
import com.p_soft.chess.domain.model.GameStatus
import com.p_soft.chess.domain.model.Move
import com.p_soft.chess.domain.model.Piece
import com.p_soft.chess.domain.model.PieceType
import com.p_soft.chess.domain.model.Player
import com.p_soft.chess.domain.model.Square

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
                title = { Text("Chess Master") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Статус игры
            GameStatusBar(gameState = gameState)

            Spacer(modifier = Modifier.height(8.dp))

            // Шахматная доска
            ChessBoard(
                gameState = gameState,
                selectedSquare = selectedSquare,
                validMoves = validMoves,
                onSquareClick = { square ->
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
                            // Повторный клик по той же клетке - отмена выбора
                            selectedSquare = null
                            validMoves = emptyList()
                        } else {
                            // Проверяем, не кликнули ли по своей фигуре
                            val clickedPiece = gameState.board[square]
                            if (clickedPiece != null && clickedPiece.player == gameState.currentPlayer) {
                                // Переключение на другую свою фигуру
                                selectedSquare = square
                                validMoves = viewModel.getValidMoves(square)
                            } else {
                                // Пытаемся сделать ход
                                viewModel.onSquareClick(square, selectedSquare)
                                selectedSquare = null
                                validMoves = emptyList()
                            }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Информация о ходе
            MoveInfo(gameState = gameState)

            Spacer(modifier = Modifier.height(8.dp))

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
fun ChessBoard(
    gameState: GameState,
    selectedSquare: Square?,
    validMoves: List<Move>,
    onSquareClick: (Square) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Отображение координат сверху (буквы)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                // Пустая клетка для выравнивания
                Box(modifier = Modifier.weight(0.1f))

                for (col in 0..7) {
                    Box(
                        modifier = Modifier.weight(0.1125f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ('a' + col).toString(),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Шахматная доска
            for (row in 7 downTo 0) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Координата слева (цифра)
                    Box(
                        modifier = Modifier
                            .weight(0.1f)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (row + 1).toString(),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Клетки доски
                    for (col in 0..7) {
                        val square = Square(row, col)
                        val isLight = (row + col) % 2 == 0
                        val isSelected = square == selectedSquare
                        val isValidMove = validMoves.any { it.to == square }
                        val isLastMove = gameState.moveHistory.lastOrNull()?.let {
                            it.from == square || it.to == square
                        } ?: false

                        ChessSquare(
                            isLight = isLight,
                            isSelected = isSelected,
                            isValidMove = isValidMove,
                            isLastMove = isLastMove,
                            piece = gameState.board[square],
                            onClick = { onSquareClick(square) }
                        )
                    }

                    // Координата справа (цифра)
                    Box(
                        modifier = Modifier
                            .weight(0.1f)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (row + 1).toString(),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Отображение координат снизу (буквы)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(modifier = Modifier.weight(0.1f))

                for (col in 0..7) {
                    Box(
                        modifier = Modifier.weight(0.1125f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ('a' + col).toString(),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChessSquare(
    isLight: Boolean,
    isSelected: Boolean,
    isValidMove: Boolean,
    isLastMove: Boolean,
    piece: Piece?,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> Color(0xFF7CB342) // Зеленый для выбранной клетки
        isLastMove -> if (isLight) Color(0xFFF7F769) else Color(0xFFB5B53B) // Желтый для последнего хода
        isValidMove && piece != null -> Color(0xFFEF5350).copy(alpha = 0.5f) // Красный для взятия
        isLight -> Color(0xFFF0D9B5) // Светлая клетка
        else -> Color(0xFFB58863) // Темная клетка
    }

    Box(
        modifier = Modifier
            //.weight(0.1125f)
            .aspectRatio(1f)
            .background(backgroundColor)
            .then(
                if (isValidMove && piece == null) {
                    Modifier.background(
                        Color.Black.copy(alpha = 0.2f),
                        CircleShape
                    )
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Индикатор возможного хода
        if (isValidMove && piece == null) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        Color.Black.copy(alpha = 0.15f),
                        CircleShape
                    )
            )
        }

        // Индикатор возможного взятия
        if (isValidMove && piece != null) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .border(4.dp, Color.Red.copy(alpha = 0.6f), CircleShape)
            )
        }

        // Фигура
        piece?.let {
            Text(
                text = getPieceUnicode(it),
                fontSize = if (piece.type == PieceType.PAWN) 28.sp else 32.sp,
                color = if (it.player == Player.WHITE) Color.White else Color.Black,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MoveInfo(gameState: GameState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Текущий ход
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                if (gameState.currentPlayer == Player.WHITE) Color.White
                                else Color.Black,
                                CircleShape
                            )
                            .border(1.dp, Color.Gray, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ход ${gameState.fullMoveNumber}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text(
                    text = "Ходят: ${if (gameState.currentPlayer == Player.WHITE) "Белые" else "Чёрные"}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Последний ход
            if (gameState.moveHistory.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                val lastMove = gameState.moveHistory.last()
                val lastPiece = gameState.board[lastMove.to] ?: gameState.board[lastMove.from]
                Text(
                    text = "Последний ход: ${lastMove.toAlgebraicNotation(lastPiece,
                        gameState.status == GameStatus.CHECK,
                        gameState.status == GameStatus.CHECKMATE)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}





fun getPieceUnicode(piece: Piece): String {
    return when (piece.type) {
        PieceType.KING -> if (piece.player == Player.WHITE) "♔" else "♚"
        PieceType.QUEEN -> if (piece.player == Player.WHITE) "♕" else "♛"
        PieceType.ROOK -> if (piece.player == Player.WHITE) "♖" else "♜"
        PieceType.BISHOP -> if (piece.player == Player.WHITE) "♗" else "♝"
        PieceType.KNIGHT -> if (piece.player == Player.WHITE) "♘" else "♞"
        PieceType.PAWN -> if (piece.player == Player.WHITE) "♙" else "♟"
    }
}