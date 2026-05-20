package com.p_soft.chess.presentation.game

import GameControls
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
import androidx.compose.ui.draw.alpha
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

    LaunchedEffect(null) {
        viewModel.gameInteractor.loadGame()
    }

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
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Статус игры
            GameStatusBar(gameState = gameState)

            Spacer(modifier = Modifier.height(8.dp))

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
                        if (gameState.pendingPromotion != null) return@ChessBoard

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
                                    // ИСПРАВЛЕНО: Правильно обрабатываем ход с превращением
                                    val piece = gameState.board[selectedSquare!!]
                                    val isPromotionMove = piece?.type == PieceType.PAWN &&
                                            square.row == (if (piece.player == Player.WHITE) 7 else 0)

                                    if (isPromotionMove) {
                                        // Создаём ход без promotion (будет показан диалог)
                                        val move = Move(selectedSquare!!, square)
                                        viewModel.makeMove(move)
                                    } else {
                                        // Обычный ход
                                        val move = Move(selectedSquare!!, square)
                                        viewModel.makeMove(move)
                                    }

                                    selectedSquare = null
                                    validMoves = emptyList()
                                }
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Захваченные фигуры
            CapturedPiecesBar(gameState = gameState)

            Spacer(modifier = Modifier.height(16.dp))

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

@Composable
private fun ChessBoard(
    gameState: GameState,
    selectedSquare: Square?,
    validMoves: List<Move>,
    onSquareClick: (Square) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Верхняя координатная строка (буквы a-h)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Spacer(modifier = Modifier.width(24.dp))
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
            Spacer(modifier = Modifier.width(24.dp))
        }

        // Ряды доски (8 рядов сверху вниз: 8,7,6,5,4,3,2,1)
        for (row in 7 downTo 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Левая координата (цифра)
                Box(
                    modifier = Modifier
                        .width(24.dp)
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

                // 8 клеток в ряду
                for (col in 0..7) {
                    val square = Square(row, col)
                    val isLight = (row + col) % 2 == 0
                    val isSelected = square == selectedSquare
                    val isValidMove = validMoves.any { it.to == square }
                    val isLastMoveFrom = gameState.moveHistory.lastOrNull()?.from == square
                    val isLastMoveTo = gameState.moveHistory.lastOrNull()?.to == square

                    ChessSquare(
                        modifier = Modifier.weight(1f),
                        isLight = isLight,
                        isSelected = isSelected,
                        isValidMove = isValidMove,
                        isLastMove = isLastMoveFrom || isLastMoveTo,
                        piece = gameState.board[square],
                        onClick = { onSquareClick(square) }
                    )
                }

                // Правая координата (цифра)
                Box(
                    modifier = Modifier
                        .width(24.dp)
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

        // Нижняя координатная строка (буквы a-h)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Spacer(modifier = Modifier.width(24.dp))
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
            Spacer(modifier = Modifier.width(24.dp))
        }
    }
}

@Composable
private fun CapturedPiecesBar(gameState: GameState) {
    val whiteCaptured = gameState.capturedPieces.filter { it.player == Player.BLACK }
    val blackCaptured = gameState.capturedPieces.filter { it.player == Player.WHITE }

    if (whiteCaptured.isEmpty() && blackCaptured.isEmpty()) return

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("♟:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                whiteCaptured.forEach { piece ->
                    Text(
                        text = getPieceUnicode(piece),
                        fontSize = 14.sp,
                        modifier = Modifier.alpha(0.7f)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                blackCaptured.forEach { piece ->
                    Text(
                        text = getPieceUnicode(piece),
                        fontSize = 14.sp,
                        modifier = Modifier.alpha(0.7f)
                    )
                }
                Text("♙:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ChessSquare(
    modifier: Modifier = Modifier,
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
        modifier = modifier
            .aspectRatio(1f)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Индикатор возможного хода (пустая клетка)
        if (isValidMove && piece == null) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.2f))
            )
        }

        // Индикатор возможного взятия (клетка с фигурой противника)
        if (isValidMove && piece != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize(0.9f)
                    .border(4.dp, Color.Red.copy(alpha = 0.6f), CircleShape)
            )
        }

        // Фигура
        piece?.let {
            Text(
                text = getPieceUnicode(it),
                fontSize = 38.sp,
                color = if (it.player == Player.WHITE) Color.White else Color.Black,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.offset(y = (-2).dp)
            )
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