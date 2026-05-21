package com.p_soft.chess.presentation.game.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.p_soft.chess.domain.model.Piece
import com.p_soft.chess.domain.model.Player
import com.p_soft.chess.presentation.theme.checkColor
import com.p_soft.chess.presentation.theme.darkSquareColor
import com.p_soft.chess.presentation.theme.lastMoveDark
import com.p_soft.chess.presentation.theme.lastMoveLight
import com.p_soft.chess.presentation.theme.lightSquareColor
import com.p_soft.chess.presentation.theme.selectedColor
import com.p_soft.chess.presentation.utils.getPieceUnicode

@Composable
fun ChessSquare(
    modifier: Modifier = Modifier,
    isLight: Boolean,
    isSelected: Boolean,
    isValidMove: Boolean,
    isLastMove: Boolean,
    isKingInCheck: Boolean,
    piece: Piece?,
    onClick: () -> Unit
) {

    val targetColor = when {
        isKingInCheck -> checkColor
        isSelected -> selectedColor
        isLastMove -> if (isLight) lastMoveLight else lastMoveDark
        isLight -> lightSquareColor
        else -> darkSquareColor
    }

    val backgroundColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "cell_color"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {

        // Индикатор возможного хода (пустая клетка)
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

        // Индикатор возможного взятия (клетка с фигурой)
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

        // Фигура с тенью для объёма
        AnimatedContent(
            targetState = piece,
            transitionSpec = {
                (scaleIn(initialScale = 0.6f) + fadeIn(animationSpec = tween(200))) togetherWith
                        (scaleOut(targetScale = 0.6f) + fadeOut(animationSpec = tween(200)))
            },
            label = "piece"
        ) { currentPiece ->
            currentPiece?.let {
                val pieceColor = if (it.player == Player.WHITE) {
                    Color(0xFFF8F8F8) // Белый с лёгким оттенком
                } else {
                    Color(0xFF1A1A1A) // Глубокий чёрный
                }

                Text(
                    text = getPieceUnicode(it),
                    style = TextStyle(
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.3f),
                            offset = Offset(1f, 2f),
                            blurRadius = 3f
                        )
                    ),
                    color = pieceColor,
                    modifier = Modifier.offset(y = (-1).dp)
                )
            }
        }
    }
}