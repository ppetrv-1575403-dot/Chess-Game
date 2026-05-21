package com.p_soft.chess.presentation.game

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
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
import com.p_soft.chess.presentation.utils.getPieceUnicode

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
                color = MaterialTheme.colorScheme.primaryContainer
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

@Composable
private fun ChessBoardCard(
    gameState: GameState,
    selectedSquare: Square?,
    validMoves: List<Move>,
    onSquareClick: (Square) -> Unit
) {
    // Адаптивный размер доски — занимает максимум доступного пространства
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val maxSize = minOf(maxWidth, maxHeight)

        Card(
            modifier = Modifier.size(maxSize),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            ChessBoard(
                gameState = gameState,
                selectedSquare = selectedSquare,
                validMoves = validMoves,
                onSquareClick = onSquareClick
            )
        }
    }
}

@Composable
private fun GameTopBarContent(
    gameState: GameState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedContent(
                targetState = gameState.currentPlayer,
                transitionSpec = {
                    fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut()
                },
                label = "player_indicator"
            ) { player ->
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (player == Player.WHITE) Color.White
                            else Color(0xFF1A1A1A)
                        )
                        .border(2.dp, Color.Gray, CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = "Ход ${gameState.fullMoveNumber}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = if (gameState.currentPlayer == Player.WHITE) "Белые" else "Чёрные",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        StatusBadge(status = gameState.status, winner = gameState.getWinner())
    }
}

@Composable
private fun StatusBadge(status: GameStatus, winner: Player?) {
    val (bgColor, text, textColor) = when (status) {
        GameStatus.ACTIVE -> Triple(Color(0xFF4CAF50).copy(alpha = 0.15f), null, Color(0xFF4CAF50))
        GameStatus.CHECK -> Triple(Color(0xFFFF9800).copy(alpha = 0.2f), "ШАХ", Color(0xFFFF9800))
        GameStatus.CHECKMATE -> Triple(Color(0xFFF44336).copy(alpha = 0.2f), "МАТ", Color(0xFFF44336))
        GameStatus.STALEMATE -> Triple(Color(0xFF2196F3).copy(alpha = 0.2f), "ПАТ", Color(0xFF2196F3))
        else -> Triple(Color(0xFF9E9E9E).copy(alpha = 0.15f), null, Color(0xFF9E9E9E))
    }

    text?.let {
        Surface(color = bgColor, shape = RoundedCornerShape(12.dp)) {
            Text(
                text = it,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun BottomGamePanel(
    gameState: GameState,
    onUndo: () -> Unit,
    onNewGame: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            CapturedPiecesBar(gameState = gameState)
            Spacer(modifier = Modifier.height(8.dp))
            GameControls(onUndo = onUndo, onNewGame = onNewGame, gameStatus = gameState.status)
        }
    }
}

@Composable
private fun CapturedPiecesBar(gameState: GameState) {
    val whiteCaptured = gameState.capturedPieces.filter { it.player == Player.BLACK }
    val blackCaptured = gameState.capturedPieces.filter { it.player == Player.WHITE }

    if (whiteCaptured.isEmpty() && blackCaptured.isEmpty()) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(1.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("♟", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            whiteCaptured.forEach { piece ->
                Text(
                    text = getPieceUnicode(piece),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.alpha(0.8f)
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(1.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            blackCaptured.forEach { piece ->
                Text(
                    text = getPieceUnicode(piece),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.alpha(0.8f)
                )
            }
            Text("♙", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun CapturedPiecesVertical(gameState: GameState) {
    val whiteCaptured = gameState.capturedPieces.filter { it.player == Player.BLACK }
    val blackCaptured = gameState.capturedPieces.filter { it.player == Player.WHITE }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Взятые белые фигуры
        Text("Взято белыми:", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (whiteCaptured.isEmpty()) {
            Text("—", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                whiteCaptured.forEach { piece ->
                    Text(getPieceUnicode(piece), fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Взятые чёрные фигуры
        Text("Взято чёрными:", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (blackCaptured.isEmpty()) {
            Text("—", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                blackCaptured.forEach { piece ->
                    Text(getPieceUnicode(piece), fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun GameControls(
    onUndo: () -> Unit,
    onNewGame: () -> Unit,
    gameStatus: GameStatus
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FilledTonalButton(
            onClick = onUndo,
            enabled = gameStatus.requiresPlayerAction(),
            modifier = Modifier.weight(1f).height(44.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Undo, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Отменить", fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }

        Button(
            onClick = onNewGame,
            modifier = Modifier.weight(1f).height(44.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Новая игра", fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
    }
}

@Composable
private fun GameControlsVertical(
    onUndo: () -> Unit,
    onNewGame: () -> Unit,
    gameStatus: GameStatus
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilledTonalButton(
            onClick = onUndo,
            enabled = gameStatus.requiresPlayerAction(),
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Undo, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Отменить", fontSize = 12.sp)
        }

        Button(
            onClick = onNewGame,
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Новая игра", fontSize = 12.sp)
        }
    }
}

@Composable
private fun ChessBoard(
    gameState: GameState,
    selectedSquare: Square?,
    validMoves: List<Move>,
    onSquareClick: (Square) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        CoordinateRow(labels = ('a'..'h').map { it.toString() })

        for (row in 7 downTo 0) {
            Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                CoordinateLabel(text = (row + 1).toString())

                for (col in 0..7) {
                    val square = Square(row, col)
                    val isLight = (row + col) % 2 == 0
                    val isSelected = square == selectedSquare
                    val isValidMove = validMoves.any { it.to == square }
                    val isLastMove = gameState.moveHistory.lastOrNull()?.let {
                        it.from == square || it.to == square
                    } ?: false
                    val isKingInCheck = gameState.status == GameStatus.CHECK &&
                            gameState.board[square]?.type == PieceType.KING &&
                            gameState.board[square]?.player == gameState.currentPlayer

                    ChessSquare(
                        modifier = Modifier.weight(1f),
                        isLight = isLight,
                        isSelected = isSelected,
                        isValidMove = isValidMove,
                        isLastMove = isLastMove,
                        isKingInCheck = isKingInCheck,
                        piece = gameState.board[square],
                        onClick = { onSquareClick(square) }
                    )
                }

                CoordinateLabel(text = (row + 1).toString())
            }
        }

        CoordinateRow(labels = ('a'..'h').map { it.toString() })
    }
}

@Composable
private fun CoordinateRow(labels: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Spacer(modifier = Modifier.width(20.dp))
        labels.forEach { label ->
            Box(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 9.sp
                )
            }
        }
        Spacer(modifier = Modifier.width(20.dp))
    }
}

@Composable
private fun CoordinateLabel(text: String) {
    Box(
        modifier = Modifier
            .width(20.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontSize = 9.sp
        )
    }
}

@Composable
private fun ChessSquare(
    modifier: Modifier = Modifier,
    isLight: Boolean,
    isSelected: Boolean,
    isValidMove: Boolean,
    isLastMove: Boolean,
    isKingInCheck: Boolean,
    piece: Piece?,
    onClick: () -> Unit
) {
    val lightColor = Color(0xFFF0D9B5)
    val darkColor = Color(0xFFB58863)
    val selectedColor = Color(0xFF7CB342)
    val checkColor = Color(0xFFE53935)

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isKingInCheck -> checkColor.copy(alpha = 0.6f)
            isSelected -> selectedColor
            isLastMove -> if (isLight) Color(0xFFF7F769).copy(alpha = 0.6f) else Color(0xFFB5B53B).copy(alpha = 0.5f)
            isLight -> lightColor
            else -> darkColor
        },
        animationSpec = tween(300),
        label = "cell_color"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = isValidMove && piece == null,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.2f))
            )
        }

        AnimatedVisibility(
            visible = isValidMove && piece != null,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(0.8f)
                    .border(3.dp, Color(0xFFE53935).copy(alpha = 0.7f), CircleShape)
            )
        }

        AnimatedContent(
            targetState = piece,
            transitionSpec = {
                (scaleIn(initialScale = 0.5f) + fadeIn()) togetherWith
                        (scaleOut(targetScale = 0.5f) + fadeOut())
            },
            label = "piece"
        ) { currentPiece ->
            currentPiece?.let {
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
}
